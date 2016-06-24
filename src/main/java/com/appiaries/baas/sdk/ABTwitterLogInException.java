package com.appiaries.baas.sdk;

class ABTwitterLogInException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int errorCode;
    private final String description;
    private final String failingUrl;

    public ABTwitterLogInException(int errorCode, String description, String failingUrl) {
        super(String.format("Twitter Log-in Error [code=%d] [url=%s] [reason:%s]",
                new Object[]{errorCode, failingUrl, description}));
        this.errorCode = errorCode;
        this.description = description;
        this.failingUrl = failingUrl;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getDescription() {
        return this.description;
    }

    public String getFailingUrl() {
        return this.failingUrl;
    }

}
