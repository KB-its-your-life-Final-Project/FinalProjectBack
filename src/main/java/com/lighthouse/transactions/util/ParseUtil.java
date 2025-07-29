package com.lighthouse.transactions.util;

public class ParseUtil {
    public static int safeParseInt(String value) {
        try {
            return Integer.parseInt(value.replaceAll(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
