package messages;

import models.UserRef;

public class JoinRoom {
    private final String room;
    private final UserRef userRef;
    private final long requestId;
    private final boolean isCreated;
    public JoinRoom(UserRef userRef, String room, long requestId, boolean isCreated) {
        this.userRef = userRef;
        this.room = room;
        this.requestId = requestId;
        this.isCreated = isCreated;
    }

    public String getRoom() {
        return room;
    }

    public UserRef getUserRef() {
        return userRef;
    }

    public long getRequestId() {
        return requestId;
    }

    public boolean isCreated() {
        return isCreated;
    }
}
