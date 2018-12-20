package messages;

public class GetRooms {
    private final long userId;
    private final long requestId;
    public GetRooms(long userId, long requestId) {
        this.userId = userId;
        this.requestId = requestId;
    }

    public long getUserId() {
        return userId;
    }

    public long getRequestId() {
        return requestId;
    }
}
