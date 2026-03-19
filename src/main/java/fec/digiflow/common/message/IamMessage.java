package fec.digiflow.common.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IamMessage implements IMessage {

    USER_NOT_FOUND ("100001", "User not found"),
    USER_REFRESH_TOKEN_NOT_FOUND ("100002", "User refresh token not found"),
    REFRESH_TOKEN_EXPIRED("100003", "Refresh token expired. Please make a new request"),
    PASSWORD_NOT_MATCH("100004", "Password not match");
    ;

    private final String code;
    private final String message;

}
