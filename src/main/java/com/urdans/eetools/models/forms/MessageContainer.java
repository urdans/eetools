package com.urdans.eetools.models.forms;

public class MessageContainer {
    public String message;
    public boolean isError = false; //no error code
//    public int id = 0; //any int meaningful value

    public MessageContainer(String message, boolean isError) {
        this.message = message;
        this.isError = isError;
    }

    public MessageContainer() {
    }
}
