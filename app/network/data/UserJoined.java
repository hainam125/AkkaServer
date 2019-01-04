package network.data;

import models.User;

public class UserJoined {
    private String username;
    private long id;
    private long objectId;

    public UserJoined(User user, long objectId) {
        this.username = user.getUsername();
        this.id = user.getId();
        this.objectId = objectId;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public long getObjectId() {
        return objectId;
    }
}
