package fec.digiflow.common.utils;

import fec.digiflow.common.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Slf4j
public abstract class BaseRestClient {

    private static final String API_KEY_HEADER = "x-api-key";
    private static final String API_USER_HEADER = "x-user-key";
    private static final int DEFAULT_TIMEOUT_MS = 60000;

    protected final RestClient restClient;

    // =============================================================================================
    // Constructors
    // =============================================================================================
    protected BaseRestClient(RestClient.Builder restClientBuilder, String baseUrl, int... timeout) {
        this(restClientBuilder, baseUrl, timeout.length > 0 ? timeout[0] : DEFAULT_TIMEOUT_MS, (ClientHttpRequestInterceptor) null);
    }

    protected BaseRestClient(RestClient.Builder restClientBuilder, String baseUrl, String securityKey, int... timeout) {
        this(restClientBuilder, baseUrl, timeout.length > 0 ? timeout[0] : DEFAULT_TIMEOUT_MS, createSecurityInterceptor(securityKey));
    }

    protected BaseRestClient(RestClient.Builder restClientBuilder, String baseUrl, String username, String password, int... timeout) {
        this(restClientBuilder, baseUrl, timeout.length > 0 ? timeout[0] : DEFAULT_TIMEOUT_MS, createBasicAuthInterceptor(username, password));
    }

    protected BaseRestClient(RestClient.Builder restClientBuilder, String baseUrl, int timeout, ClientHttpRequestInterceptor authFilter) {

        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeout))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(timeout));

        RestClient.Builder builder = restClientBuilder.clone()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (authFilter != null) {
            builder.requestInterceptor(authFilter);
        }

        this.restClient = builder.build();
    }

    // =============================================================================================
    // Security & Interceptor Logic
    // =============================================================================================

    private static ClientHttpRequestInterceptor createSecurityInterceptor(String securityKey) {
        if (securityKey == null || securityKey.isEmpty()) {
            throw new IllegalArgumentException("Service key must not be null or empty.");
        }
        return (request, body, execution) -> {
            request.getHeaders().add(API_KEY_HEADER, securityKey);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                addUserHeader(request.getHeaders(), auth);
            }
            return execution.execute(request, body);
        };
    }

    private static ClientHttpRequestInterceptor createBasicAuthInterceptor(String username, String password) {
        return (request, body, execution) -> {
            request.getHeaders().setBasicAuth(username, password);
            return execution.execute(request, body);
        };
    }

    private static void addUserHeader(HttpHeaders headers, Authentication auth) {
        try {
            Object principal = auth.getPrincipal();
            String userJson = JacksonUtils.toJson(principal);
            if (userJson != null) {
                String headerValue = Base64.getEncoder().encodeToString(userJson.getBytes(StandardCharsets.UTF_8));
                headers.add(API_USER_HEADER, headerValue);
            }
        } catch (Exception e) {
            log.error("Failed to add user header", e);
        }
    }

    // =============================================================================================
    // Core Execution Method
    // =============================================================================================

    protected <T, R> R execute(HttpMethod method, String path, T requestBody, HttpHeaders headers, ParameterizedTypeReference<R> responseType) {
        long startTime = System.currentTimeMillis();

        try {
            RestClient.RequestBodySpec requestSpec = restClient.method(method).uri(path);

            if (headers != null) {
                requestSpec.headers(h -> h.addAll(headers));
            }

            if (requestBody != null) {
                requestSpec.body(requestBody);
            }

            R response = requestSpec.retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        throw new RestClientResponseException(
                                "API call failed: " + clientResponse.getStatusCode(),
                                clientResponse.getStatusCode().value(),
                                clientResponse.getStatusText(),
                                clientResponse.getHeaders(),
                                clientResponse.getBody().readAllBytes(),
                                StandardCharsets.UTF_8
                        );
                    })
                    .body(responseType);

            logSuccess(method, path, requestBody, response, headers, System.currentTimeMillis() - startTime);
            return response;

        } catch (Exception e) {
            logError(method, path, requestBody, e, headers);
            throw e;
        }
    }

    // =============================================================================================
    // HTTP Shortcuts
    // =============================================================================================

    protected <T> BaseResponse<T> getBase(String path, ParameterizedTypeReference<T> typeReference, HttpHeaders... headers) {
        return executeBase(HttpMethod.GET, path, null, typeReference, headers);
    }

    protected <T> BaseResponse<T> postBase(String path, Object body, ParameterizedTypeReference<T> typeReference, HttpHeaders... headers) {
        return executeBase(HttpMethod.POST, path, body, typeReference, headers);
    }

    protected <T> BaseResponse<T> postBase(String path, MultiValueMap<String, Object> body, ParameterizedTypeReference<T> typeReference, HttpHeaders... headers) {
        HttpHeaders finalHeaders = (headers != null && headers.length > 0) ? headers[0] : new HttpHeaders();
        finalHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        return executeBase(HttpMethod.POST, path, body, typeReference, finalHeaders);
    }

    protected <T> BaseResponse<T> putBase(String path, Object body, ParameterizedTypeReference<T> typeReference, HttpHeaders... headers) {
        return executeBase(HttpMethod.PUT, path, body, typeReference, headers);
    }

    protected <T> BaseResponse<T> deleteBase(String path, ParameterizedTypeReference<T> typeReference, HttpHeaders... headers) {
        return executeBase(HttpMethod.DELETE, path, null, typeReference, headers);
    }

    protected <T> BaseResponse<T> executeBase(HttpMethod method, String path, Object body, ParameterizedTypeReference<T> typeReference, HttpHeaders... headers) {
        HttpHeaders finalHeaders = (headers != null && headers.length > 0) ? headers[0] : null;
        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(
                BaseResponse.class,
                ResolvableType.forType(typeReference.getType())
        );
        ParameterizedTypeReference<BaseResponse<T>> typeRef = ParameterizedTypeReference.forType(resolvableType.getType());
        return execute(method, path, body, finalHeaders, typeRef);
    }

    protected <R> R get(String path, ParameterizedTypeReference<R> responseType, HttpHeaders... headers) {
        return execute(HttpMethod.GET, path, null, (headers != null && headers.length > 0) ? headers[0] : null, responseType);
    }

    protected <R> R post(String path, Object body, ParameterizedTypeReference<R> responseType, HttpHeaders... headers) {
        return execute(HttpMethod.POST, path, body, (headers != null && headers.length > 0) ? headers[0] : null, responseType);
    }

    protected <R> R put(String path, Object body, ParameterizedTypeReference<R> responseType, HttpHeaders... headers) {
        return execute(HttpMethod.PUT, path, body, (headers != null && headers.length > 0) ? headers[0] : null, responseType);
    }

    protected <R> R delete(String path, ParameterizedTypeReference<R> responseType, HttpHeaders... headers) {
        return execute(HttpMethod.DELETE, path, null, (headers != null && headers.length > 0) ? headers[0] : null, responseType);
    }

    // =============================================================================================
    // Internal Helpers
    // =============================================================================================

    private String toJson(Object o) {
        try {
            return JacksonUtils.toJson(o);
        } catch (Exception e) {
            return "error";
        }
    }

    private void logSuccess(HttpMethod m, String p, Object b, Object res, HttpHeaders h, long d) {
        log.info("[OUTBOUND_RESPONSE]: {} {} ({} ms) | Body: {}", m, p, d, toJson(res));
    }

    private void logError(HttpMethod m, String p, Object b, Throwable e, HttpHeaders h) {
        log.error("[OUTBOUND_ERROR]: {} {} | Error: {}", m, p, e.getMessage());
    }
}
