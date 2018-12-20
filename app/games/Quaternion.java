package games;

public class Quaternion {
    public static Quaternion zero = new Quaternion(0, 0, 0, 1);

    public final float x;
    public final float y;
    public final float z;
    public final float w;

    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float get(int position){
        switch (position){
            case 0: return x;
            case 1: return y;
            case 2: return z;
            case 3: return w;
        }
        return x;
    }

    public Quaternion add(Vector3 vector3) {
        Vector3 original = toVector3Rad();
        Vector3 total = original.add(vector3);
        return Quaternion.fromVector3(total);
    }

    //https://www.dreamincode.net/forums/topic/349917-convert-from-quaternion-to-euler-angles-vector3/
    public Vector3 toVector3Rad() {
        double q0 = w;
        double q1 = y;
        double q2 = x;
        double q3 = z;

        float y = (float)Math.atan2(2 * (q0 * q1 + q2 * q3), 1 - 2 * (q1 * q1 + q2 * q2));
        float x = (float)Math.asin(2 * (q0 * q2 - q3 * q1));
        float z = (float)Math.atan2(2 * (q0 * q3 + q1 * q2), 1 - 2 * (q2 * q2 + q3 * q3));

        Vector3 radAngles = new Vector3(x, y, z);
        //return (radAngles * 180 / Mathf.PI);
        return radAngles;
    }

    public static Quaternion fromVector3(Vector3 rotation) {
        return CreateFromYawPitchRoll(rotation.y, rotation.x, rotation.z);
    }

    //https://www.gamedev.net/forums/topic/525748-mathematics-behind-xnas-matrixcreatefromyawpitchroll/
    private static Quaternion CreateFromYawPitchRoll(float yaw, float pitch, float roll) {
        double num9 = roll * 0.5f;
        double num6 = Math.sin(num9);
        double num5 = Math.cos(num9);
        double num8 = pitch * 0.5f;
        double num4 = Math.sin(num8);
        double num3 = Math.cos(num8);
        double num7 = yaw * 0.5f;
        double num2 = Math.sin(num7);
        double num = Math.cos(num7);
        double x = ((num * num4) * num5) + ((num2 * num3) * num6);
        double y = ((num2 * num3) * num5) - ((num * num4) * num6);
        double z = ((num * num3) * num6) - ((num2 * num4) * num5);
        double w = ((num * num3) * num5) + ((num2 * num4) * num6);
        Quaternion result = new Quaternion((float)x, (float)y, (float)z, (float)w);
        return result;
    }

    @Override
    public String toString() {
        return x + " , " + y + " , " + z + " , " + w;
    }
}
