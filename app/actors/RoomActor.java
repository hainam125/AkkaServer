package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import data.*;
import games.*;
import messages.*;
import models.CreateRoom;
import models.User;
import models.UserRef;
import network.data.Optimazation;
import network.object.ExistingEntity;
import network.object.NewEntity;
import network.object.SnapShot;
import play.libs.Json;

import java.time.Duration;
import java.util.*;

public class RoomActor extends AbstractActor {
    public static int tick = 15;
    private Map<Long, ActorRef > websockets;
    private final ActorRef lobbyActor;
    private Map<UserRef, Long> commandsSoFar;
    private Map<UserRef, ServerObject> objectMap;
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
        gameLoop = getContext().getSystem().scheduler().schedule(Duration.ZERO, Duration.ofMillis(time), new GameLoop(), getContext().getSystem().dispatcher());
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

            ServerObject serverObject = new ServerObject();
            userRef.setServerObject(serverObject);

            SnapShot snapShot = currentRoomStatus();
            String snapShotString = Json.toJson(snapShot).toString();

            if(data.isCreated()){
                Response response = new Response(data.getRequestId(), Json.toJson(new CreateRoom(data.getRoom(), serverObject.getId(), snapShotString, true)).toString(), CreateRoom.class.getSimpleName());
                userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
            }
            else {
                Response broadcastResponse = new Response(-1, Json.toJson(new UserJoined(userRef.getUser(), serverObject.getId())).toString(), UserJoined.class.getSimpleName());
                broadcast(Json.toJson(broadcastResponse), userId);

                List<Long> ids = new ArrayList<>();
                List<String> usernames = new ArrayList<>();
                for (Map.Entry<UserRef, ServerObject> entry : objectMap.entrySet())
                {
                    usernames.add(entry.getKey().getUser().getUsername());
                    ids.add(entry.getValue().getId());
                }
                Response response = new Response(data.getRequestId(), Json.toJson(new EnterRoom(data.getRoom(), serverObject.getId(), snapShotString, ids, usernames)).toString(), EnterRoom.class.getSimpleName());
                userRef.getOut().tell(Json.toJson(response), ActorRef.noSender());
            }

            gameMap.objects.add(serverObject);
            commandsSoFar.put(userRef, 0L);
            objectMap.put(userRef, serverObject);
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
            ServerObject serverObject = objectMap.get(userRef);
            objectMap.remove(userRef);
            gameMap.objects.remove(serverObject);
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
        userRef.getServerObject().receiveCommand(command);
        commandsSoFar.put(userRef, command.id);
    }

    private SnapShot currentRoomStatus(){
        ArrayList<NewEntity> syncEntities = new ArrayList<>();
        for(ServerObject object : gameMap.objects){
            NewEntity entity = new NewEntity(
                    object.getId(),
                    ServerObject.PrefabId,
                    object.transform.rotation,
                    object.transform.position,
                    object.transform.bound
            );
            syncEntities.add(entity);
        }
        for(Obstacle object : gameMap.obstacles){
            NewEntity entity = new NewEntity(
                    Obstacle.Id,
                    Obstacle.PrefabId,
                    object.transform.rotation,
                    object.transform.position,
                    object.transform.bound
            );
            syncEntities.add(entity);
        }
        SnapShot snapShot = new SnapShot();
        snapShot.newEntities = syncEntities;
        return snapShot;
    }

    private class GameLoop implements Runnable {
        public void run() {
            Iterator<ServerObject> iter = gameMap.objects.iterator();
            ArrayList<ExistingEntity> syncEntities = new ArrayList<>();

            while (iter.hasNext()) {
                ServerObject object = iter.next();
                object.updateGame(gameMap);
                if (object.isDirty) {
                    ExistingEntity entity = new ExistingEntity(
                            object.getId(),
                            Optimazation.CompressRot(object.transform.rotation),
                            Optimazation.CompressPos2(object.transform.position)
                    );
                    syncEntities.add(entity);
                    object.isDirty = false;
                }
            }

            SnapShot snapShot = new SnapShot();
            snapShot.existingEntities = syncEntities;
            for (Map.Entry<UserRef, Long> entry : commandsSoFar.entrySet()) {
                SnapShot clone = snapShot.clone();
                clone.commandId = entry.getValue();
                Response response = new Response(-1, Json.toJson(clone).toString(), SnapShot.class.getSimpleName());
                entry.getKey().getOut().tell(Json.toJson(response), ActorRef.noSender());
            }
        }
    }
}
