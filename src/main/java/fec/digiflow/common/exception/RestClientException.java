package fec.digiflow.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class RestClientException extends RuntimeException {

    private final HttpStatusCode httpStatusCode;
    private final String responseBody;

    public RestClientException(String message, HttpStatusCode httpStatusCode, String responseBody) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
    }

    public RestClientException(String message, HttpStatusCode httpStatusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
    }

    @Override
    public String getMessage() {
        return String.format("%s - Status: %s, Response: %s", super.getMessage(), httpStatusCode, responseBody);
    }
}
