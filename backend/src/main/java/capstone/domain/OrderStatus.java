package capstone.domain;

public enum OrderStatus {
    CANCELLED(0),
    BEFORE_RECEIVE(1),
    RESERVED(2),
    COMPLETED(3);

    private final int code;
    OrderStatus(int code) { this.code = code; }

    public int getCode() {
        return code;
    }

    public static OrderStatus fromCode(Integer dbData) {
        if (dbData == null) return null;
        for (OrderStatus s : values()) {
            if (s.code == dbData) return s;
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + dbData);
    }
}