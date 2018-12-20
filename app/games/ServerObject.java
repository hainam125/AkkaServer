package games;

import data.Command;

import java.util.LinkedList;
import java.util.Queue;

public class ServerObject {
    private static long currentId = 1;
    private static Vector3 RotateSpeed = new Vector3(0, 100f, 0).mul((float)(Math.PI / 180));
    private final float Speed = 7f;
    private static float deltaTime = 1f / 30f;
    private long id;
    private Queue<Command> commands = new LinkedList<>();
    public boolean isDirty = true;
    public Vector3 position = Vector3.zero;
    public Quaternion rotation = Quaternion.zero;

    public ServerObject(){
        id = currentId;
        currentId++;
    }
    public long getId() {
        return id;
    }

    public void ReceiveCommand(Command command)
    {
        commands.add(command);
    }

    public void UpdateGame()
    {
        while(commands.size() > 0) {
            HandleCommand(commands.poll());
        }
    }

    private void HandleCommand(Command command)
    {
        isDirty = true;
        switch (command.keyCode)
        {
            case 0:
                position = position.add(Vector3.forward.mul(Speed * deltaTime));
                break;
            case 1:
                position = position.subtract(Vector3.forward.mul(Speed * deltaTime));
                break;
            case 2:
                position = position.add(Vector3.right.mul(Speed * deltaTime));
                break;
            case 3:
                position = position.subtract(Vector3.right.mul(Speed * deltaTime));
                break;
            case 4:
                rotation = rotation.add(RotateSpeed.mul(deltaTime));
                break;
        }
        //System.out.println(position);
    }
}
