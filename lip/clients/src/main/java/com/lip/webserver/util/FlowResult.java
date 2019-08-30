package com.lip.webserver.util;

public class FlowResult {
    String transactionID, message;

    public FlowResult(String transactionID, String message) {
        this.transactionID = transactionID;
        this.message = message;
    }
}
