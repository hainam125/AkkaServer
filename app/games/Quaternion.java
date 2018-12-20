package games;

public class Quaternion {
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

    public static Quaternion zero = new Quaternion(0, 0, 0, 1);
}
