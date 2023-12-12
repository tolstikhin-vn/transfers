package ru.sovcombank.petbackendtransfers.model.enums;

public enum RequestTypeEnum {

    ACCOUNT("ACCOUNT"),
    PHONE("PHONE");

    private final String requestType;

    private RequestTypeEnum(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestType() {
        return requestType;
    }
}
