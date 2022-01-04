package com.jeeps.gamecollector.utils;

import java.util.Locale;

public class FormatUtils {
    public static String formatDecimal(double decimal) {
        if (decimal % 1.0 != 0)
            return String.format(Locale.US,"%s", decimal);
        else
            return String.format(Locale.US,"%.0f", decimal);
    }
}
