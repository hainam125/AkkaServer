package network.object;

import java.util.ArrayList;

public class SnapShot {
    public ArrayList<NewEntity> newEntities;
    public ArrayList<ExistingEntity> existingEntities;
    public ArrayList<ExistingEntity> movingEntities;
    public ArrayList<DestroyedEntity> destroyedEntities;
    public long commandId;

    public SnapShot clone() {
        SnapShot data = new SnapShot();

        data.newEntities = newEntities;
        data.existingEntities = existingEntities;
        data.movingEntities = movingEntities;
        data.destroyedEntities = destroyedEntities;
        data.commandId = commandId;
        return data;
    }
}
