package network.object;

import network.data.CompressPosition2;
import network.data.CompressRotation;

public class ExistingEntity extends Entity {
    public CompressRotation rotation;
    public CompressPosition2 position;

    public ExistingEntity(long id, CompressRotation rotation, CompressPosition2 position) {
        this.id = id;
        this.rotation = rotation;
        this.position = position;
    }
}
