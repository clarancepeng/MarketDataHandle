package org.alige.data.md.utils;

public class Utils {
    public static int guessDate(String fileName) {
        int place = fileName.indexOf('.');
        if (place > 8) {
            String dateStr = fileName.substring(place - 8, place);
            return Integer.parseInt(dateStr);
        }
        return 0;
    }

    public static int toInt(String data) {
        try {
            return Integer.parseInt(data);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long toLong(String data) {
        try {
            return Long.parseLong(data);
        } catch (Exception e) {
            return 0;
        }
    }

    public static double toDouble(String data) {
        try {
            return Double.parseDouble(data);
        } catch (Exception e) {
            return 0;
        }
    }
}
