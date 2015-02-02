package com.techshroom.hendrix;

/**
 * Small utilities, some of these may not be used in production.
 * 
 * @author Kenzie Togami
 */
public final class Util {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Convert a byte array to a hex string.
     * 
     * @param bytes - The bytes to convert
     * @return A hex string representing the bytes
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private Util() {
        throw new AssertionError();
    }
}
