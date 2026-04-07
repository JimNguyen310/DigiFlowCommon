package fec.digiflow.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fec.digiflow.common.exception.BaseException;
import fec.digiflow.common.message.GlobalMessage;
import fec.digiflow.common.message.IMessage;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class BaseResponse<TData> {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private TData data;

    @JsonProperty("detail")
    private String detailError;

    public BaseResponse() {
    }

    public BaseResponse(String code, String message, TData data, String detailError) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.detailError = detailError;
    }

    // region Getters & Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TData getData() {
        return data;
    }

    public void setData(TData data) {
        this.data = data;
    }

    public String getDetailError() {
        return detailError;
    }

    public void setDetailError(String detailError) {
        this.detailError = detailError;
    }
    // endregion

    public boolean hasError() {
        return !GlobalMessage.SUCCESS.getCode().equalsIgnoreCase(this.code);
    }

    public static <TData> BaseResponse<TData> success(TData data) {
        return BaseResponse.<TData>builder()
                .code(GlobalMessage.SUCCESS.getCode())
                .message(GlobalMessage.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    public static <TData> BaseResponse<TData> failure(IMessage message, String... detail) {
        String detailMsg = (detail != null && detail.length > 0) ? detail[0] : null;
        return BaseResponse.<TData>builder()
                .code(message.getCode())
                .message(message.getMessage())
                .detailError(detailMsg)
                .build();
    }

    public static <TData> BaseResponse<TData> failure(BaseException exception) {
        return BaseResponse.<TData>builder()
                .code(exception.getCode())
                .message(exception.getMessage())
                .detailError(exception.getDetail())
                .build();
    }

    public static <TData> Builder<TData> builder() {
        return new Builder<>();
    }

    public static class Builder<TData> {
        private String code;
        private String message;
        private TData data;
        private String detailError;

        public Builder<TData> code(String code) {
            this.code = code;
            return this;
        }

        public Builder<TData> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<TData> data(TData data) {
            this.data = data;
            return this;
        }

        public Builder<TData> detailError(String detailError) {
            this.detailError = detailError;
            return this;
        }

        public BaseResponse<TData> build() {
            return new BaseResponse<>(code, message, data, detailError);
        }
    }
}
