package moe.orangelabs.json.exceptions;

public class JsonCastException extends RuntimeException {

    public JsonCastException() {
        super();
    }

    public JsonCastException(String message) {
        super(message);
    }

    public JsonCastException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonCastException(Throwable cause) {
        super(cause);
    }
}
