package data;

import network.object.SnapShot;

public class EnterRoom {
    private String name;
    private long objectId;
    private String snapShot;

    public EnterRoom() {}

    public EnterRoom(String name, long objectId, String snapShot) {
        this.name = name;
        this.objectId = objectId;
        this.snapShot = snapShot;
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
}
