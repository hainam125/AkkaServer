package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import data.Response;
import data.RoomList;
import models.UserRef;
import messages.*;
import play.libs.Json;

import java.util.HashMap;
import java.util.Map;

public class LobbyActor extends AbstractActor {
    private Map<Long, UserRef> websockets;
    private Map<String, ActorRef> rooms;

    @Override
    public void preStart() throws Exception{
        super.preStart();
        websockets = new HashMap<>();
        rooms = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Login.class, data -> {
            UserRef userRef = data.getUserRef();
            websockets.put(userRef.getUser().getId(), userRef);
        }).match(NewRoom.class, data -> {
            String roomName = data.getRoomName();
            ActorRef roomActorRef = getContext().actorOf(RoomActor.props(self()), roomName);
            UserRef userRef = websockets.get(data.getUserId());
            rooms.put(roomName, roomActorRef);
            self().tell(new JoinRoom(userRef, roomName, data.getRequestId(), true), ActorRef.noSender());
        }).match(GetRooms.class, data -> {
            UserRef userRef = websockets.get(data.getUserId());
            Response response = new Response(data.getRequestId(), Json.toJson(new RoomList(rooms.keySet())).toString(), RoomList.class.getSimpleName());
            userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
        }).match(JoinRoom.class, data -> {
            String roomName = data.getRoom();
            UserRef userRef = websockets.get(data.getUserRef().getUser().getId());
            ActorRef roomActorRef = rooms.get(roomName);
            userRef.setRoom(roomActorRef);
            roomActorRef.tell(data, ActorRef.noSender());
        }).match(Logout.class, data -> {
            websockets.remove(data.getUserRef().getUser().getId());
        }).match(RoomStatus.class, data -> {
            int member = data.getAmount();
            if(member == 0) {
                rooms.values().remove(sender());
                context().stop(sender());
            }
            System.out.println(rooms.size());
        }).build();
    }
}
