package games.network.entity;

import java.util.ArrayList;

public class SnapShot {
    public ArrayList<NewEntity> newEntities;
    public ArrayList<NewPlayer> newPlayers;
    public ArrayList<ExistingEntity> existingEntities;
    public ArrayList<ExistingPlayer> existingPlayers;
    public ArrayList<DestroyedEntity> destroyedEntities;
    public long commandId;

    public SnapShot clone() {
        SnapShot data = new SnapShot();

        data.newEntities = newEntities;
        data.newPlayers = newPlayers;
        data.existingEntities = existingEntities;
        data.existingPlayers = existingPlayers;
        data.destroyedEntities = destroyedEntities;
        data.commandId = commandId;
        return data;
    }
}
