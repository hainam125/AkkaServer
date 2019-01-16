package models;

public class Room {
    private static long currentId = 1;

    private final long id;
    private final String name;
    private final int maxPlayer;
    private int size;

    public Room(String roomName, int playerNb){
        id = currentId++;
        name = roomName;
        maxPlayer = playerNb;
        size = 0;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public int getSize() {
        return size;
    }

    public void addPlayer() {
        size++;
    }

    public void removePlayer() {
        size--;
    }

    public boolean isEmpty(){
        return size <= 0;
    }

    public boolean isFull(){
        return size >= maxPlayer;
    }
}
