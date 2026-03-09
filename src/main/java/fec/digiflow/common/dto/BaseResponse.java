package fec.digiflow.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fec.digiflow.common.exception.BaseException;
import fec.digiflow.common.message.GlobalMessage;
import fec.digiflow.common.message.IMessage;
import lombok.*;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public final class BaseResponse<TData> {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private TData data;

    @JsonProperty("detail")
    private String detailError;

    /**
     * Checks if the response indicates an error.
     *
     * @return true if the response has an error, false otherwise.
     */
    public boolean hasError() {
        return !GlobalMessage.SUCCESS.getCode().equalsIgnoreCase(this.code);
    }

    /**
     * Creates a success response with data.
     *
     * @param data the response data.
     * @return a success BaseResponse instance.
     */
    public static <TData> BaseResponse<TData> success(TData data) {
        return BaseResponse.<TData>builder()
                .code(GlobalMessage.SUCCESS.getCode())
                .message(GlobalMessage.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    /**
     * Creates a failure response with an IMessage.
     *
     * @param message the application message.
     * @param detail optional detail error message
     * @return a failure BaseResponse instance.
     */
    public static <TData> BaseResponse<TData> failure(IMessage message, String... detail) {
        String detailMsg = (detail != null && detail.length > 0) ? detail[0] : null;
        return BaseResponse.<TData>builder()
                .code(message.getCode())
                .message(message.getMessage())
                .detailError(detailMsg)
                .build();
    }

    /**
     * Creates a failure response with a BaseException.
     *
     * @param exception the base exception.
     * @return a failure BaseResponse instance.
     */
    public static <TData> BaseResponse<TData> failure(BaseException exception) {
        return BaseResponse.<TData>builder()
                .code(exception.getCode())
                .message(exception.getMessage())
                .detailError(exception.getDetail())
                .build();
    }

    /**
     * Creates a failure response with code and message.
     *
     * @param code the error code.
     * @param message the error message.
     * @return a failure BaseResponse instance.
     */
    public static <TData> BaseResponse<TData> failure(String code, String message) {
        return BaseResponse.<TData>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * Creates a failure response with code, message and detail.
     *
     * @param code the error code.
     * @param message the error message.
     * @param detailError the detail error message.
     * @return a failure BaseResponse instance.
     */
    public static <TData> BaseResponse<TData> failure(String code, String message, String detailError) {
        return BaseResponse.<TData>builder()
                .code(code)
                .message(message)
                .detailError(detailError)
                .build();
    }
}
