package com.example.nfcapp;

import static com.example.nfcapp.MyHostApduService.bytesToHex;
import static com.example.nfcapp.MyHostApduService.concatenate;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MyNfcActivity extends Activity implements NfcAdapter.ReaderCallback {
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_nfc_activity_layout);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device.", Toast.LENGTH_LONG).show();
            finish();
            return;
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

    @Override
    public void onTagDiscovered(Tag tag) {
        // Handle tag discovery here
        byte[] tagId = tag.getId();
        String tagIdString = bytesToHex(tagId);
        String tagTechnology = Arrays.toString(tag.getTechList());
        Log.d("MyNfcActivity", "Tag discovered - ID: " + tagIdString + ", Technology: " + tagTechnology);
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                readFromMifareClassicTag(tag);
                break;
            }else if(tech.equals(IsoDep.class.getName())) {
                readFromTag(tag);
                break;
            }
        }
    }

    public void readFromTag(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep != null) {
            try {
                isoDep.connect();
// Get the AID bytes from the XML configuration
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
                byte[]  SELECT_AID_COMMAND_1 = concatenate(SELECT_AID_COMMAND, aidBytes);

                byte[] READ_MESSAGE_COMMAND = {
                        (byte) 0x00, // CLA
                        (byte) 0xB0, // INS
                        (byte) 0x00, // P1
                        (byte) 0x00, // P2
                        (byte) 0x00  // Le
                };

                byte[] GET_VERSION_COMMAND = {
                        (byte) 0x80, // CLA
                        (byte) 0x00, // INS
                        (byte) 0x00, // P1
                        (byte) 0x00, // P2
                        (byte) 0x00  // LE
                };
                byte[] READ_BINARY_COMMAND = {
                        (byte) 0x00, // CLA
                        (byte) 0xB0, // INS
                        (byte) 0x00, // P1 (MSB of offset)
                        (byte) 0x00, // P2 (LSB of offset)
                        (byte) 0x0F  // LE (length of data to read)
                };
                byte[] response = isoDep.transceive(SELECT_AID_COMMAND_1);
                String responseByte = bytesToHex(response);
                if (response != null && response.length >= 2 && response[0] == (byte) 0x90 && response[1] == (byte) 0x00) {
                    // Successful select command
                    byte[] messageResponse = isoDep.transceive(READ_MESSAGE_COMMAND);
                    String messageResponseByte = bytesToHex(response);
                    if (messageResponse != null && messageResponse.length > 0) {
                        String message = new String(messageResponse, StandardCharsets.UTF_8);
                        Log.d("MyNfcActivity", "Message read from tag: " + message);
                    }
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
    }

    private static final byte[] KEY_DEFAULT = new byte[]{(byte) 0x70, (byte) 0x29, (byte) 0x94, (byte) 0x02, (byte) 0x06, (byte) 0x58};
    private static final int SECTOR_INDEX = 1;
    private static final int BLOCK_INDEX = 4;

    public void readFromMifareClassicTag(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        if (mfc == null) {
            Log.e("MyNfcActivity", "Tag is not MIFARE Classic");
            return;
        }

        try {
            mfc.connect();
            boolean auth = mfc.authenticateSectorWithKeyA(SECTOR_INDEX, KEY_DEFAULT);
            if (auth) {
                byte[] data = mfc.readBlock(BLOCK_INDEX);
                String message = new String(data, StandardCharsets.UTF_8);
                Log.d("MyNfcActivity", "Message read from tag: " + message);
            } else {
                Log.e("MyNfcActivity", "Authentication failed");
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

    // Helper method to convert a hex string to byte array
    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}

