package games.objects;

import games.transform.Quaternion;
import games.transform.Transform;
import games.transform.Vector3;

public class Projectile {
    public static final float Speed = 18f;
    public static final int PrefabId = 2;
    private long id;
    private PlayerObject playerObject;
    public Transform transform = new Transform();
    public Vector3 direction;
    public boolean isDead;
    public boolean isNew;

    public Projectile(Vector3 direction, Vector3 position, Vector3 bound, Quaternion rotation, PlayerObject playerObject){
        this.id = GameObject.getCurrentId();
        this.isNew = true;
        this.direction = direction;
        this.playerObject = playerObject;
        transform.position = position;
        transform.rotation = rotation;
        transform.bound = bound;
    }

    public long getId(){
        return id;
    }

    public PlayerObject getPlayerObject() {
        return playerObject;
    }
}
