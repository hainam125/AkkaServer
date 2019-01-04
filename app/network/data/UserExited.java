package network.data;

import models.User;

public class UserExited {
    private String username;
    private long id;
    private long objectId;

    public UserExited(User user, long objectId) {
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
