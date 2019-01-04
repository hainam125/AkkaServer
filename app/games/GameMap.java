package games;

import games.network.entity.NewEntity;
import games.network.entity.SnapShot;
import games.objects.Obstacle;
import games.objects.PlayerObject;
import games.objects.Projectile;
import games.transform.Quaternion;
import games.transform.Vector3;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameMap {
    public List<Obstacle> obstacles;
    public List<PlayerObject> playerObjects;
    public List<Projectile> movingObjects;

    private HashMap<PlayerObject, HashSet<PlayerObject>> overlapTransforms;

    public GameMap(){
        playerObjects = new ArrayList<>();
        movingObjects = new CopyOnWriteArrayList<>();
        overlapTransforms = new HashMap<>();
        createWalls();
    }

    private void createWalls(){
        obstacles = new ArrayList<>();
        obstacles.add(new Obstacle(new Vector3(10f, 0f, 0.5f), new Vector3(1.5f, 1f, 3.5f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(0f, 0f, 130f), new Vector3(270f, 1f, 4f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(0f, 0f, -130f), new Vector3(270f, 1f, 4f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(-130f, 0f, 0f), new Vector3(4f, 1f, 270f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(130f, 0f, 0f), new Vector3(4f, 1f, 270f), Quaternion.zero));
    }

    public void removePlayerObject(PlayerObject playerObject) {
        playerObjects.remove(playerObject);
        overlapTransforms.remove(playerObject);
        for (Map.Entry<PlayerObject, HashSet<PlayerObject>> entry : overlapTransforms.entrySet())
        {
            entry.getValue().remove(playerObject);
        }
    }

    public PlayerObject createPlayerObject(){
        PlayerObject playerObject = new PlayerObject();
        HashSet<PlayerObject> overlap = new HashSet<>();
        for(PlayerObject o : playerObjects){
            if(playerObject.transform.checkCollision(o.transform)){
                overlap.add(o);
                if(!overlapTransforms.containsKey(o)){
                    overlapTransforms.put(o, new HashSet<>());
                }
                overlapTransforms.get(o).add(playerObject);
            }
        }
        if(overlap.size() > 0) overlapTransforms.put(playerObject, overlap);
        return playerObject;
    }

    public void addMovingObject(Projectile object) {
        movingObjects.add(object);
    }

    public boolean checkObstacleCollision(Projectile projectile) {
        for(Obstacle obstacle : obstacles){
            if(projectile.transform.checkCollision(obstacle.transform)){
                return true;
            }
        }
        return false;
    }

    public PlayerObject checkPlayerCollision(Projectile projectile) {
        for(PlayerObject o : playerObjects){
            if(projectile.getPlayerObject() != o && projectile.transform.checkCollision(o.transform)){
                return o;
            }
        }
        return null;
    }

    public boolean checkPlayerCollision(PlayerObject player) {
        for(PlayerObject o : playerObjects){
            if(o == player) continue;
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

    public SnapShot currentMapStatus(){
        ArrayList<NewEntity> entities = new ArrayList<>();
        for(PlayerObject object : playerObjects){
            NewEntity entity = new NewEntity(
                    object.getId(),
                    PlayerObject.PrefabId,
                    object.transform.rotation,
                    object.transform.position,
                    object.transform.bound
            );
            entities.add(entity);
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
        return snapShot;
    }
}
