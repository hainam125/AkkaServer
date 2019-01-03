package games;

public enum KeyCode {
    Up((byte)1), Down((byte)2), Right((byte)4), Left((byte)8), Space((byte)16);

    private final byte value;
    KeyCode(byte value) {this.value = value;}
    public byte getValue() {return value;}

    public static boolean isUp(byte code) { return is(Up, code);}
    public static boolean isLeft(byte code) { return is(Left, code);}
    public static boolean isRight(byte code) { return is(Right, code);}
    public static boolean isDown(byte code) { return is(Down, code);}
    public static boolean isSpace(byte code) { return is(Space, code);}
    private static boolean is(KeyCode kc, byte code) { return (kc.getValue() & code) == kc.getValue(); }
}
