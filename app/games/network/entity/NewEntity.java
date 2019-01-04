package games.network.entity;

import games.transform.Quaternion;
import games.transform.Vector3;
import games.network.data.CompressPosition2;
import games.network.data.CompressRotation;
import games.network.data.Optimazation;

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
