package data;

import java.util.List;

public class EnterRoom {
    private String name;
    private long objectId;
    private String snapShot;
    private List<Long> ids;
    private List<String> usernames;

    public EnterRoom() {}

    public EnterRoom(String name, long objectId, String snapShot, List<Long> ids, List<String> usernames) {
        this.name = name;
        this.objectId = objectId;
        this.snapShot = snapShot;
        this.ids = ids;
        this.usernames = usernames;
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

    public List<Long> getIds() {
        return ids;
    }

    public List<String> getUsernames() {
        return usernames;
    }
}
