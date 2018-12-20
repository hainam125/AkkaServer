package models;

public class CreateRoom {
    private String name;
    private long objectId;
    private boolean success;

    public CreateRoom() {}

    public CreateRoom(String name, long objectId, boolean success) {
        this.name = name;
        this.objectId = objectId;
        this.success = success;
    }

    public String getName() {
        return name;
    }

    public long getObjectId() {
        return objectId;
    }

    public boolean isSuccess() {
        return success;
    }
}
