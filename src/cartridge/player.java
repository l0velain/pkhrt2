package cartridge;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.Arrays;

public class player {
    private static final byte[] name = new byte[8]; // 7 characters + 1 null byte
    private static int gender;
    private static byte[] displayedName;

    public static final int MALE = 0x00;
    public static final int FEMALE = 0x01;

    public player(File f) {
        // Read byte at 0x19421 to determine the player's gender and read string at 0x19404 to determine the player's name (MAX 7 characters).
        try (RandomAccessFile stream = new RandomAccessFile(f, "r")) {
            stream.seek(0x19421);
            gender = stream.read();
            stream.seek(0x19404);
            byte i = stream.readByte();
            int counter = 0;
            int byteCounter = 0x19404;
            while (i != -1) { // -2^7 = -128. +2^7-1 = 127. That's all you need to know.
                if (i == 0x00) {
                    stream.seek(++byteCounter);
                    i = (stream.readByte());
                    continue;
                }
                name[counter++] = i;
                stream.seek(++byteCounter);
                i = stream.readByte();
            }
            displayedName = new byte[counter]; // Remove null bytes from our name array in a C-ish way.
            int finalCounter = 0;
            while(finalCounter < counter) {
                displayedName[finalCounter] = name[finalCounter++]; // The content of displayedName will be displayed on our JTextField.
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // Shouldn't get triggered (hopefully)!
        }
    }

    public int getGender() {
        return gender;
    }

    public String getName() {
        return new String(displayedName);
    }

    public void setGender(int g) {
        gender = g;
    }

    public void setName(String n) {
        Arrays.fill(name, (byte) 0x00);
        int i = 0;
        while(i < n.length()) {
            name[i] = n.getBytes()[i];
            i++;
        }
    }

    public void writeGender(File f) {
        try (RandomAccessFile gStream = new RandomAccessFile(f, "rw")) {
            gStream.seek(0x19421);
            gStream.writeByte(gender);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeName(File f) {
        try (RandomAccessFile nStream = new RandomAccessFile(f, "rw")) {
            int byteCounter = 0x19404;
            int c = 0;
            int i = 0;
            int toSixteen = 0; // Used to fill the savefile with zeroes after ending our string with 0x00, 0xFF, 0xFF without overwriting other info at 0x19414.
            while(name[i] != 0x00) { // If name[i] is even, write that byte. If not, write 0x00. Keep doing so until name[i] equals 0x00.
                nStream.seek(byteCounter);
                if((c & 1) == 0)
                    nStream.write(name[i++]);
                else
                    nStream.write(0x00);
                byteCounter++;
                c++;
                toSixteen++;
            }
            // The player's name ends with these bytes: 0x00, 0xFF. 0xFF
            nStream.seek(byteCounter++);
            nStream.write(0x00);
            nStream.seek(byteCounter++);
            nStream.write(0xFF);
            nStream.seek(byteCounter++);
            nStream.write(0xFF);
            toSixteen += 3;
            while(toSixteen < 16) {
                nStream.seek(byteCounter++);
                nStream.write(0x00);
                toSixteen++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}