package games.objects;

import games.transform.Quaternion;
import games.transform.Transform;
import games.transform.Vector3;

public class Obstacle {
    public static final int PrefabId = 1;
    public Transform transform = new Transform();
    private long id;

    public Obstacle(Vector3 position, Vector3 bound, Quaternion rotation){
        id = GameObject.getCurrentId();
        transform.position = position;
        transform.rotation = rotation;
        transform.bound = bound;
    }

    public long getId(){
        return id;
    }
}
