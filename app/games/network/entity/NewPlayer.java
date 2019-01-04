package games.network.entity;

import games.transform.Quaternion;
import games.transform.Vector3;

public class NewPlayer extends NewEntity {
    public int hp;

    public NewPlayer(long id, int prefabId, int hp, Quaternion rotation, Vector3 position, Vector3 bound) {
        super(id, prefabId, rotation, position, bound);
        this.hp = hp;
    }
}
