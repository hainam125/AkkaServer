package network.object;

import network.data.CompressPosition2;
import network.data.CompressRotation;

public class NewEntity extends Entity {
    public int prefabId;
    public CompressRotation rotation;
    public CompressPosition2 position;

    public NewEntity(long id, int prefabId, CompressRotation rotation, CompressPosition2 position) {
        this.id = id;
        this.prefabId = prefabId;
        this.rotation = rotation;
        this.position = position;
    }
}
