package com.jeeps.gamecollector.model;

import java.io.Serializable;

public class ToggleCompletionResponse implements Serializable {
    private boolean completed;

    public ToggleCompletionResponse(boolean completed) {
        this.completed = completed;
    }

    public ToggleCompletionResponse() { }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
