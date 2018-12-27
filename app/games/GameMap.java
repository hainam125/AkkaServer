package games;

import java.util.ArrayList;
import java.util.List;

public class GameMap {
    public List<Obstacle> obstacles;
    public List<ServerObject> objects;

    public GameMap(){
        objects = new ArrayList<>();
        obstacles = new ArrayList<>();
        //obstacles.add(new Obstacle(new Vector3(1, 0, -4.5f), new Vector3(3f, 1f, 1.5f), Quaternion.zero));
    }
}
