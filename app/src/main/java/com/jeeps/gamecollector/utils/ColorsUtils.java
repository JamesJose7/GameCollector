package com.jeeps.gamecollector.utils;

import android.content.Context;

import androidx.annotation.ColorInt;

import com.jeeps.gamecollector.R;

public class ColorsUtils {

    private static final int RANGE_20 = 8;
    private static final int RANGE_40 = 16;
    private static final int RANGE_60 = 30;
    private static final int RANGE_80 = 60;
    private static final int RANGE_100 = 80;

    @ColorInt
    public static int getColorByHoursRange(Context context, double hours) {
        if (hours <= RANGE_20)
            return context.getColor(R.color.hours_range_20);
        else if (hours < RANGE_40)
            return context.getColor(R.color.hours_range_40);
        else if (hours < RANGE_60)
            return context.getColor(R.color.hours_range_60);
        else if (hours < RANGE_80)
            return context.getColor(R.color.hours_range_80);
        else
            return context.getColor(R.color.hours_range_100);
    }
}
