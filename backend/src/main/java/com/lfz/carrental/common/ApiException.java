package com.lfz.carrental.common;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final int code;

    public ApiException(String message) {
        super(message);
        this.code = -1;
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }
}

