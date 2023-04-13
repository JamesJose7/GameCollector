package com.jeeps.gamecollector.deprecated.utils;

import com.jeeps.gamecollector.R;

import java.util.Arrays;
import java.util.Optional;

public class PlatformCovers {
    enum Covers {
        SWITCH(R.drawable.switch_cover, "switch", "nintendo switch"),
        WII_U(R.drawable.wiiu_cover, "wii u", "wiiu"),
        N3DS(R.drawable.n3ds_cover, "3ds", "nintendo 3ds", "n3ds"),
        WII(R.drawable.wii_cover, "wii"),
        NDS(R.drawable.ds_cover, "nds", "ds", "nintendo ds"),
        PS4(R.drawable.ps4_cover, "ps4", "playstation 4", "ps 4"),
        XBOX1(R.drawable.xbox1_cover, "xbox one", "xbone", "xbox1", "xbox 1"),
        DEFAULT(R.drawable.default_cover, "default");

        private int cover;
        private String[] tags;

        Covers(int cover, String... tags) {
            this.cover = cover;
            this.tags = tags;
        }

        public int getCover() {
            return cover;
        }

        public String[] getTags() {
            return tags;
        }
    }


    public static int getPlatformCover(String platformName) {
        Optional<Covers> optionalCover = Arrays.stream(Covers.values())
                .filter(cover -> {
                    for (String tag : cover.getTags()) {
                        if (platformName.toLowerCase().trim().contains(tag))
                            return true;
                    }
                    return false;
                })
                .findFirst();

        return optionalCover.map(Covers::getCover)
                .orElseGet(Covers.DEFAULT::getCover);
    }
}
