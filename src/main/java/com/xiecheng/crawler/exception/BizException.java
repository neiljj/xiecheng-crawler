package com.xiecheng.crawler.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {


    private final int code;
    private boolean needAlert = true;

    public BizException(String msg) {
        super(msg);
        this.code = 500;
    }

    public BizException(String msg, boolean needAlert) {
        super(msg);
        this.code = 500;
        this.needAlert = needAlert;
    }

    public BizException(String msg, Throwable cause) {
        super(msg, cause);
        this.code = 500;
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BizException(Throwable cause) {
        super(cause);
        this.code = 500;
    }
}
