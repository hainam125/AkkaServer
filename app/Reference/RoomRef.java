package Reference;

import akka.actor.ActorRef;
import models.Room;

public class RoomRef {
    private final ActorRef actorRef;
    private final Room room;

    public RoomRef(Room room, ActorRef actorRef){
        this.actorRef = actorRef;
        this.room = room;
    }

    public ActorRef getActorRef() {
        return actorRef;
    }

    public Room getRoom() {
        return room;
    }
}
