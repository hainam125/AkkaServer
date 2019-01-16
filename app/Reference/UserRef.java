package Reference;

import akka.actor.ActorRef;
import games.objects.PlayerObject;
import models.User;

public class UserRef {
    private ActorRef self;
    private final ActorRef out;
    private final User user;
    private ActorRef room;
    private PlayerObject playerObject;

    public UserRef(User user, ActorRef out) {
        this.user = user;
        this.out = out;
    }

    public User getUser() {
        return user;
    }

    public void setSelf(ActorRef self) {
        this.self = self;
    }

    public ActorRef getOut() {
        return out;
    }

    public ActorRef getSelf() {
        return self;
    }

    public ActorRef getRoom() {
        return room;
    }

    public void setRoom(ActorRef room) {
        this.room = room;
    }

    public PlayerObject getPlayerObject() {
        return playerObject;
    }

    public void setPlayerObject(PlayerObject playerObject) {
        this.playerObject = playerObject;
    }
}
