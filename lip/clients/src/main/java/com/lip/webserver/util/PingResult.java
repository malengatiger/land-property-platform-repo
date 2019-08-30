package com.lip.webserver.util;

public  class PingResult {
    String message;
    String nodeInfo;

    public PingResult(String message, String nodeInfo) {
        this.message = message;
        this.nodeInfo = nodeInfo;
    }
}
