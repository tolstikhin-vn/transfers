package ru.sovcombank.petbackendtransfers.model.enums;

public enum CurEnum {

    RUB("810"),
    BYN("933"),
    USD("840");
    private final String cur;

    private CurEnum(String cur) {
        this.cur = cur;
    }

    public String getCur() {
        return cur;
    }
}