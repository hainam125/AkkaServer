package games.network.entity;

import games.network.data.CompressPosition2;
import games.network.data.CompressRotation;

public class ExistingEntity extends Entity {
    public CompressRotation rotation;
    public CompressPosition2 position;
    public int prefabId;

    public ExistingEntity(long id, int prefabId, CompressRotation rotation, CompressPosition2 position) {
        this.id = id;
        this.prefabId = prefabId;
        this.rotation = rotation;
        this.position = position;
    }
}
