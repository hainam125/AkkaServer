package messages;

public class NewRoom {
    private final String roomName;
    private final long userId;
    private final long requestId;
    private final int playerAmount;
    public NewRoom(String roomName, long userId, long requestId, int playerAmount) {
        this.roomName = roomName;
        this.userId = userId;
        this.requestId = requestId;
        this.playerAmount = playerAmount;
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

    public int getPlayerAmount() {
        return playerAmount;
    }
}
