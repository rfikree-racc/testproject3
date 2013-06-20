package org.ozsoft.secs.format;

/**
 * 2-byte unsigned integer (U2).
 * 
 * @author Oscar Stigter
 */
public class U2 implements Data<Integer> {
    
    public static final int LENGTH = 2;
    
    private static final int MIN_VALUE = 0x00;
    
    private static final int MAX_VALUE = 0xffff;
    
    private int value;
    
    public U2() {
        this(0);
    }
    
    public U2(int value) {
        setValue(value);
    }
    
    public U2(byte[] data) {
        setValue(data);
    }
    
    public U2(B b) {
        setValue(b);
    }
    
    @Override
    public Integer getValue() {
        return value;
    }
    
    @Override
    public void setValue(Integer value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException("Invalid U2 value: " + value);
        }
        this.value = value;
    }

    public void setValue(byte[] data) {
        if (data.length != LENGTH) {
            throw new IllegalArgumentException("Invalid U2 length: " + data.length);
        }
        setValue((((int) (data[0] & 0xff)) << 8) | (((int) (data[1] & 0xff))));
    }
    
    public void setValue(B b) {
        final int length = b.length();
        if (length != LENGTH) {
            throw new IllegalArgumentException("Invalid U2 length: " + length);
        }
        value = b.get(0) << 8 | b.get(1);
    }
    
    @Override
    public int length() {
        return LENGTH;
    }
    
    @Override
    public byte[] toByteArray() {
        return new byte[] {(byte) (value >> 8), (byte) (value & 0xff)};
    }
    
    public B toB() {
        B b = new B();
        b.add(value >> 8);
        b.add(value & 0xff);
        return b;
    }
    
    @Override
    public int hashCode() {
        return Integer.valueOf(value).hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof U2) {
            return ((U2) obj).value == value;
        } else {
            return false;
        }
    }
    
    @Override
    public String toSml() {
        return String.format("U2(%d)", value);
    }

    @Override
    public String toString() {
        return toSml();
    }
    
}