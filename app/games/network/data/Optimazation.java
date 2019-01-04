package games.network.data;

import games.transform.Quaternion;
import games.transform.Vector3;

//java byte range [-128;127]
//c# byte range [0-256]
public class Optimazation {
    private final static float ROT_FLOAT_PRECISION_MULT = 32767f / 0.707107f;
    private final static float POS_FLOAT_PRECISION_MULT = 255f;
    private final static float POS1_FLOAT_PRECISION_MULT = 127f;

    public static CompressPosition2 CompressPos2(Vector3 pos) {
        int x1 = (int) Math.floor(pos.x);
        float x2 = pos.x - x1;
        float a1 = x1;
        float a2 = (x2 * 2 - 1) * POS1_FLOAT_PRECISION_MULT;
        int y1 = (int) Math.floor(pos.y);
        float y2 = pos.y - y1;
        float b1 = y1;
        float b2 = (y2 * 2 - 1) * POS1_FLOAT_PRECISION_MULT;
        int z1 = (int) Math.floor(pos.z);
        float z2 = pos.z - z1;
        float c1 = z1;
        float c2 = (z2 * 2 - 1) * POS1_FLOAT_PRECISION_MULT;
        CompressPosition2 data = new CompressPosition2();

        data.a1 = (byte) a1;
        data.b1 = (byte) b1;
        data.c1 = (byte) c1;
        data.a2 = (byte) a2;
        data.b2 = (byte) b2;
        data.c2 = (byte) c2;
        return data;
    }

    public static Vector3 DecompressPos2(CompressPosition2 pos1)
    {
        float a1 = (float)pos1.a1;
        float b1 = (float)pos1.b1;
        float c1 = (float)pos1.c1;
        float a2 = (((float)pos1.a2) / POS1_FLOAT_PRECISION_MULT + 1) / 2f;
        float b2 = (((float)pos1.b2) / POS1_FLOAT_PRECISION_MULT + 1) / 2f;
        float c2 = (((float)pos1.c2) / POS1_FLOAT_PRECISION_MULT + 1) / 2f;
        return new Vector3(a1 + a2, b1 + b2, c1 + c2);
    }

    public static CompressPosition CompressPos(Vector3 pos) {
        int a1 = (int) Math.floor(pos.x);
        float a = pos.x - a1;
        float a2 = (a * 2 - 1) * POS1_FLOAT_PRECISION_MULT;
        int b1 = (int) Math.floor(pos.y);
        float b = pos.y - b1;
        float b2 = (b * 2 - 1) * POS1_FLOAT_PRECISION_MULT;
        int c1 = (int) Math.floor(pos.z);
        float c = pos.z - c1;
        float c2 = (c * 2 - 1) * POS1_FLOAT_PRECISION_MULT;
        CompressPosition data = new CompressPosition();

        data.a1 = (short) a1;
        data.b1 = (short) b1;
        data.c1 = (short) c1;
        data.a2 = (byte) a2;
        data.b2 = (byte) b2;
        data.c2 = (byte) c2;
        return data;
    }

    public static Vector3 DecompressPos(CompressPosition pos)
    {
        float a1 = (float)pos.a1;
        float b1 = (float)pos.b1;
        float c1 = (float)pos.c1;
        float a2 = (((float)pos.a2) / POS1_FLOAT_PRECISION_MULT + 1f) / 2f;
        float b2 = (((float)pos.b2) / POS1_FLOAT_PRECISION_MULT + 1f) / 2f;
        float c2 = (((float)pos.c2) / POS1_FLOAT_PRECISION_MULT + 1f) / 2f;
        return new Vector3(a1 + a2, b1 + b2, c1 + c2);
    }

