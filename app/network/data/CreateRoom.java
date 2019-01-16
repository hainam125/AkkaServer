package network.data;

public class CreateRoom {
    private String name;
    private int nb;
    private long objectId;
    private String snapShot;
    private boolean success;

    public CreateRoom() {}

    public CreateRoom(String name, long objectId, String snapShot, boolean success) {
        this.name = name;
        this.objectId = objectId;
        this.snapShot = snapShot;
        this.success = success;
    }

    public String getName() {
        return name;
    }

    public long getObjectId() {
        return objectId;
    }

    public String getSnapShot() {
        return snapShot;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getNb() {
        return nb;
    }
}
