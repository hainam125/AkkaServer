package games;

public class GameObject {
    private static long currentId = 1;

    public static long getCurrentId(){
        long id = currentId;
        currentId++;
        return id;
    }
}
