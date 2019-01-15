package games.objects;

import actors.RoomActor;
import network.data.Command;
import games.*;
import games.transform.Quaternion;
import games.transform.Transform;
import games.transform.Vector3;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerObject {
    public static final int PrefabId = 0;
    private static final Vector3 RotateSpeed = new Vector3(0f, 50f, 0f).mul((float)(Math.PI / 180f));
    private static final float Speed = 7f;
    private static final float MoveStep = 0.1f;
    private static final float TotalRespawnTime = 3f;
    private static final int MaxHp = 5;

    public Transform transform = new Transform();

    private long id;
    private Queue<Command> commands = new ConcurrentLinkedQueue<>();
    private boolean isDirty;
    private int hp;
    private float respawnTimeLeft;
    private GameMap gameMap;

    public PlayerObject(GameMap map){
        id = GameObject.getCurrentId();
        gameMap = map;
        respawnTimeLeft = 0f;
        respawn();
    }

    public long getId() {
        return id;
    }

    public void decreaseHp(){
        isDirty = true;
        hp--;
    }

    public int getHp(){
        return hp;
    }

    public boolean isDeath() {
        return hp <= 0;
    }

    public boolean checkDirty(){
        if(isDirty){
            isDirty = false;
            return true;
        }
        return false;
    }

    public void reset(){
        isDirty = true;
        respawnTimeLeft = TotalRespawnTime;
    }

    private void respawn() {
        transform.position = Vector3.zero;
        hp = MaxHp;
        isDirty = true;
        gameMap.setNewPosition(this);
    }

    public void receiveCommand(Command command)
    {
        commands.add(command);
    }

    public void updateGame(float deltaTime)
    {
        if(respawnTimeLeft > 0){
            respawnTimeLeft -= deltaTime;
            if(respawnTimeLeft <= 0) {
                respawn();
            }
            return;
        }
        while(commands.size() > 0) {
            handleCommand(commands.poll());
        }
    }

    private void handleCommand(Command command)
    {
        isDirty = true;
        byte code = command.keyCode;
        if(KeyCode.isUp(code)){
            handleMovement(1f);
        }
        else if(KeyCode.isDown(code)) {
            handleMovement(-1f);
        }
        if(KeyCode.isRight(code)){
            handleRotation(1f);
        }
        else if(KeyCode.isLeft(code)) {
            handleRotation(-1f);
        }
    }

    private void handleRotation(float direction) {
        Quaternion oldRot = transform.rotation;
        transform.rotation = transform.rotation.add(RotateSpeed.mul(direction * RoomActor.deltaTime));

        if(gameMap.checkPlayerCollision(this)) {
            transform.rotation = oldRot;
            return;
        }
        if(gameMap.checkObstacleCollision(this)) {
            transform.rotation = oldRot;
            return;
        }
    }

    private void handleMovement(float direction) {
        float current = 0f;
        while (current <= Speed) {
            current += MoveStep;
            float step = current > Speed ? Speed - (current - MoveStep) : MoveStep;
            Vector3 oldPos = transform.position;
            transform.position = transform.position.add(transform.getForward().mul(step * RoomActor.deltaTime * direction));

            if(gameMap.checkPlayerCollision(this)) {
                transform.position = oldPos;
                return;
            }
            if(gameMap.checkObstacleCollision(this)) {
                transform.position = oldPos;
                return;
            }
        }
    }
}
