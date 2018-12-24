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
        Vector3 oldPos = null;
        Quaternion oldRot = null;
        switch (command.keyCode)
        {
            case 0:
                oldPos = transform.position;
                transform.position = transform.position.add(getForward().mul(Speed * deltaTime));
                break;
            case 1:
                oldPos = transform.position;
                transform.position = transform.position.subtract(getForward().mul(Speed * deltaTime));
                break;
            case 2:
                oldRot = transform.rotation;
                transform.rotation = transform.rotation.add(RotateSpeed.mul(deltaTime));
                break;
            case 3:
                oldRot = transform.rotation;
                transform.rotation = transform.rotation.add(RotateSpeed.mul(-deltaTime));
                break;
        }
        if(oldPos != null || oldRot != null) {
            for(Obstacle obstacle : gameMap.obstacles){
                if(transform.checkCollision(obstacle.transform)){
                    if(oldPos != null) transform.position = oldPos;
                    if(oldRot != null) transform.rotation = oldRot;
                }
            }
        }
    }
}
