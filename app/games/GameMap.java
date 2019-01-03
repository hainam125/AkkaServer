package games;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameMap {
    public List<Obstacle> obstacles;
    public List<ServerObject> serverObjects;
    public List<Projectile> movingObjects;

    public GameMap(){
        serverObjects = new ArrayList<>();
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

    public ServerObject checkPlayerCollision(Projectile projectile) {
        for(ServerObject o : serverObjects){
            if(projectile.transform.checkCollision(o.transform)){
                return o;
            }
        }
        return null;
    }
}
