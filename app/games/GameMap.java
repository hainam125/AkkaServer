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
        obstacles.add(new Obstacle(new Vector3(10, 0, 0.5f), new Vector3(1.5f, 1f, 3.5f), Quaternion.zero));
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
