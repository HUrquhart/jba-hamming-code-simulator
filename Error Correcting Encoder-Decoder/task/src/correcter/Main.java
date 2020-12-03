package correcter;

import java.io.*;
import java.util.Random;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Write a mode: ");
        switch(scanner.nextLine()){
            case "encode":
                hammingCode();
                break;
            case "send":
                simulateErrors();
                break;
            case "decode":
                decode();
                break;
        }

    }

    private static void simulateErrors() {
        byte[] bytesToProcess = new byte[]{};

        File file = new File("encoded.txt");
        try(InputStream inputStream = new FileInputStream(file)){
            bytesToProcess = inputStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(OutputStream outputStream = new FileOutputStream("received.txt")) {
            Random random = new Random();
            for(byte b : bytesToProcess){
                byte rand = (byte)random.nextInt(8);
                byte change = (byte) (0x1 << rand);
                outputStream.write((b ^ change));
                System.out.println();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static byte findAndFix(byte b){
        // pull all bits but last out
        byte n1 = (byte)(b >> 7 & 0b00000001);
        byte n2 = (byte)(b >> 6 & 0b00000001);
        byte n3 = (byte)(b >> 5 & 0b00000001);
        byte n4 = (byte)(b >> 4 & 0b00000001);
        byte n5 = (byte)(b >> 3 & 0b00000001);
        byte n6 = (byte)(b >> 2 & 0b00000001);
        byte n7 = (byte)(b >> 1 & 0b00000001);
        // x-x-x-x-
        boolean p1 = ((n1 + n3 + n5 + n7) % 2 == 1);
        // -xx--xx-
        boolean p2 = ((n2 + n3 + n6 + n7) % 2 == 1);
        // ---xxxx-
        boolean p3 = ((n4 + n5 + n6 + n7) % 2 == 1);
        // if all three parities are odd then d4 contains the error
        if(p1 && p2 && p3) b ^= 2;
        // if only p1 and p2 are odd d1 contains the error
        if(p1 && p2 && !p3) b ^= 0b00100000;
        // if only p1 and p3 are odd then d2 is the error
        if(p1 && p3 && !p2) b ^= 0b00001000;
        // if only p2 and p3 are odd d3 is the error
        if(p2 && p3 && !p1) b ^= 0b00000100;
        // if only one p is odd then the error is p
        // we dont care about the parity bits when only one bit is in error
        return b;
    }

    private static void decode() {


        byte[] bytesToProcess = new byte[]{};

        File file = new File("received.txt");
        try(InputStream inputStream = new FileInputStream(file)){
            bytesToProcess = inputStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try(OutputStream outputStream = new FileOutputStream("decoded.txt")) {

            // take in two bytes at a time as we know the format is 2 * n original bytes
            // grab each bit and validate the bits
            // we also know that the last bit in every byte should be 0
            // if a 1 is found simply take the current bits in the current byte
            // we are also looking for even parity

            byte byteToWrite = 0;

            for(int i = 0; i < bytesToProcess.length; i+= 2){
                byte b1 = bytesToProcess[i];
                byte b2 = bytesToProcess[i+1];
                if((b1 & 0b00000001) != 1){
                    b1 = findAndFix(b1);
                }
                if((b2 & 0b00000001) != 1){
                    b2 = findAndFix(b2);
                }
                // ..0.000.
                byte n1 = (byte)(b1 >> 5 & 0b00000001);
                byte n2 = (byte)(b1 >> 3 & 0b00000001);
                byte n3 = (byte)(b1 >> 2 & 0b00000001);
                byte n4 = (byte)(b1 >> 1 & 0b00000001);

                byte n5 = (byte)(b2 >> 5 & 0b00000001);
                byte n6 = (byte)(b2 >> 3 & 0b00000001);
                byte n7 = (byte)(b2 >> 2 & 0b00000001);
                byte n8 = (byte)(b2 >> 1 & 0b00000001);

                byteToWrite = (byte) ((byteToWrite ^ n1) << 1); // 7
                byteToWrite = (byte) ((byteToWrite ^ n2) << 1); // 6
                byteToWrite = (byte) ((byteToWrite ^ n3) << 1); // 5
                byteToWrite = (byte) ((byteToWrite ^ n4) << 1); // 4
                byteToWrite = (byte) ((byteToWrite ^ n5) << 1); // 3
                byteToWrite = (byte) ((byteToWrite ^ n6) << 1); // 2
                byteToWrite = (byte) ((byteToWrite ^ n7) << 1); // 1
                byteToWrite = (byte) ((byteToWrite ^ n8));      // 0
                outputStream.write(byteToWrite);
                byteToWrite = 0;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static boolean IsPowerOfTwo(int x) {
        return (x != 0) && ((x & (x - 1)) == 0);
    }


    // lets ensure that we are correctly encoding the data
    // ..0.000.
    // each dot is a parity bit
    // the last bit is not used and is always set to 0
    private static void hammingCode(){
        byte[] bytesToProcess = new byte[]{};

        try(InputStream inputStream = new FileInputStream("send.txt")){
            BufferedReader in = new BufferedReader(new FileReader("send.txt"));
            String line = in.readLine();
            while(line != null)
            {
                System.out.println(line);
                line = in.readLine();
            }
            in.close();
            bytesToProcess = inputStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(OutputStream outputStream = new FileOutputStream("encoded.txt")) {
            // using hamming code (7,4) each byte to be encoded requires 2 bytes
            for (byte b: bytesToProcess){
                byte temp1 = 0b00000000;
                // pull out needed bits from current byte
                byte n1 = (byte)(b >> 7 & 0b00000001);
                byte n2 = (byte)(b >> 6 & 0b00000001);
                byte n3 = (byte)(b >> 5 & 0b00000001);
                byte n4 = (byte)(b >> 4 & 0b00000001);
                // calculate the resulting paritys
                byte p1 = (byte) ((n1 + n2 + n4) % 2);
                byte p2 = (byte) ((n1 + n3 + n4) % 2);
                byte p3 = (byte) ((n2 + n3 + n4) % 2);
                // write 4 bits to a byte -> expand to **B*BBB*  where each asterisk is a parity bit
                // write bits to byte in order and ensure last bit is zero then write it to file
                temp1 = (byte) (temp1 ^ (p1 << 7));
                temp1 = (byte) (temp1 ^ (p2 << 6));
                temp1 = (byte) (temp1 ^ (n1 << 5));
                temp1 = (byte) (temp1 ^ (p3 << 4));
                temp1 = (byte) (temp1 ^ (n2 << 3));
                temp1 = (byte) (temp1 ^ (n3 << 2));
                temp1 = (byte) (temp1 ^ (n4 << 1));

                outputStream.write(temp1);

                byte temp2 = 0b00000000;
                byte n5 = (byte)(b >> 3 & 0b00000001);
                byte n6 = (byte)(b >> 2 & 0b00000001);
                byte n7 = (byte)(b >> 1 & 0b00000001);
                byte n8 = (byte)(b & 0b00000001);
                // calculate the resulting paritys
                byte p4 = (byte) ((n5 + n6 + n8) % 2);
                byte p5 = (byte) ((n5 + n7 + n8) % 2);
                byte p6 = (byte) ((n6 + n7 + n8) % 2);
                // write 4 bits to a byte -> expand to **B*BBB*  where each asterisk is a parity bit
                // write bits to byte in order and ensure last bit is zero then write it to file
                temp2 = (byte) (temp2 ^ (p4 << 7));
                temp2 = (byte) (temp2 ^ (p5 << 6));
                temp2 = (byte) (temp2 ^ (n5 << 5));
                temp2 = (byte) (temp2 ^ (p6 << 4));
                temp2 = (byte) (temp2 ^ (n6 << 3));
                temp2 = (byte) (temp2 ^ (n7 << 2));
                temp2 = (byte) (temp2 ^ (n8 << 1));

                outputStream.write(temp2);
            }
        }catch (IOException ignored){ }

    }

}
