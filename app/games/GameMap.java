package games;

import games.network.data.Optimazation;
import games.network.entity.*;
import games.objects.Obstacle;
import games.objects.PlayerObject;
import games.objects.Projectile;
import games.transform.Quaternion;
import games.transform.Transform;
import games.transform.Vector3;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameMap {
    private List<Obstacle> obstacles;
    private List<PlayerObject> playerObjects;
    private List<Projectile> movingObjects;

    private HashMap<PlayerObject, HashSet<PlayerObject>> overlapTransforms;

    public GameMap(){
        playerObjects = new ArrayList<>();
        movingObjects = new CopyOnWriteArrayList<>();
        overlapTransforms = new HashMap<>();
        createWalls();
    }

    private void createWalls(){
        obstacles = new ArrayList<>();

        obstacles.add(new Obstacle(new Vector3(10f, 0f, 0.5f), new Vector3(1.5f, 3f, 3.5f), Quaternion.zero));

        obstacles.add(new Obstacle(new Vector3(24f, 0f, 10f), new Vector3(2f, 3f, 30f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(-22f, 0f, 30f), new Vector3(25f, 3f, 2f), new Quaternion(0f, 0.38275f, 0f, 0.92385f)));

        obstacles.add(new Obstacle(new Vector3(0f, 0f, 50f), new Vector3(100f, 3f, 4f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(0f, 0f, -50f), new Vector3(100f, 3f, 4f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(-50f, 0f, 0f), new Vector3(4f, 3f, 100f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(50f, 0f, 0f), new Vector3(4f, 3f, 100f), Quaternion.zero));
    }

    public void addMovingObject(Projectile object) {
        movingObjects.add(object);
    }

    public void removePlayerObject(PlayerObject playerObject) {
        playerObjects.remove(playerObject);
        overlapTransforms.remove(playerObject);
        for (Map.Entry<PlayerObject, HashSet<PlayerObject>> entry : overlapTransforms.entrySet())
        {
            entry.getValue().remove(playerObject);
        }
    }

    public void addPlayerObject(PlayerObject playerObject) {
        playerObjects.add(playerObject);
    }

    public void setNewPosition(PlayerObject playerObject) {
        HashSet<PlayerObject> overlap = new HashSet<>();
        for(PlayerObject o : playerObjects){
            if(o == playerObject) continue;
            if(playerObject.transform.checkCollision(o.transform)){
                overlap.add(o);
                if(!overlapTransforms.containsKey(o)){
                    overlapTransforms.put(o, new HashSet<>());
                }
                overlapTransforms.get(o).add(playerObject);
            }
        }
        if(overlap.size() > 0) overlapTransforms.put(playerObject, overlap);
    }

    public boolean checkPlayerCollision(PlayerObject player) {
        for(PlayerObject o : playerObjects){
            if(o == player || o.isDeath()) continue;
            if(player.transform.checkCollision(o.transform)) {
                if(!overlapTransforms.containsKey(player)) {
                    return true;
                }
            }
            else {
                if(overlapTransforms.containsKey(player)) {
                    HashSet<PlayerObject> overlap = overlapTransforms.get(player);
                    if(overlap.remove(o) && overlap.size() == 0){
                        overlapTransforms.remove(player);
                    }
                }
            }
        }
        return false;
    }

    public boolean checkObstacleCollision(PlayerObject player) {
        for(Obstacle obstacle : obstacles){
            if(player.transform.checkCollision(obstacle.transform)){
                return true;
            }
        }
        return false;
    }

    public SnapShot updateGame(float deltaTime){
        ArrayList<ExistingEntity> existingProjectiles = new ArrayList<>();
        ArrayList<ExistingPlayer> existingPlayers = new ArrayList<>();
        ArrayList<NewEntity> newProjectiles = new ArrayList<>();
        ArrayList<DestroyedEntity> removeEntities = new ArrayList<>();
        ArrayList<Projectile> deadProjectiles = new ArrayList<>();

        for (Iterator<PlayerObject> iter = playerObjects.iterator(); iter.hasNext();) {
            PlayerObject object = iter.next();
            object.updateGame(deltaTime);
        }

        for (Iterator<Projectile> iter = movingObjects.iterator(); iter.hasNext();) {
            Projectile object = iter.next();

            if(object.isNew) {
                if(object.checkProjectileCollisionWithOthers(obstacles, playerObjects)){
                    deadProjectiles.add(object);
                }
                else {
                    object.isNew = false;
                    object.transform.position = object.transform.position.add(object.direction.mul(Projectile.Speed).mul(deltaTime * 0.25f));
                    NewEntity entity = new NewEntity(
                            object.getId(),
                            Projectile.PrefabId,
                            object.transform.rotation,
                            object.transform.position,
                            object.transform.bound
                    );
                    newProjectiles.add(entity);
                }
            }
            else if(object.isDead) {
                DestroyedEntity entity = new DestroyedEntity(object.getId());
                removeEntities.add(entity);
                deadProjectiles.add(object);
            }
            else  {
                object.handleMovement(obstacles, playerObjects, deltaTime);
                ExistingEntity entity = new ExistingEntity(
                        object.getId(),
                        Projectile.PrefabId,
                        Optimazation.CompressRot(object.transform.rotation),
                        Optimazation.CompressPos2(object.transform.position)
                );
                existingProjectiles.add(entity);
            }
        }
        movingObjects.removeAll(deadProjectiles);

        for (Iterator<PlayerObject> iter = playerObjects.iterator(); iter.hasNext();) {
            PlayerObject object = iter.next();
            if (object.checkDirty()) {
                ExistingPlayer entity = new ExistingPlayer(
                        object.getId(),
                        PlayerObject.PrefabId,
                        object.getHp(),
                        Optimazation.CompressRot(object.transform.rotation),
                        Optimazation.CompressPos2(object.transform.position)
                );
                existingPlayers.add(entity);
            }
        }

        SnapShot snapShot = new SnapShot();
        snapShot.existingEntities = existingProjectiles;
        snapShot.existingPlayers = existingPlayers;
        snapShot.newEntities = newProjectiles;
        snapShot.destroyedEntities = removeEntities;
        return snapShot;
    }

    public SnapShot currentMapStatus(){
        ArrayList<NewPlayer> players = new ArrayList<>();
        ArrayList<NewEntity> entities = new ArrayList<>();
        for(PlayerObject object : playerObjects){
            NewPlayer entity = new NewPlayer(
                    object.getId(),
                    PlayerObject.PrefabId,
                    object.getHp(),
                    object.transform.rotation,
                    object.transform.position,
                    object.transform.bound
            );
            players.add(entity);
        }
        for(Obstacle object : obstacles){
            NewEntity entity = new NewEntity(
                    object.getId(),
                    Obstacle.PrefabId,
                    object.transform.rotation,
                    object.transform.position,
                    object.transform.bound
            );
            entities.add(entity);
        }

        for(Projectile object : movingObjects){
            NewEntity entity = new NewEntity(
                    object.getId(),
                    Projectile.PrefabId,
                    object.transform.rotation,
                    object.transform.position,
                    object.transform.bound
            );
            entities.add(entity);
        }
        SnapShot snapShot = new SnapShot();
        snapShot.newEntities = entities;
        snapShot.newPlayers = players;
        return snapShot;
    }
}
