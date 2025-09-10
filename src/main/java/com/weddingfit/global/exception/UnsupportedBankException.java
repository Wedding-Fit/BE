package com.weddingfit.global.exception;

public class UnsupportedBankException extends RuntimeException {
    public UnsupportedBankException(String bankName) {
        super("지원하지 않는 은행입니다: " + bankName);
    }
}