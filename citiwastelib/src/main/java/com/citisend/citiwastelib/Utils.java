package com.citisend.citiwastelib;

import java.math.BigInteger;
import java.util.Locale;

public class Utils {
    public static String HexToBinary(String Hex) {
        String bin = new BigInteger(Hex, 16).toString(2);
        int inb = Integer.parseInt(bin);
        bin = String.format(Locale.getDefault(), "%08d", inb);
        return bin;
    }
}
