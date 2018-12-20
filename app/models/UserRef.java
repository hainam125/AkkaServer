package models;

import akka.actor.ActorRef;
import games.ServerObject;

public class UserRef {
    private ActorRef self;
    private final ActorRef out;
    private final User user;
    private ActorRef room;
    private ServerObject serverObject;

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

    public ServerObject getServerObject() {
        return serverObject;
    }

    public void setServerObject(ServerObject serverObject) {
        this.serverObject = serverObject;
    }
}
