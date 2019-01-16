package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import network.Response;
import network.data.EnterRoom;
import network.data.RoomList;
import network.data.CreateRoom;
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
            UserRef userRef = websockets.get(data.getUserId());
            if(rooms.containsKey(roomName)) {
                Response response = new Response(data.getRequestId(), Json.toJson(new CreateRoom("", -1, "", false)).toString(), CreateRoom.class.getSimpleName());
                userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
            }
            else {
                ActorRef roomActorRef = getContext().actorOf(RoomActor.props(self(), data.getPlayerAmount()), roomName);
                rooms.put(roomName, roomActorRef);
                self().tell(new JoinRoom(userRef, roomName, data.getRequestId(), true), ActorRef.noSender());
            }
        }).match(GetRooms.class, data -> {
            UserRef userRef = websockets.get(data.getUserId());
            Response response = new Response(data.getRequestId(), Json.toJson(new RoomList(rooms.keySet())).toString(), RoomList.class.getSimpleName());
            userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
        }).match(JoinRoom.class, data -> {
            String roomName = data.getRoom();
            if(rooms.containsKey(roomName)) {
                ActorRef roomActorRef = rooms.get(roomName);
                roomActorRef.tell(data, ActorRef.noSender());
            }
            else {
                UserRef userRef = websockets.get(data.getUserRef().getUser().getId());
                Response response = new Response(data.getRequestId(), Json.toJson(new EnterRoom("", -1, "", null, null)).toString(), CreateRoom.class.getSimpleName());
                userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
            }
        }).match(Logout.class, data -> {
            websockets.remove(data.getUserRef().getUser().getId());
        }).match(RoomStatus.class, data -> {
            int member = data.getAmount();
            if(member == 0) {
                rooms.values().remove(sender());
                context().stop(sender());
            }
        }).build();
    }
}
