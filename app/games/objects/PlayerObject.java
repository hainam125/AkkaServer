package games.objects;

import actors.RoomActor;
import network.data.Command;
import games.*;
import games.transform.Quaternion;
import games.transform.Transform;
import games.transform.Vector3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerObject {
    public static final int PrefabId = 0;
    private static final Vector3 RotateSpeed = new Vector3(0f, 100f, 0f).mul((float)(Math.PI / 180f));
    private static final float Speed = 7f;
    private static final float MoveStep = 0.1f;

    private long id;
    private Queue<Command> commands = new ConcurrentLinkedQueue<>();
    public boolean isDirty = true;
    public Transform transform = new Transform();

    public PlayerObject(){
        id = GameObject.getCurrentId();
    }

    public long getId() {
        return id;
    }

    public void receiveCommand(Command command)
    {
        commands.add(command);
    }

    public void updateGame(GameMap gameMap)
    {
        while(commands.size() > 0) {
            handleCommand(commands.poll(), gameMap);
        }
    }

    private void handleCommand(Command command, GameMap gameMap)
    {
        isDirty = true;
        byte code = command.keyCode;
        if(KeyCode.isUp(code)){
            handleMovement(1f, gameMap);
        }
        else if(KeyCode.isDown(code)) {
            handleMovement(-1f, gameMap);
        }
        if(KeyCode.isRight(code)){
            handleRotation(1f, gameMap);
        }
        else if(KeyCode.isLeft(code)) {
            handleRotation(-1f, gameMap);
        }
    }

    private void handleRotation(float direction, GameMap gameMap) {
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

    private void handleMovement(float direction, GameMap gameMap) {
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
