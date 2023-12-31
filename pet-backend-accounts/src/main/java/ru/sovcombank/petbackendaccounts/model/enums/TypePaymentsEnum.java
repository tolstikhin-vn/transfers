package ru.sovcombank.petbackendaccounts.model.enums;

public enum TypePaymentsEnum {

    REPLENISHMENT("REPLENISHMENT"),
    DEBITING("DEBITING");

    private final String typePayment;

    private TypePaymentsEnum(String typePayment) {
        this.typePayment = typePayment;
    }

    public String getTypePayment() {
        return typePayment;
    }
}
