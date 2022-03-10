package com.jeeps.gamecollector.utils;

import com.jeeps.gamecollector.R;

public enum PlatformColors {
    NORMIE_WHITE("#ffffff", R.id.color_switch_normiewhite),
    SWITCH_RED("#E60012", R.id.color_switchred),
    XBOX_GREEN("#107C10", R.id.color_xboxgreen),
    PLAYSTATION_BLUE("#0070D1", R.id.color_playstationblue);

    private String color;
    private int colorId;

    PlatformColors(String color, int colorId) {
        this.color = color;
        this.colorId = colorId;
    }

    public String getColor() {
        return color;
    }

    public int getColorId() {
        return colorId;
    }
}
