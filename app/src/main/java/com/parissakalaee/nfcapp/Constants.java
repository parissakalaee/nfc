package com.parissakalaee.nfcapp;

public class Constants {

    public static final String AID = "F0010203040506";
    public static final byte[] VERSION_NUMBER = {(byte) 0x50, (byte) 0x00};
    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static final byte[] OKAY = new byte[]{(byte) 0x90, (byte) 0x00};
    public static final byte[] UNKNOWN_CMD = new byte[]{(byte) 0x6D, (byte) 0x00};

    public static final byte[] KEY_DEFAULT = new byte[]{(byte) 0x70, (byte) 0x29, (byte) 0x94, (byte) 0x02, (byte) 0x06, (byte) 0x58};
    public static final int SECTOR_INDEX = 1;
    public static final int BLOCK_INDEX = 4;

    // SELECT AID command
    public static final byte[] SELECT_AID_COMMAND = new byte[]{
            (byte) 0x00, // CLA
            (byte) 0xA4, // INS
            (byte) 0x04, // P1
            (byte) 0x00, // P2
            (byte) 0x00  // LC (will be updated later)
    };

    // READ MESSAGE command
    public static final byte[] READ_MESSAGE_COMMAND = {
            (byte) 0x00, // CLA
            (byte) 0xB0, // INS
            (byte) 0x00, // P1
            (byte) 0x00, // P2
            (byte) 0x00  // Le
    };

    // GET VERSION command
    public static final byte[] GET_VERSION_COMMAND = {
            (byte) 0x80, // CLA
            (byte) 0x00, // INS
            (byte) 0x00, // P1
            (byte) 0x00, // P2
            (byte) 0x00  // LE
    };

    // READ BINARY command
    public static final byte[] READ_BINARY_COMMAND = {
            (byte) 0x00, // CLA
            (byte) 0xB0, // INS
            (byte) 0x00, // P1 (MSB of offset)
            (byte) 0x00, // P2 (LSB of offset)
            (byte) 0x0F  // LE (length of data to read)
    };
}
