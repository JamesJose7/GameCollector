package com.jeeps.gamecollector.utils;

public enum Colors {
    NORMIE_WHITE("#ffffff"),
    SWITCH_RED("#E60012"),
    XBOX_GREEN("#107C10"),
    PLAYSTATION_BLUE("#0070D1");

    private String color;

    Colors(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
