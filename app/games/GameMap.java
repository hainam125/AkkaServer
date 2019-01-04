package games;

import games.objects.Obstacle;
import games.objects.PlayerObject;
import games.objects.Projectile;
import games.transform.Quaternion;
import games.transform.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameMap {
    public List<Obstacle> obstacles;
    public List<PlayerObject> playerObjects;
    public List<Projectile> movingObjects;

    public GameMap(){
        playerObjects = new ArrayList<>();
        movingObjects = new CopyOnWriteArrayList<>();
        obstacles = new ArrayList<>();
        obstacles.add(new Obstacle(new Vector3(10f, 0f, 0.5f), new Vector3(1.5f, 1f, 3.5f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(0f, 0f, 130f), new Vector3(270f, 1f, 4f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(0f, 0f, -130f), new Vector3(270f, 1f, 4f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(-130f, 0f, 0f), new Vector3(4f, 1f, 270f), Quaternion.zero));
        obstacles.add(new Obstacle(new Vector3(130f, 0f, 0f), new Vector3(4f, 1f, 270f), Quaternion.zero));
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
            if(projectile.transform.checkCollision(o.transform)){
                return o;
            }
        }
        return null;
    }
}
