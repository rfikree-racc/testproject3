package org.ozsoft.secs.format;

import java.util.ArrayList;
import java.util.List;

/**
 * 2-byte unsigned integer (U2).
 * 
 * @author Oscar Stigter
 */
public class U4 implements Data<List<Long>> {
    
    public static final int FORMAT_CODE = 0xb0;
    
    public static final int MIN_LENGTH = 4;
    
    private static final long MIN_VALUE = 0x00000000L;
    
    private static final long MAX_VALUE = 0xffffffffL;
    
    private List<Long> values = new ArrayList<Long>();
    
    public U4() {
        // Empty implementation.
    }
    
    public U4(long value) {
        addValue(value);
    }
    
    public U4(byte[] data) {
        addValue(data);
    }
    
    @Override
    public List<Long> getValue() {
        return values;
    }
    
    @Override
    public void setValue(List<Long> value) {
        this.values = value;
    }
    
    public long getValue(int index) {
        return values.get(index);
    }
    
    public void addValue(long value) {
      if (value < MIN_VALUE || value > MAX_VALUE) {
          throw new IllegalArgumentException("Invalid U4 value: " + value);
      }
        this.values.add(value);
    }
    
    public void addValue(byte[] data) {
        if (data.length < MIN_LENGTH) {
            throw new IllegalArgumentException("Invalid U2 length: " + data.length);
        }
        addValue((((long) (data[0] & 0x7f)) << 24) | (((long) (data[1] & 0xff)) << 16) | (((long) (data[2] & 0xff)) << 8) | (((long) (data[3] & 0xff))));
    }
    
    @Override
    public int length() {
        return values.size();
    }
    
    @Override
    public byte[] toByteArray() {
//      byte formatByte = (byte) (FORMAT_CODE | 0x01); // 1 length byte
//      byte lengthByte = values.size();
      int length = length();
      byte[] array = new byte[length * MIN_LENGTH];
      for (int i = 0; i < length; i++) {
          long value = values.get(i);
          array[i * MIN_LENGTH] = (byte) (value >> 24);
          array[i * MIN_LENGTH + 1] = (byte) (value >> 16);
          array[i * MIN_LENGTH + 2] = (byte) (value >> 8);
          array[i * MIN_LENGTH + 3] = (byte) (value & 0x000000ff);
      }
      return array;
    }

    @Override
    public String toSml() {
        int length = length();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("U4:%d", length));
        if (length > 0) {
            boolean first = true;
            sb.append(" {");
            for (long value : values) {
                if (!first) {
                    sb.append(' ');
                } else {
                    first = false;
                }
                sb.append(value);
            }
            sb.append('}');
        }
        return sb.toString();
    }
    
    //FIXME: Implement U4.hashCode()
    
    //FIXME: Enhance U4.equals()
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof U4) {
            return ((U4) obj).values == values;
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return toSml();
    }

}
