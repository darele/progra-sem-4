package info.kgeorgiy.ja.piche_kruz.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {
    private static final byte[] ERROR_OUTPUT = BigInteger.valueOf(0).toByteArray();

    private static byte[] getHashFromFile(String file, MessageDigest md) throws IOException, SecurityException {
        byte[] buffer = new byte[1024];
        try (FileInputStream reader = new FileInputStream(file)) {
            int readCharacters = reader.read(buffer);
            while (readCharacters != -1) {
                md.update(buffer, 0, readCharacters);
                readCharacters = reader.read(buffer);
            }
            return md.digest();
        }
    }

    private static String toHex(byte[] hashSum) {
        BigInteger bigInt = new BigInteger(1, hashSum);
//NOTE        String.format("");
        return String.format("%064x", bigInt);
    }

    private static void outputError(String message, Exception e) {
        System.err.println(message + " " + e.getMessage());
    }

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("no arguments given");
            return;
        } else if (args.length != 2) { // 3 agumnets
            System.err.println("Expected 2 arguments, " + args.length + "given");
            return;
        } else if (args[0] == null) {
            System.err.println("Could not read input file, null argument given");
            return;
        } // NOTE: исправить
         else if (args[1] == null) {
            System.err.println("Cannot create out file with null name, null argument given");
            return;
        }
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            outputError("Something went wrong when initializing encrypting function", e);
            return;
        }
        Path inputFilePath;
        try {
            inputFilePath = Path.of(args[0]);
        } catch (InvalidPathException e) {
            outputError("Invalid path given, unable to open input file", e);
            return;
        }

        Path outputFilePath;
        try {
            outputFilePath = Path.of(args[1]);
            if (outputFilePath.getParent() != null) {
                Files.createDirectories(outputFilePath.getParent());
            }
        } catch (InvalidPathException e) {
            outputError("Something is wrong with the directory hierarchy of the output file", e);
            return;
        } catch (FileAlreadyExistsException e) {
            outputError("Something is wrong with the directory hierarchy of the output file", e);
            return;
        } catch (IOException | SecurityException e) {
            outputError("Something went wrong when creating output file hierarchy", e);
            return;
        }

        try (BufferedReader inputReader = Files.newBufferedReader(inputFilePath, StandardCharsets.UTF_8)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8)) {
                byte[] hashSum = null; // :NOTE: не чиать все сразу
                String file = inputReader.readLine();
                while (file != null) {
                    try {
                        hashSum = getHashFromFile(file, md);
                    } catch (IOException | SecurityException e) {
                        hashSum = ERROR_OUTPUT;
                    } finally {
                        String ans = toHex(hashSum);
                        writer.write(ans + " " + file + System.lineSeparator());
                    }
                    file = inputReader.readLine();
                }
            } catch (IOException | SecurityException e) {
                outputError("Unable to write to output file", e);
            }
        } catch (IOException | SecurityException e) {
            outputError("Can't open input file", e);
        }
    }
}
