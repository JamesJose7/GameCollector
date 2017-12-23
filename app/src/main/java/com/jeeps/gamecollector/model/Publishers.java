package com.jeeps.gamecollector.model;

import java.util.List;

/**
 * Created by jeeps on 12/23/2017.
 */

public class Publishers {
    private List<String> mPublishersList;

    public Publishers(List<String> publishersList) {
        mPublishersList = publishersList;
    }

    public List<String> getPublishersList() {
        return mPublishersList;
    }

    public void setPublishersList(List<String> publishersList) {
        mPublishersList = publishersList;
    }
}
