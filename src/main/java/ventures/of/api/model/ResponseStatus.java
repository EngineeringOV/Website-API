package ventures.of.api.model;

public enum ResponseStatus {

    SUCCESS,
    BAD_REQUEST,
    ERROR;

    public static final ResponseStatus DEFAULT_VAL = ResponseStatus.SUCCESS;
}
