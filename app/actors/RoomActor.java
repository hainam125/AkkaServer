package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import games.network.entity.*;
import network.*;
import games.*;
import games.objects.PlayerObject;
import games.objects.Projectile;
import games.transform.Transform;
import games.transform.Vector3;
import messages.*;
import network.data.*;
import models.User;
import models.UserRef;
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

    public static Props props(ActorRef lobbyActor) {
        return Props.create(RoomActor.class, () -> new RoomActor(lobbyActor));
    }

    public RoomActor(ActorRef lobbyActor) {
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
            long userId = userRef.getUser().getId();
            websockets.put(userId, userRef.getOut());

            PlayerObject playerObject = new PlayerObject(gameMap);
            userRef.setPlayerObject(playerObject);

            SnapShot snapShot = gameMap.currentMapStatus();
            String snapShotString = Json.toJson(snapShot).toString();

            if(data.isCreated()){
                Response response = new Response(data.getRequestId(), Json.toJson(new CreateRoom(data.getRoom(), playerObject.getId(), snapShotString, true)).toString(), CreateRoom.class.getSimpleName());
                userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
            }
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
            if(websockets.size() == 0) lobbyActor.tell(new RoomStatus(websockets.size()), getSelf());
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
        if(KeyCode.isSpace(command.keyCode)) {
            Transform transform = object.transform;
            Vector3 forward = transform.getForward();
            gameMap.addMovingObject(new Projectile(
                    forward,
                    transform.position.add(forward.mul(0.80f)).add(new Vector3(0f, 0.2f, 0f)),
                    new Vector3(0.25f, 0.25f, 0.35f),
                    transform.rotation,
                    object
            ));
        }
        if(command.keyCode != KeyCode.Space.getValue()) {
            object.receiveCommand(command);
        }
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
