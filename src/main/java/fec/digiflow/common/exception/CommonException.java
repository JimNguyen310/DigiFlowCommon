package fec.digiflow.common.exception;

import lombok.Getter;

@Getter
public class CommonException extends BaseException {

    public CommonException(String code, String message, String detail) {
        super(message, code, detail);
    }
}
