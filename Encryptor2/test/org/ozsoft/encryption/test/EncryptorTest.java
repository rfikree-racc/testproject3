package org.ozsoft.encryption.test;

import java.util.Random;

import org.ozsoft.encryption.Encryptor;

/**
 * Test driver for the Encryptor class.
 * 
 * @author Oscar Stigter
 */
public class EncryptorTest {

    private static final String CLEARTEXT = "This is a very secret message.";

    private static final String PASSWORD = "aBc#123!";

    private static final int BLOCK_SIZE = 10 * 1024 * 1024; // 10MB
    
    public static void main(String[] args) throws Exception {
        Random random = new Random();
        byte[] cleardata = null;
        byte[] cipherdata = null;
        String cleartext = null;
        String ciphertext = null;
        long startTime = 0L;
        long duration = 0L;

        startTime = System.currentTimeMillis();
        Encryptor encryptor = new Encryptor();
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Encryptor created in %d ms\n", duration);

        // Set the shared key based on the password.
        startTime = System.currentTimeMillis();
        encryptor.setKey(PASSWORD);
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Key generated in %d ms\n", duration);

        // Encrypt and decrypt a byte array.
        startTime = System.currentTimeMillis();
        cipherdata = encryptor.encrypt(CLEARTEXT.getBytes());
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Byte array encrypted in %d ms\n", duration);
        startTime = System.currentTimeMillis();
        cleardata = encryptor.decrypt(cipherdata);
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Byte array decrypted in %d ms\n", duration);

        // Encrypt and decrypt a large data block.
        cleardata = new byte[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            cleardata[i] = (byte) random.nextInt(256);
        }
        startTime = System.currentTimeMillis();
        cipherdata = encryptor.encrypt(cleardata);
        duration = System.currentTimeMillis() - startTime;
        System.out.println("Large text block encrypted in  " + duration + " ms.");
        // Decrypt large data block.
        startTime = System.currentTimeMillis();
        cleardata = encryptor.decrypt(cipherdata);
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Large data block decrypted in %d ms\n", duration);

        // Encypt and decrypt a String.
        cleartext = CLEARTEXT;
        System.out.format("cleartext:  '%s'\n", cleartext);
        startTime = System.currentTimeMillis();
        ciphertext = encryptor.encrypt(cleartext);
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Ciphertext: '%s'\n", ciphertext);
        System.out.format("String encrypted in %d ms\n", duration);
        startTime = System.currentTimeMillis();
        cleartext = encryptor.decrypt(ciphertext);
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Cleartext:  '%s'\n", cleartext);
        System.out.format("String decrypted in %d ms\n", duration);

//        // Encypt and decrypt a file.
//        startTime = System.currentTimeMillis();
//        encryptor.encryptFile("data/Secret.doc");
//        duration = System.currentTimeMillis() - startTime;
//        System.out.println("Encypted file in " + duration + " ms.");
//        startTime = System.currentTimeMillis();
//        encryptor.decryptFile("data/Secret.doc.enc");
//        duration = System.currentTimeMillis() - startTime;
//        System.out.println("Decypted file in " + duration + " ms.");
    }

}
