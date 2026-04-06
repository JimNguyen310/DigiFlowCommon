package fec.digiflow.common.exception;

import fec.digiflow.common.dto.BaseResponse;
import fec.digiflow.common.message.IMessage;
import lombok.Getter;

@Getter
public class ApplicationException extends BaseException {
    public ApplicationException(IMessage message) {
        super(message.getMessage(), message.getCode(), null);
    }

    public ApplicationException(BaseResponse<?> errorResponse) {
        super(errorResponse.getMessage(), errorResponse.getCode(), errorResponse.getDetailError());
    }

    public ApplicationException(String message, String code) {
        super(message, code, null);
    }

    public ApplicationException(IMessage message, String detail) {
        super(message.getMessage(), message.getCode(), detail);
    }
}
