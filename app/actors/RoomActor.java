package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import games.network.entity.*;
import models.Room;
import network.*;
import games.*;
import games.objects.PlayerObject;
import messages.*;
import network.data.*;
import models.User;
import Reference.UserRef;
import play.libs.Json;

import java.time.Duration;
import java.util.*;

public class RoomActor extends AbstractActor {
    public static int tick = 15;
    public static float deltaTime = 1f / 50f;
    private Map<Long, ActorRef > websockets;
    private final ActorRef lobbyActor;
    private Map<UserRef, Long> commandsSoFar;
    private Map<UserRef, PlayerObject> objectMap;
    private GameMap gameMap;
    private Cancellable gameLoop;
    private Room room;

    public static Props props(ActorRef lobbyActor, Room room) {
        return Props.create(RoomActor.class, () -> new RoomActor(lobbyActor, room));
    }

    public RoomActor(ActorRef lobbyActor, Room room) {
        this.room = room;
        this.lobbyActor = lobbyActor;
        websockets = new HashMap<>();
        commandsSoFar = new HashMap<>();
        objectMap = new HashMap<>();
        gameMap = new GameMap();
    }

    @Override
    public void preStart() throws Exception{
        super.preStart();
        long time = 1000 / tick;
        gameLoop = getContext().getSystem().scheduler().schedule(Duration.ZERO, Duration.ofMillis(time), new GameLoop(time), getContext().getSystem().dispatcher());
    }

    @Override
    public void postStop() throws Exception {
        gameLoop.cancel();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JoinRoom.class, data -> {
            UserRef userRef = data.getUserRef();

            userRef.setRoom(self());
            long userId = userRef.getUser().getId();
            websockets.put(userId, userRef.getOut());

            PlayerObject playerObject = new PlayerObject(gameMap);
            userRef.setPlayerObject(playerObject);

            SnapShot snapShot = gameMap.currentMapStatus();
            String snapShotString = Json.toJson(snapShot).toString();

            room.addPlayer();

            //create and join room
            if(data.isCreated()){
                Response response = new Response(data.getRequestId(), Json.toJson(new CreateRoom(data.getRoom(), playerObject.getId(), snapShotString, true)).toString(), CreateRoom.class.getSimpleName());
                userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
            }
            //join room only
            else {
                Response broadcastResponse = new Response(-1, Json.toJson(new UserJoined(userRef.getUser(), playerObject.getId())).toString(), UserJoined.class.getSimpleName());
                broadcast(Json.toJson(broadcastResponse), userId);

                List<Long> ids = new ArrayList<>();
                List<String> usernames = new ArrayList<>();
                for (Map.Entry<UserRef, PlayerObject> entry : objectMap.entrySet())
                {
                    usernames.add(entry.getKey().getUser().getUsername());
                    ids.add(entry.getValue().getId());
                }
                Response response = new Response(data.getRequestId(), Json.toJson(new EnterRoom(data.getRoom(), playerObject.getId(), snapShotString, ids, usernames)).toString(), EnterRoom.class.getSimpleName());
                userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
            }

            gameMap.addPlayerObject(playerObject);//add here to avoid duplicate
            commandsSoFar.put(userRef, 0L);
            objectMap.put(userRef, playerObject);
        }).match(Send.class, data -> {
            receiveCommand(data.getUserRef(), data.getCommand());
        }).match(Logout.class, data -> {
            UserRef userRef = data.getUserRef();
            User user = userRef.getUser();
            long userId = user.getId();
            long objectId = objectMap.get(data.getUserRef()).getId();
            Response broadcastResponse = new Response(-1, Json.toJson(new UserExited(user, objectId)).toString(), UserExited.class.getSimpleName());
            broadcast (Json.toJson(broadcastResponse), userId);

            commandsSoFar.remove(userRef);
            PlayerObject playerObject = objectMap.get(userRef);
            objectMap.remove(userRef);
            gameMap.removePlayerObject(playerObject);
            websockets.remove(userId);
            room.removePlayer();
            if(room.isEmpty()) lobbyActor.tell(new RoomStatus(room.getSize(), room.getName()), getSelf());
        }).build();
    }

    private void broadcast(JsonNode res, long user) {
        for (Map.Entry<Long, ActorRef> entry : websockets.entrySet())
        {
            if(!entry.getKey().equals(user)) entry.getValue().tell(res, ActorRef.noSender());
        }
    }

    private void receiveCommand(UserRef userRef, Command command){
        PlayerObject object = userRef.getPlayerObject();
        object.receiveCommand(command);
        commandsSoFar.put(userRef, command.id);
    }

    private class GameLoop implements Runnable {
        private float deltaTime;
        private GameLoop(long time){
            deltaTime = time / 1000f;
        }
        public void run() {
            SnapShot snapShot = gameMap.updateGame(deltaTime);

            for (Map.Entry<UserRef, Long> entry : commandsSoFar.entrySet()) {
                SnapShot clone = snapShot.clone();
                clone.commandId = entry.getValue();
                Response response = new Response(-1, Json.toJson(clone).toString(), SnapShot.class.getSimpleName());
                entry.getKey().getOut().tell(Json.toJson(response), ActorRef.noSender());
            }
        }
    }
}
