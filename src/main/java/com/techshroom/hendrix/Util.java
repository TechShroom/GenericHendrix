package com.techshroom.hendrix;

import static com.google.common.base.Verify.verify;
import fj.data.Array;

/**
 * Small utilities, some of these may not be used in production.
 * 
 * @author Kenzie Togami
 */
public final class Util {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

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
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return String.copyValueOf(hexChars);
    }

    /**
     * Splits the given string by {@code sep} and then join all but the last
     * part with {@code replace}. If there is no separator, the first part will
     * be the whole string and the last part will be empty.
     * 
     * @param str - The string to split
     * @param sep - The character to split by
     * @param replace - The character to join with, effectively replacing
     *        {@code sep}
     * @return An array consisting of the first parts and the last part, in that
     *         order
     */
    public static Array<String> splitReplaceAndPopLast(String str, int sep,
                    int replace) {
        String firstBits = null;
        String lastBit = null;
        int lastSep = str.lastIndexOf(sep);
        if (lastSep > -1) {
            // last bit is always the same in every case
            lastBit = str.substring(lastSep + 1);
            if (sep == replace) {
                // special case: with no replace it's quicker to just strip off
                // the
                // last bit
                firstBits = str.substring(0, lastSep);
            } else {
                int[] data = stringToCodePoints(str);
                for (int i = 0; i < lastSep; i++) {
                    int codePoint = data[i];
                    if (codePoint == sep) {
                        codePoint = replace;
                    }
                    data[i] = codePoint;
                }
                firstBits = new String(data, 0, lastSep);
            }
        } else {
            firstBits = str;
            lastBit = "";
        }
        verify(firstBits != null, "no first parts");
        verify(lastBit != null, "no last part");
        return Array.array(firstBits, lastBit);
    }

    /**
     * Converts a String to an array of code points.
     * 
     * @param str - The string to convert
     * @return The string as an array of code points
     */
    public static int[] stringToCodePoints(String str) {
        int[] codePoints = new int[str.length()];
        for (int i = 0; i < codePoints.length; i++) {
            codePoints[i] = str.codePointAt(i);
        }
        return codePoints;
    }

    private Util() {
        throw new AssertionError();
    }
}
