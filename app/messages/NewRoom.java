package messages;

public class NewRoom {
    private final String roomName;
    private final long userId;
    private final long requestId;
    public NewRoom(String roomName, long userId, long requestId) {
        this.roomName = roomName;
        this.userId = userId;
        this.requestId = requestId;
    }

    public String getRoomName() {
        return roomName;
    }

    public long getUserId() {
        return userId;
    }

    public long getRequestId() {
        return requestId;
    }
}
