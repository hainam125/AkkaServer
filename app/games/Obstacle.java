package games;

public class Obstacle {
    public static final int Id = 0;
    public static final int PrefabId = 1;
    public Transform transform = new Transform();

    public Obstacle(Vector3 position, Vector3 bound, Quaternion rotation){
        transform.position = position;
        transform.rotation = rotation;
        transform.bound = bound;
    }
}
