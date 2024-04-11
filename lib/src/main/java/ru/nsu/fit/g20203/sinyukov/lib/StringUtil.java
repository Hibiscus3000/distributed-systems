package ru.nsu.fit.g20203.sinyukov.lib;

public class StringUtil {

    public static String getPluralOrSingular(int count) {
        if (1 != count) {
            return "s";
        }
        return "";
    }

    public static String capitalizeFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
