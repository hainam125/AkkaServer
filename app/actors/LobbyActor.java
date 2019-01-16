package actors;

import Reference.RoomRef;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import models.Room;
import network.Response;
import network.data.EnterRoom;
import network.data.RoomList;
import network.data.CreateRoom;
import Reference.UserRef;
import messages.*;
import play.libs.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LobbyActor extends AbstractActor {
    private Map<Long, UserRef> websockets;
    private Map<String, RoomRef> rooms;

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
                Room room = new Room(roomName, data.getPlayerAmount());
                ActorRef roomActorRef = getContext().actorOf(RoomActor.props(self(), room), roomName);
                RoomRef roomRef = new RoomRef(room, roomActorRef);
                rooms.put(roomName, roomRef);
                self().tell(new JoinRoom(userRef, roomName, data.getRequestId(), true), ActorRef.noSender());
            }
        }).match(GetRooms.class, data -> {
            UserRef userRef = websockets.get(data.getUserId());
            ArrayList<Room> roomList = new ArrayList<>();
            for (Map.Entry<String, RoomRef> entry : rooms.entrySet())
            {
                roomList.add(entry.getValue().getRoom());
            }
            Response response = new Response(data.getRequestId(), Json.toJson(new RoomList(roomList)).toString(), RoomList.class.getSimpleName());
            userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
        }).match(JoinRoom.class, data -> {
            String roomName = data.getRoom();
            UserRef userRef = websockets.get(data.getUserRef().getUser().getId());
            if(rooms.containsKey(roomName)) {
                RoomRef roomRef = rooms.get(roomName);
                Room room = roomRef.getRoom();
                if(room.isFull()) {
                    Response response = new Response(data.getRequestId(), Json.toJson(new EnterRoom("", -1, "", null, null)).toString(), CreateRoom.class.getSimpleName());
                    userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
                }
                else {
                    ActorRef roomActorRef = roomRef.getActorRef();
                    roomActorRef.tell(data, ActorRef.noSender());
                }
            }
            //Room has been expired
            else {
                Response response = new Response(data.getRequestId(), Json.toJson(new EnterRoom("", -1, "", null, null)).toString(), CreateRoom.class.getSimpleName());
                userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
            }
        }).match(Logout.class, data -> {
            websockets.remove(data.getUserRef().getUser().getId());
        }).match(RoomStatus.class, data -> {
            int member = data.getAmount();
            if(member == 0) {
                rooms.remove(data.getRoomName());
                context().stop(sender());
            }
        }).build();
    }
}
