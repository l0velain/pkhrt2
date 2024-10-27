package cartridge;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class checksum {
    private static File sav = null;

    public checksum(File sav) {
        this.sav = sav;
    }

    public int fixedCrc16citt(byte[] bytes) {
        int crc = 0xFFFF; // Initial value
        for (byte b: bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= 0x1021; // Truncated polynomial
            }
        }
        crc &= 0xFFFF;
        return ((crc >> 8) & 0xFF) | ((crc & 0xFF) << 8); // Fix endianness and return our 2 bytes.
    }

    public void updateChecksums() {
        try (RandomAccessFile stream = new RandomAccessFile(sav, "rw")) {
            stream.seek(0x19400); // First checksum: player data.
            byte[] bytes = new byte[0xB0];
            stream.readFully(bytes);
            final int crc = fixedCrc16citt(bytes);
            stream.seek(0x194B2); // First checksum: player data.
            stream.writeShort(crc);
            stream.seek(0x25F36); // Also update the final checksums block.
            stream.writeShort(crc);
            updateFinal();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateFinal() {
        try (RandomAccessFile stream = new RandomAccessFile(sav, "rw")) {
            stream.seek(0x25F00); // Beginning of the final checksum block.
            byte[] bytes = new byte[0x94];
            stream.readFully(bytes);
            final int crc = fixedCrc16citt(bytes);
            stream.seek(0x25FA2); // Final checksum.
            stream.writeShort(crc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
