package com.parissakalaee.nfcapp;

public class NfcUtils {
    public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] selectAid(String aid) {
        Constants.SELECT_AID_COMMAND[4] = (byte) hexStringToByteArray(aid).length;
        return concatenate(Constants.SELECT_AID_COMMAND, hexStringToByteArray(aid));
    }

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = Constants.HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = Constants.HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}