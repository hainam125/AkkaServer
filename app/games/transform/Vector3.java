package games.transform;

public class Vector3 {
    public static Vector3 zero = new Vector3(0, 0, 0);
    public static Vector3 one = new Vector3(1, 1, 1);
    public static Vector3 forward = new Vector3(0, 0, 1);
    public static Vector3 right = new Vector3(1, 0, 0);

    public final float x;
    public final float y;
    public final float z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 add(Vector3 other) {
        return new Vector3(other.x + x, other.y + y, other.z + z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(-other.x + x, -other.y + y, -other.z + z);
    }

    public Vector3 mul(float multiplier) {
        return new Vector3(multiplier * x, multiplier * y, multiplier * z);
    }

    public static float DistanceSq(Vector3 v1, Vector3 v2) {
        float dx = v1.x - v2.x;
        float dy = v1.y - v2.y;
        float dz = v1.z - v2.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public String toString() {
        return x + " , " + y + " , " + z;
    }
}
