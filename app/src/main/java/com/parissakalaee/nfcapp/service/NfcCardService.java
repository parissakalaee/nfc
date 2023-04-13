package com.parissakalaee.nfcapp.service;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import com.parissakalaee.nfcapp.Constants;
import com.parissakalaee.nfcapp.NfcUtils;

import java.util.Arrays;

public class NfcCardService extends HostApduService {

    private static final String TAG = NfcCardService.class.getSimpleName();
    byte[] messageData = new byte[] { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78 };

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        byte[] response = Constants.UNKNOWN_CMD;

        Log.d(TAG, "Received APDU Cmd: " + NfcUtils.bytesToHexString(commandApdu));
        if (Arrays.equals(commandApdu, NfcUtils.selectAid(Constants.AID))) {
            response = Constants.OKAY;
        }else if (Arrays.equals(commandApdu, Constants.GET_VERSION_COMMAND)) {
            response = buildVersionResponse();
        }else if (Arrays.equals(commandApdu, Constants.READ_MESSAGE_COMMAND)) {
            response = buildMessageResponse(messageData);
        }
        Log.d(TAG, "Response APDU match: " + NfcUtils.bytesToHexString(response));
        return response;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Deactivated: " + reason);
        // Handle deactivation
    }
    private byte[] buildVersionResponse() {
        byte[] response = Arrays.copyOf(Constants.VERSION_NUMBER, Constants.VERSION_NUMBER.length + 2);
        System.arraycopy(Constants.OKAY, 0, response, Constants.VERSION_NUMBER.length, 2);
        return response;
    }
    private byte[] buildMessageResponse(byte[] messageData) {
        if (messageData == null) {
            return Constants.UNKNOWN_CMD;
        }

        byte[] response = new byte[messageData.length + 2];
        System.arraycopy(messageData, 0, response, 0, messageData.length);
        System.arraycopy(Constants.OKAY, 0, response, messageData.length, Constants.OKAY.length);

        return response;
    }
}