    public static Quaternion DecompressRot(CompressRotation rotation)
    {
        byte maxIndex = rotation.maxIndex;
        // Values between 4 and 7 indicate that only the index of the single field whose value is 1f was
        // sent, and (maxIndex - 4) is the correct index for that field.
        if (maxIndex >= 4 && maxIndex <= 7)
        {
            float x = (maxIndex == 4) ? 1f : 0f;
            float y = (maxIndex == 5) ? 1f : 0f;
            float z = (maxIndex == 6) ? 1f : 0f;
            float w = (maxIndex == 7) ? 1f : 0f;

            return new Quaternion(x, y, z, w);
        }
        // Read the other three fields and derive the value of the omitted field
        float a = (float)rotation.a / ROT_FLOAT_PRECISION_MULT;
        float b = (float)rotation.b / ROT_FLOAT_PRECISION_MULT;
        float c = (float)rotation.c / ROT_FLOAT_PRECISION_MULT;
        float d = (float) Math.sqrt(1f - (a * a + b * b + c * c));

        if (maxIndex == 0) return new Quaternion(d, a, b, c);
        else if (maxIndex == 1) return new Quaternion(a, d, b, c);
        else if (maxIndex == 2) return new Quaternion(a, b, d, c);

        return new Quaternion(a, b, c, d);
    }

    public static CompressRotation CompressRot(Quaternion rotation)
    {
        short a = (short)0;
        short b = (short)0;
        short c = (short)0;
        byte maxIndex = (byte)0;
        float maxValue = Float.MIN_VALUE;
        float sign = 1f;

        // Determine the index of the largest (absolute value) element in the Quaternion.
        // We will transmit only the three smallest elements, and reconstruct the largest
        // element during decoding.
        for (int i = 0; i < 4; i++)
        {
            float element = rotation.get(i);
            float abs = Math.abs(rotation.get(i));
            if (abs > maxValue)
            {
                // We don't need to explicitly transmit the sign bit of the omitted element because you
                // can make the omitted element always positive by negating the entire quaternion if
                // the omitted element is negative (in quaternion space (x,y,z,w) and (-x,-y,-z,-w)
                // represent the same rotation.), but we need to keep track of the sign for use below.
                sign = (element < 0) ? -1 : 1;

                // Keep track of the index of the largest element
                maxIndex = (byte)i;
                maxValue = abs;
            }
        }

        // We multiply the value of each element by QUAT_PRECISION_MULT before converting to 16-bit integer
        // in order to maintain precision. This is necessary since by definition each of the three smallest
        // elements are less than 1.0, and the conversion to 16-bit integer would otherwise truncate everything
        // to the right of the decimal place. This allows us to keep five decimal places.

        if (maxIndex == 0)
        {
            a = (short)(rotation.y * sign * ROT_FLOAT_PRECISION_MULT);
            b = (short)(rotation.z * sign * ROT_FLOAT_PRECISION_MULT);
            c = (short)(rotation.w * sign * ROT_FLOAT_PRECISION_MULT);
        }
        else if (maxIndex == 1)
        {
            a = (short)(rotation.x * sign * ROT_FLOAT_PRECISION_MULT);
            b = (short)(rotation.z * sign * ROT_FLOAT_PRECISION_MULT);
            c = (short)(rotation.w * sign * ROT_FLOAT_PRECISION_MULT);
        }
        else if (maxIndex == 2)
        {
            a = (short)(rotation.x * sign * ROT_FLOAT_PRECISION_MULT);
            b = (short)(rotation.y * sign * ROT_FLOAT_PRECISION_MULT);
            c = (short)(rotation.w * sign * ROT_FLOAT_PRECISION_MULT);
        }
        else
        {
            a = (short)(rotation.x * sign * ROT_FLOAT_PRECISION_MULT);
            b = (short)(rotation.y * sign * ROT_FLOAT_PRECISION_MULT);
            c = (short)(rotation.z * sign * ROT_FLOAT_PRECISION_MULT);
        }
        CompressRotation data = new CompressRotation();
        data.a = a;
        data.b = b;
        data.c = c;
        data.maxIndex = maxIndex;
        return data;
    }
}
