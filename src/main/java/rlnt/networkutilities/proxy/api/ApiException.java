package rlnt.networkutilities.proxy.api;

public class ApiException extends java.lang.Exception {
    private static final long serialVersionUID = 1L;

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
