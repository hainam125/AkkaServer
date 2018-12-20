package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.*;
import models.*;
import messages.*;
import play.libs.Json;

public class WebSocketActor extends AbstractActor {
    private final ActorRef lobbyActor;
    private final UserRef userRef;

    public static Props props(UserRef userRef, ActorRef lobbyActor) {
        return Props.create(WebSocketActor.class, userRef, lobbyActor);
    }

    private WebSocketActor(UserRef userRef, ActorRef lobbyActor) {
        this.userRef = userRef;
        this.lobbyActor = lobbyActor;
    }

    @Override
    public void preStart() throws Exception{
        super.preStart();
        userRef.setSelf(self());
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        lobbyActor.tell(new Logout(userRef), ActorRef.noSender());
        ActorRef room = userRef.getRoom();
        if(room != null) room.tell(new Logout(userRef), ActorRef.noSender());
        userRef.setServerObject(null);
        System.out.println("Logout...");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JsonNode.class, msg->{
            Request request = Json.fromJson(msg, Request.class);
            User user = userRef.getUser();
            ObjectMapper mapper = new ObjectMapper();
            //System.out.println(request.type);
            JsonNode actualObj = request.data.isEmpty() ? null : mapper.readTree(request.data);
            if(request.type.equals(User.class.getSimpleName())){
                user.setUsername(Json.fromJson(actualObj, User.class).getUsername());
                Response response = new Response(request.id, Json.toJson(user).toString(), User.class.getSimpleName());
                userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
            }
            else if(request.type.equals(CreateRoom.class.getSimpleName())){
                CreateRoom createRoom = Json.fromJson(actualObj, CreateRoom.class);
                lobbyActor.tell(new NewRoom(createRoom.getName(), userRef.getUser().getId(), request.id), ActorRef.noSender());
            }
            else if(request.type.equals(RoomList.class.getSimpleName())){
                lobbyActor.tell(new GetRooms(userRef.getUser().getId(), request.id), ActorRef.noSender());
            }
            else if(request.type.equals(EnterRoom.class.getSimpleName())){
                lobbyActor.tell(new JoinRoom(userRef, Json.fromJson(actualObj, CreateRoom.class).getName(), request.id, false), ActorRef.noSender());
            }
            else if(request.type.equals(Command.class.getSimpleName())){
                Command command = Json.fromJson(actualObj, Command.class);
                userRef.getRoom().tell(new Send(command, userRef), ActorRef.noSender());
            }

        }).build();
    }
}
