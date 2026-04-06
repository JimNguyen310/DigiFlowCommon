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
}
