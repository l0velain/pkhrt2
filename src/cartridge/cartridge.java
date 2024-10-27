package cartridge;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;

public class cartridge {
    public static boolean isValid(File f) {
        if(f.length() != 524288)
            return false;
        player p = new player(f);
        if(p.getGender() != player.MALE && p.getGender() != player.FEMALE)
            return false;
        try (RandomAccessFile stream = new RandomAccessFile(f, "r")) {
            stream.seek(0x19400);
            byte[] bytes = new byte[0xB0];
            stream.readFully(bytes);
            checksum c = new checksum(f);
            final int crc = c.fixedCrc16citt(bytes);
            stream.seek(0x194B2);
            if((((crc) & 0xFF00) | ((crc) & 0xFF)) != stream.readUnsignedShort())
                return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
