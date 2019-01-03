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
import network.object.DestroyedEntity;
import network.object.ExistingEntity;
import network.object.NewEntity;
import network.object.SnapShot;
import play.libs.Json;

import java.time.Duration;
import java.util.*;

public class RoomActor extends AbstractActor {
    public static int tick = 15;
    public static float deltaTime = 1f / 50f;
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

            gameMap.serverObjects.add(serverObject);
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
            gameMap.serverObjects.remove(serverObject);
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
        ServerObject object = userRef.getServerObject();
        if(KeyCode.isSpace(command.keyCode)) {
            Transform transform = object.transform;
            Vector3 forward = transform.getForward();
            gameMap.addMovingObject(new Projectile(
                    forward,
                    transform.position.add(forward.mul(1.5f)),
                    new Vector3(0.3f, 0.3f, 0.3f),
                    transform.rotation
            ));
        }
        if(command.keyCode != KeyCode.Space.getValue()) {
            object.receiveCommand(command);
        }
        commandsSoFar.put(userRef, command.id);
    }

    private SnapShot currentRoomStatus(){
        ArrayList<NewEntity> syncEntities = new ArrayList<>();
        for(ServerObject object : gameMap.serverObjects){
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
                    object.getId(),
                    Obstacle.PrefabId,
                    object.transform.rotation,
                    object.transform.position,
                    object.transform.bound
            );
            syncEntities.add(entity);
        }

        for(Projectile object : gameMap.movingObjects){
            NewEntity entity = new NewEntity(
                    object.getId(),
                    Projectile.PrefabId,
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
            ArrayList<ExistingEntity> syncEntities = new ArrayList<>();
            ArrayList<ExistingEntity> movingEntities = new ArrayList<>();
            ArrayList<NewEntity> newEntities = new ArrayList<>();
            ArrayList<DestroyedEntity> removeEntities = new ArrayList<>();

            for (Iterator<ServerObject> iter = gameMap.serverObjects.iterator(); iter.hasNext();) {
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

            ArrayList<Projectile> deadProjectiles = new ArrayList<>();
            for (Iterator<Projectile> iter = gameMap.movingObjects.iterator(); iter.hasNext();) {
                Projectile object = iter.next();
                Vector3 oldPos = object.transform.position;

                if(object.isNew) {
                    object.isNew = false;
                    NewEntity entity = new NewEntity(
                            object.getId(),
                            Projectile.PrefabId,
                            object.transform.rotation,
                            object.transform.position,
                            object.transform.bound
                    );
                    newEntities.add(entity);
                }
                else if(object.isDead) {
                    DestroyedEntity entity = new DestroyedEntity(object.getId());
                    removeEntities.add(entity);
                    deadProjectiles.add(object);
                }
                else  {
                    object.transform.position = object.transform.position.add(object.direction.mul(Projectile.Speed).mul(1f / tick));
                    if(gameMap.checkCollision(object)){
                        object.transform.position = oldPos;
                        object.isDead = true;
                    }
                    ExistingEntity entity = new ExistingEntity(
                            object.getId(),
                            Optimazation.CompressRot(object.transform.rotation),
                            Optimazation.CompressPos2(object.transform.position)
                    );
                    movingEntities.add(entity);
                    System.out.println(object.direction);
                }
            }
            gameMap.movingObjects.removeAll(deadProjectiles);

            SnapShot snapShot = new SnapShot();
            snapShot.existingEntities = syncEntities;
            snapShot.movingEntities = movingEntities;
            snapShot.newEntities = newEntities;
            snapShot.destroyedEntities = removeEntities;

            for (Map.Entry<UserRef, Long> entry : commandsSoFar.entrySet()) {
                SnapShot clone = snapShot.clone();
                clone.commandId = entry.getValue();
                Response response = new Response(-1, Json.toJson(clone).toString(), SnapShot.class.getSimpleName());
                entry.getKey().getOut().tell(Json.toJson(response), ActorRef.noSender());
            }
        }
    }
}
