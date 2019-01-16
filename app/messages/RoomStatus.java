package messages;

public class RoomStatus {
    private final int amount;
    private final String roomName;

    public RoomStatus(int amount, String roomName) {
        this.amount = amount;
        this.roomName = roomName;
    }

    public int getAmount() {
        return amount;
    }

    public String getRoomName() {
        return roomName;
    }
}
