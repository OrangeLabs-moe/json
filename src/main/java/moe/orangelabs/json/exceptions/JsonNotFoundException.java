package moe.orangelabs.json.exceptions;

public class JsonNotFoundException extends RuntimeException {

    public JsonNotFoundException() {
        super();
    }

    public JsonNotFoundException(String message) {
        super(message);
    }

    public JsonNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonNotFoundException(Throwable cause) {
        super(cause);
    }
}
