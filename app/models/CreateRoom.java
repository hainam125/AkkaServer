package models;

public class CreateRoom {
    private String name;
    private long objectId;

    public CreateRoom() {}

    public CreateRoom(String name, long objectId) {
        this.name = name;
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public long getObjectId() {
        return objectId;
    }
}
