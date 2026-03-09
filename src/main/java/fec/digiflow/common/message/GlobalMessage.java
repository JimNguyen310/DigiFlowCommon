package fec.digiflow.common.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum GlobalMessage implements IMessage {
    EMPTY("0", "0"),
    SUCCESS("000000", "Success."),
    BAD_REQUEST("000001", "Bad request."),
    SQL_HAD_ERROR("000002", "Sql exception."),
    UNAUTHORIZED("000005", "Unauthorized."),
    INTERNAL_SERVER_ERROR("999999", "Internal server error."),
    HAD_ERROR_THIRD_PARTY("000006", "Had error third party."),
    ACCESS_DENIED("000007", "Access Denied."),
    NOT_FOUND("000008", "Not found."),
    PAYLOAD_TOO_LARGE("000009", "Payload too large."),
    SERVICE_UNAVAILABLE("888888", "Service Unavailable.");

    private static final Map<String, GlobalMessage> CODE_TO_MESSAGE_MAP =
            Arrays.stream(GlobalMessage.values())
                    .collect(Collectors.toMap(GlobalMessage::getCode, Function.identity()));

    private final String code;
    private final String message;

    /**
     * Finds an ApplicationMessage by its code.
     * This implementation uses a pre-computed map for O(1) lookup time.
     *
     * @param code The code to look for (e.g., "000000").
     * @return The corresponding ApplicationMessage, or EMPTY if not found.
     */
    public static GlobalMessage from(String code) {
        return CODE_TO_MESSAGE_MAP.getOrDefault(code, EMPTY);
    }

}
