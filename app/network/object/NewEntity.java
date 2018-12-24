package network.object;

import games.Quaternion;
import games.Vector3;
import network.data.CompressPosition2;
import network.data.CompressRotation;
import network.data.Optimazation;

public class NewEntity extends Entity {
    public int prefabId;
    public CompressRotation rotation;
    public CompressPosition2 position;
    public CompressPosition2 bound;

    public NewEntity(long id, int prefabId, Quaternion rotation, Vector3 position, Vector3 bound) {
        this.id = id;
        this.prefabId = prefabId;
        this.rotation = Optimazation.CompressRot(rotation);
        this.position = Optimazation.CompressPos2(position);
        this.bound = Optimazation.CompressPos2(bound);
    }
}
