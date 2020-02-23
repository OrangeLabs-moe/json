package moe.orangelabs.json;

public interface JsonSerializable {

    /**
     * Serialize object. Allowed to return null
     */
    Json serialize();

}
