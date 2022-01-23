package geekbrains.cloud;

public enum MessageType {
    TEXT(0), FILE(1), UNKNOWN(-1);

    public static final int MAX_FILENAME_LENGTH = 512;

    private int rawValue;

    MessageType(int rawValue) {
        this.rawValue = rawValue;
    }

    public int getRawValue() {return rawValue;};

    public static MessageType fromRawValue(int rawValue) {
        return switch (rawValue){
            case 0 -> TEXT;
            case 1 -> FILE;
            default -> UNKNOWN;
        };
    }
}
