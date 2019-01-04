package games.network.entity;

import games.network.data.CompressPosition2;
import games.network.data.CompressRotation;

public class ExistingPlayer extends ExistingEntity {
    public int hp;

    public ExistingPlayer(long id, int prefabId, int hp, CompressRotation rotation, CompressPosition2 position) {
        super(id, prefabId, rotation, position);
        this.hp = hp;
    }
}
