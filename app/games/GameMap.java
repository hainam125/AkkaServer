package games;

import java.util.ArrayList;
import java.util.List;

public class GameMap {
    public List<Obstacle> obstacles;
    public List<ServerObject> serverObjects;
    public List<Projectile> movingObjects;

    public GameMap(){
        serverObjects = new ArrayList<>();
        movingObjects = new ArrayList<>();
        obstacles = new ArrayList<>();
        obstacles.add(new Obstacle(new Vector3(10, 0, 0.5f), new Vector3(1.5f, 1f, 3.5f), Quaternion.zero));
    }

    public void addMovingObject(Projectile object) {
        movingObjects.add(object);
    }

    public boolean checkCollision(Projectile projectile) {
        for(Obstacle obstacle : obstacles){
            if(projectile.transform.checkCollision(obstacle.transform)){
                return true;
            }
        }
        for(ServerObject o : serverObjects){
            if(projectile.transform.checkCollision(o.transform)){
                return true;
            }
        }
        return false;
    }
}
