package games;

import actors.RoomActor;
import data.Command;

import java.util.LinkedList;
import java.util.Queue;

public class ServerObject {
    public static final int PrefabId = 0;
    private static long currentId = 1;
    private static Vector3 RotateSpeed = new Vector3(0, 100f, 0).mul((float)(Math.PI / 180));
    private final float Speed = 7f;
    private final float MoveStep = 0.1f;
    private long id;
    private Queue<Command> commands = new LinkedList<>();
    public boolean isDirty = true;
    public Transform transform = new Transform();

    public ServerObject(){
        id = currentId;
        currentId++;
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
        for(Obstacle obstacle : gameMap.obstacles){
            if(transform.checkCollision(obstacle.transform)){
                transform.rotation = oldRot;
                return;
            }
        }
        for(ServerObject o : gameMap.serverObjects){
            if(o != this && transform.checkCollision(o.transform)){
                transform.rotation = oldRot;
                return;
            }
        }
    }

    private void handleMovement(float direction, GameMap gameMap) {
        float current = 0f;
        while (current <= Speed) {
            current += MoveStep;
            float step = current > Speed ? Speed - (current - MoveStep) : MoveStep;
            Vector3 oldPos = transform.position;
            transform.position = transform.position.add(transform.getForward().mul(step * RoomActor.deltaTime * direction));
            for(Obstacle obstacle : gameMap.obstacles){
                if(transform.checkCollision(obstacle.transform)){
                    transform.position = oldPos;
                    return;
                }
            }
            for(ServerObject o : gameMap.serverObjects){
                if(o != this && transform.checkCollision(o.transform)){
                    transform.position = oldPos;
                    return;
                }
            }
        }
    }
}
