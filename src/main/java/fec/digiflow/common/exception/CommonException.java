package fec.digiflow.common.exception;

import fec.digiflow.common.message.IMessage;
import lombok.Getter;

@Getter
public class CommonException extends BaseException {

    public CommonException(IMessage iMessage) {
        super(iMessage.getMessage(), iMessage.getCode(), null);
    }
}
