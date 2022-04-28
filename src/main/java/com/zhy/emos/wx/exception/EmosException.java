package com.zhy.emos.wx.exception;

import lombok.Data;

@Data
public class EmosException extends RuntimeException {
    private String msg;
    private int code = 500;

    public EmosException(String message, String msg) {
        super(message);
        this.msg = msg;
    }

    public EmosException(String msg) {
        this.msg = msg;
    }

    public EmosException(String message, Throwable cause, String msg) {
        super(message, cause);
        this.msg = msg;
    }
}
