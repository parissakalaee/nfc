package com.parissakalaee.nfcapp;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NfcReaderActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {
    private static final String TAG = NfcReaderActivity.class.getSimpleName();

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_reader);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableReaderMode(this);
    }

    final boolean isVersion = true;

    @Override
    public void onTagDiscovered(Tag tag) {
        byte[] tagId = tag.getId();
        String tagIdString = NfcUtils.bytesToHexString(tagId);
        String tagTechnology = Arrays.toString(tag.getTechList());
        Log.d(TAG, "Tag discovered - ID: " + tagIdString + ", Technology: " + tagTechnology);
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                readFromMifareClassicTag(tag);
                break;
            } else if (tech.equals(IsoDep.class.getName())) {
                if (isVersion) {
                    readVersion(tag);
                } else {
                    readMessage(tag);
                }
                break;
            }
        }
    }

    public void selectAid(IsoDep isoDep) {
        try {
            isoDep.connect();
            byte[] selectCommand = NfcUtils.selectAid(Constants.AID);
            byte[] response = isoDep.transceive(selectCommand);
            String responseByte = NfcUtils.bytesToHexString(response);
            Log.d(TAG, "Response from SELECT command: " + responseByte);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                isoDep.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readFromTag(Tag tag, byte[] command) {
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            return;
        }
        try {
            isoDep.connect();
            byte[] response = isoDep.transceive(NfcUtils.selectAid(Constants.AID));
            String responseHex = NfcUtils.bytesToHexString(response);
            Log.d(TAG, "Response 1 from tag: " + responseHex);
            if (Arrays.equals(response, Constants.OKAY)) {
                byte[] messageResponse = isoDep.transceive(command);
                String messageResponseHex = NfcUtils.bytesToHexString(messageResponse);
                handleMessageResponse(messageResponseHex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                isoDep.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readVersion(Tag tag) {
        readFromTag(tag, Constants.GET_VERSION_COMMAND);
    }

    public void readMessage(Tag tag) {
        readFromTag(tag, Constants.READ_MESSAGE_COMMAND);
    }

    void handleMessageResponse(String message) {
        int okayIndex = message.lastIndexOf(NfcUtils.bytesToHexString(Constants.OKAY));
        if (okayIndex >= 0)
            message = message.substring(0, okayIndex);
        Log.d(TAG, "Response 2 from tag: " + message);
        String finalMessageResponseHex = message;
        new Handler(Looper.getMainLooper()).post(
                () -> Toast.makeText(getApplicationContext(), "Version from tag: " + finalMessageResponseHex, Toast.LENGTH_LONG).show());
    }

    public void readFromMifareClassicTag(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        if (mfc == null) {
            Log.e(TAG, "Tag is not MIFARE Classic");
            return;
        }

        try {
            mfc.connect();
            boolean auth = mfc.authenticateSectorWithKeyA(Constants.SECTOR_INDEX, Constants.KEY_DEFAULT);
            if (auth) {
                byte[] data = mfc.readBlock(Constants.BLOCK_INDEX);
                String message = new String(data, StandardCharsets.UTF_8);
                Log.d(TAG, "Message read from tag: " + message);
            } else {
                Log.e(TAG, "Authentication failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                mfc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

