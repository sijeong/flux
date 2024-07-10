package net.cfxp.api.util.http;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

/**
 * HttpErroInfo
 */
public class HttpErrorInfo {
    private final ZonedDateTime timestamp;
    private final String path;
    private final HttpStatus httpStatus;
    private final String message;

    public HttpErrorInfo() {
        timestamp = null;
        httpStatus = null;
        path = null;
        message = null;
    }

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
        timestamp = ZonedDateTime.now();
        this.path = path;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public HttpStatus getStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return httpStatus.getReasonPhrase();
    }
}