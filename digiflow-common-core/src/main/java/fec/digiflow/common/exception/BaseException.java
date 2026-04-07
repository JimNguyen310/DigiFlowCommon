package fec.digiflow.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class BaseException extends RuntimeException {
    private final String code;

    @JsonIgnore
    private final String detail;

    protected BaseException(String message, String code, String detail) {
        super(message);
        this.code = code;
        this.detail = detail;
    }

    public String getCode() {
        return code;
    }

    public String getDetail() {
        return detail;
    }
}
