package games;

import data.Command;

import java.util.LinkedList;
import java.util.Queue;

public class ServerObject {
    public static final int PrefabId = 0;
    private static long currentId = 1;
    private static Vector3 RotateSpeed = new Vector3(0, 100f, 0).mul((float)(Math.PI / 180));
    private final float Speed = 7f;
    private static float deltaTime = 1f / 30f;
    private long id;
    private Queue<Command> commands = new LinkedList<>();
    public boolean isDirty = true;
    public Transform transform = new Transform();

    public ServerObject(){
        id = currentId;
        currentId++;
    }

    private Vector3 getForward(){
        Matrix3x3 matrix = Matrix3x3.getIdentity();
        matrix.RotateY(transform.rotation.toVector3Rad().y);
        return matrix.TransformY(Vector3.forward);
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
        switch (command.keyCode)
        {
            case 0:
                handleMovement(1f, gameMap);
                break;
            case 1:
                handleMovement(-1f, gameMap);
                break;
            case 2:
                handleRotation(1f, gameMap);
                break;
            case 3:
                handleRotation(-1f, gameMap);
                break;
        }
    }

    private void handleRotation(float directon, GameMap gameMap) {
        Quaternion oldRot = transform.rotation;
        transform.rotation = transform.rotation.add(RotateSpeed.mul(directon * deltaTime));
        for(Obstacle obstacle : gameMap.obstacles){
            if(transform.checkCollision(obstacle.transform)){
                transform.rotation = oldRot;
            }
        }
    }

    private void handleMovement(float direction, GameMap gameMap) {
        Vector3 oldPos = transform.position;
        float step = 0.1f;
        float current = 0f;
        while (current <= Speed) {
            current += step;
            if(current > Speed) step = Speed - (current - step);
            transform.position = transform.position.add(getForward().mul(step * deltaTime * direction));
            for(Obstacle obstacle : gameMap.obstacles){
                if(transform.checkCollision(obstacle.transform)){
                    if(oldPos != null) transform.position = oldPos;
                }
            }
        }
    }
}
