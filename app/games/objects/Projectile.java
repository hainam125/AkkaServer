package games.objects;

import games.GameMap;
import games.transform.Quaternion;
import games.transform.Transform;
import games.transform.Vector3;

import java.util.List;

public class Projectile {
    private static final float MaxDistance = 20f;
    private static final float MoveStep = 0.1f;
    public static final float Speed = 18f;
    public static final int PrefabId = 2;
    private long id;
    private PlayerObject playerObject;
    public Transform transform = new Transform();
    public Vector3 direction;
    public boolean isDead;
    public boolean isNew;
    private float distance;

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

    public void handleMovement(List<Obstacle> obstacles, List<PlayerObject> playerObjects, float deltaTime){
        float current = 0f;
        while (current <= Speed) {
            current += MoveStep;
            float step = current > Speed ? Speed - (current - MoveStep) : MoveStep;
            float dist = step * deltaTime;
            distance += dist;
            transform.position = transform.position.add(direction.mul(dist));
            if(distance > MaxDistance || checkProjectileCollisionWithOthers(obstacles, playerObjects)){
                isDead = true;
                break;
            }
        }
    }

    public boolean checkProjectileCollisionWithOthers(List<Obstacle> obstacles, List<PlayerObject> playerObjects) {
        PlayerObject playerObject = checkPlayerCollision(playerObjects);
        if(playerObject != null){
            playerObject.decreaseHp();
            if(playerObject.isDeath()) {
                playerObject.reset();
            }
            return true;
        }
        else if(checkObstacleCollision(obstacles)) {
            return true;
        }
        return false;
    }

    private boolean checkObstacleCollision(List<Obstacle> obstacles) {
        for(Obstacle obstacle : obstacles){
            if(transform.checkCollision(obstacle.transform)){
                return true;
            }
        }
        return false;
    }

    private PlayerObject checkPlayerCollision(List<PlayerObject> playerObjects) {
        for(PlayerObject o : playerObjects){
            if(playerObject != o && !o.isDeath() && transform.checkCollision(o.transform)){
                return o;
            }
        }
        return null;
    }
}
