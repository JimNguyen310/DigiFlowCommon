package fec.digiflow.common.exception;

public class JsonOperationException extends RuntimeException {
    public JsonOperationException(String message) {
        super(message);
    }

    public JsonOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
