package games;

public class Projectile {
    public static final float Speed = 13f;
    public static final int PrefabId = 2;
    public Transform transform = new Transform();
    public Vector3 direction;
    private long id;
    public boolean isDead;
    public boolean isNew;

    public Projectile(Vector3 direction, Vector3 position, Vector3 bound, Quaternion rotation){
        this.id = GameObject.getCurrentId();
        this.isNew = true;
        this.direction = direction;
        transform.position = position;
        transform.rotation = rotation;
        transform.bound = bound;
    }

    public long getId(){
        return id;
    }
}
