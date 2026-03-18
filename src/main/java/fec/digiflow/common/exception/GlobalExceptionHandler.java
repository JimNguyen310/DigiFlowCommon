package fec.digiflow.common.exception;

import fec.digiflow.common.dto.BaseResponse;
import fec.digiflow.common.message.GlobalMessage;
import fec.digiflow.common.utils.JacksonUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class GlobalExceptionHandler {

    protected static final String FORMAT_LOG_ERROR = "[INBOUND_ERROR]: {} | Message: {}";
    protected static final String FORMAT_LOG_WARN = "[INBOUND_ERROR]: {} | Location: {} | Code: {} | Message: {}";

    // --- Business Logic Exceptions ---

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Object>> handleBusinessException(BaseException error, ServletRequest request) {
        logWarning(request, error.getCode(), error.getMessage(), error);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, error.getCode(), error.getMessage(), error.getDetail());
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<BaseResponse<Object>> handleApplicationException(ApplicationException error, ServletRequest request) {
        logWarning(request, error.getCode(), error.getMessage(), error);
        // ApplicationException usually implies a business rule violation -> 400 Bad Request
        return buildResponse(HttpStatus.BAD_REQUEST, error.getCode(), error.getMessage(), error.getDetail());
    }
    // --- Validation & Binding Exceptions ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<BaseResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex, ServletRequest request) {
        return handleBindingErrors(request, ex.getBindingResult());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<BaseResponse<Object>> handleBindException(BindException ex, ServletRequest request) {
        return handleBindingErrors(request, ex.getBindingResult());
    }

    // --- Request Handling Exceptions ---

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<BaseResponse<Object>> handleMissingParams(MissingServletRequestParameterException ex, ServletRequest request) {
        String message = "Missing required parameter: " + ex.getParameterName();
        logSimpleWarning(request, message);
        return buildResponse(HttpStatus.BAD_REQUEST, GlobalMessage.BAD_REQUEST.getCode(), message, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<BaseResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, ServletRequest request) {
        String message = String.format("Parameter '%s' should be of type '%s'", ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        logSimpleWarning(request, message);
        return buildResponse(HttpStatus.BAD_REQUEST, GlobalMessage.BAD_REQUEST.getCode(), message, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<BaseResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, ServletRequest request) {
        logSimpleWarning(request, "Malformed JSON request: " + ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, GlobalMessage.BAD_REQUEST.getCode(), "Malformed JSON request or invalid data format", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<BaseResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, ServletRequest request) {
        logSimpleWarning(request, "Method Not Allowed: " + ex.getMethod());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "405", "Method Not Allowed", null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseEntity<BaseResponse<Object>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, ServletRequest request) {
        logSimpleWarning(request, "Unsupported Media Type: " + ex.getContentType());
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "415", "Unsupported Media Type", null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<BaseResponse<Object>> handleNoHandlerFound(NoHandlerFoundException ex, ServletRequest request) {
        logSimpleWarning(request, "Not Found");
        return buildResponse(HttpStatus.NOT_FOUND, GlobalMessage.NOT_FOUND.getCode(), GlobalMessage.NOT_FOUND.getMessage(), null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<BaseResponse<Object>> handleMaxSizeException(MaxUploadSizeExceededException ex, ServletRequest request) {
        logSimpleWarning(request, "File size exceeds limit");
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, GlobalMessage.PAYLOAD_TOO_LARGE.getCode(), GlobalMessage.PAYLOAD_TOO_LARGE.getMessage(), null);
    }

    // --- System & Generic Exceptions ---

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<BaseResponse<Object>> handleIOException(IOException error, ServletRequest request) {
        logError(request, error.getMessage(), error);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, GlobalMessage.INTERNAL_SERVER_ERROR.getCode(), GlobalMessage.INTERNAL_SERVER_ERROR.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<BaseResponse<Object>> handleIllegalArgumentError(IllegalArgumentException error, ServletRequest request) {
        logSimpleWarning(request, error.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, GlobalMessage.BAD_REQUEST.getCode(), GlobalMessage.BAD_REQUEST.getMessage(), null);
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<BaseResponse<Object>> handleNullPointerError(NullPointerException error, ServletRequest request) {
        logError(request, "NullPointerException detected", error);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, GlobalMessage.INTERNAL_SERVER_ERROR.getCode(), GlobalMessage.INTERNAL_SERVER_ERROR.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<BaseResponse<Object>> handleGenericError(Exception error, ServletRequest request) {
        logError(request, "Unhandled Exception", error);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, GlobalMessage.INTERNAL_SERVER_ERROR.getCode(), GlobalMessage.INTERNAL_SERVER_ERROR.getMessage(), null);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<BaseResponse<Object>> handleThrowableError(Throwable error, ServletRequest request) {
        logError(request, "Critical Error", error);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, GlobalMessage.INTERNAL_SERVER_ERROR.getCode(), GlobalMessage.INTERNAL_SERVER_ERROR.getMessage(), null);
    }

    @ExceptionHandler({RestClientException.class})
    public ResponseEntity<BaseResponse<Object>> handleRestClientError(RestClientException error, HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        log.error(FORMAT_LOG_ERROR, url, error.getMessage(), error);

        String responseBody = error.getResponseBody();
        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                BaseResponse<?> errorResponse = JacksonUtils.fromJson(responseBody, BaseResponse.class);
                if (errorResponse != null && errorResponse.getCode() != null) {
                    return buildResponse(error.getHttpStatusCode(), errorResponse.getCode(), errorResponse.getMessage(), errorResponse.getDetailError());
                }
            } catch (RuntimeException e) {
                log.debug("Failed to parse error body as BaseResponse: {}", e.getMessage());
            }
        }
        return buildResponse(error.getHttpStatusCode(), String.valueOf(error.getHttpStatusCode().value()), error.getMessage(), null);
    }

    // --- Helper Methods ---

    protected ResponseEntity<BaseResponse<Object>> handleBindingErrors(ServletRequest request, BindingResult bindingResult) {
        String url = getUrl(request);
        Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing));

        log.warn("[INBOUND_ERROR]: {} | Validation Failed: {}", url, errors);

        String detailErrorJson = JacksonUtils.toJson(errors); // Convert Map to JSON String
        return buildResponse(HttpStatus.BAD_REQUEST, GlobalMessage.BAD_REQUEST.getCode(), GlobalMessage.BAD_REQUEST.getMessage(), detailErrorJson);
    }

    protected ResponseEntity<BaseResponse<Object>> buildResponse(HttpStatusCode status, String code, String message, String detailError) {
        BaseResponse<Object> response = BaseResponse.failure(code, message);
        if (detailError != null) {
            response.setDetailError(detailError);
        }
        return ResponseEntity.status(status).body(response);
    }

    protected void logWarning(ServletRequest request, String code, String message, Throwable ex) {
        String url = getUrl(request);
        String errorLocation = getStackTraceLocation(ex);
        log.warn(FORMAT_LOG_WARN, url, errorLocation, code, message);
    }

    protected void logSimpleWarning(ServletRequest request, String message) {
        String url = getUrl(request);
        log.warn(FORMAT_LOG_ERROR, url, message);
    }

    protected void logError(ServletRequest request, String message, Throwable ex) {
        String url = getUrl(request);
        log.error(FORMAT_LOG_ERROR, url, message, ex);
    }

    protected String getUrl(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getRequestURL().toString();
        }
        return "Unknown URL";
    }

    protected String getStackTraceLocation(Throwable error) {
        if (error == null) return "Unknown Source";
        StackTraceElement[] stackTrace = error.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            StackTraceElement element = stackTrace[0];
            return String.format("%s.%s:%d", element.getClassName(), element.getMethodName(), element.getLineNumber());
        }
        return "Unknown Source";
    }
}
