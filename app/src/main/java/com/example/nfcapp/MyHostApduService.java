package com.example.nfcapp;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

public class MyHostApduService extends HostApduService {

    private static final String TAG = "MyHostApduService";

    String aid = "F0010203040506"; // Replace with your AID from XML
    byte[] aidBytes = hexStringToByteArray(aid);

    // Create the SELECT AID command
    byte[] SELECT_AID_COMMAND = new byte[]{
            (byte) 0x00, // CLA
            (byte) 0xA4, // INS
            (byte) 0x04, // P1
            (byte) 0x00, // P2
            (byte) aidBytes.length, // LC
    };

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
    private static final byte[] SELECT_OKAY = new byte[]{(byte) 0x90, (byte) 0x00};
    private static final byte[] UNKNOWN_CMD = new byte[]{(byte) 0x6D, (byte) 0x00};

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        byte[] ourCommand = concatenate(SELECT_AID_COMMAND, aidBytes);

        Log.d(TAG, "Received APDU: " + bytesToHex(commandApdu));
        if (Arrays.equals(commandApdu, ourCommand)) {
            // Handle the APDU command here
            Log.d(TAG, "Command APDU match: " + bytesToHex(commandApdu));
            return SELECT_OKAY;
        } else {
            return UNKNOWN_CMD;
        }
    }

    public static byte[] concatenate(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Deactivated: " + reason);
        // Handle deactivation
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}



