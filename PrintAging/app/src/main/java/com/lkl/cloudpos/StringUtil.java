package com.lkl.cloudpos.aidl;


import java.nio.ByteBuffer;
import java.util.Arrays;

public class StringUtil {

    public static final String[] hexStrings;

    static {
        hexStrings = new String[256];
        for (int i = 0; i < 256; i++) {
            StringBuilder d = new StringBuilder(2);
            char ch = Character.forDigit((byte) i >> 4 & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            ch = Character.forDigit((byte) i & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            hexStrings[i] = d.toString();
        }

    }

    public static int bcd2int(byte bb) {
        return ((bb >> 4) & 0x0F) * 10 + (bb & 0x0F);
    }

    public static int bcd2int(byte[] bb, int offset, int len) {
        int result = 0;
        for (int i = 0; i < len; i++) {
            result = result * 100 + bcd2int(bb[offset + i]);
        }
        return result;
    }

    public static byte[] str2bcd(String s, boolean padLeft, byte[] d, int offset) {
        char c;
        int len = s.length();
        int start = (len & 1) == 1 && padLeft ? 1 : 0;
        for (int i = start; i < len + start; i++) {
            c = s.charAt(i - start);
            if (c >= '0' && c <= '?') // 30~3f
                c -= '0';
            else {
                c &= ~0x20;
                c -= 'A' - 10;
            }
            d[offset + (i >> 1)] |= c << ((i & 1) == 1 ? 0 : 4);
        }
        return d;
    }

    public static byte[] str2bcd(String s, boolean padLeft) {
        if (s == null)
            return null;
        int len = s.length();
        byte[] d = new byte[len + 1 >> 1];
        return str2bcd(s, padLeft, d, 0);
    }

    public static int byte2int(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        for (int i = 0; i < 4 - bytes.length; i++) {
            byteBuffer.put((byte) 0);
        }
        for (int i = 0; i < bytes.length; i++) {
            byteBuffer.put(bytes[i]);
        }
        byteBuffer.position(0);
        return byteBuffer.getInt();
    }

    public static int byte2int(byte[] bb, int offset, int len) {
        byte[] temp = new byte[len];
        System.arraycopy(bb, offset, temp, 0, len);
        return byte2int(temp);
    }

    public static byte[] str2bcd(String s, boolean padLeft, byte fill) {
        char c;
        int len = s.length();
        byte[] d = new byte[len + 1 >> 1];
        Arrays.fill(d, fill);
        int start = (len & 1) == 1 && padLeft ? 1 : 0;
        for (int i = start; i < len + start; i++) {
            c = s.charAt(i - start);
            if (c >= '0' && c <= '?') // 30~3f
                c -= '0';
            else {
                c &= ~0x20;
                c -= 'A' - 10;
            }
            d[i >> 1] |= c << ((i & 1) == 1 ? 0 : 4);
            // d[i >> 1] |= s.charAt(i - start) - '0' << ((i & 1) == 1 ? 0 : 4);
        }
        return d;
    }

    public static String bcd2str(byte[] b, int offset, int len, boolean padLeft) {
        StringBuilder d = new StringBuilder(len);
        int start = (len & 1) == 1 && padLeft ? 1 : 0;
        for (int i = start; i < len + start; i++) {
            int shift = (i & 1) == 1 ? 0 : 4;
            char c = Character.forDigit(b[offset + (i >> 1)] >> shift & 0x0F, 16);
            if (c == 'd')
                c = '=';
            d.append(Character.toUpperCase(c));
        }
        return d.toString();
    }

    public static String hexString(byte[] b, int offset, int len) {
        String ret = "";
        try {
            if (b == null) {
                return "";
            }
            StringBuilder d = new StringBuilder(len * 2);
            len += offset;
            for (int i = offset; i < len; i++) {
                d.append(hexStrings[(int) b[i] & 0xFF]);
            }
            ret = d.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String hexString(byte[] b) {
        if (b == null) {
            return "";
        }
        return hexString(b, 0, b.length);
    }

    public static byte[] hex2ByteArray(String hexStr) {
        int len = hexStr.length();
        if ((len & 0x01) == 0x01) {
            throw new IllegalArgumentException("hexStr is invalid: " + hexStr);
        }

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (Short.valueOf(hexStr.substring(i, i + 2).toString(), 16) & 0xff);
        }
        return data;
    }

    public static byte[] hex2byte(byte[] b, int offset, int len) {
        byte[] d = new byte[len];
        for (int i = 0; i < len * 2; i++) {
            int shift = i % 2 == 1 ? 0 : 4;
            d[i >> 1] |= Character.digit((char) b[offset + i], 16) << shift;
        }
        return d;
    }

    public static byte[] hex2byte(String s) {
        if (s.length() % 2 == 0) {
            return hex2byte(s.getBytes(), 0, s.length() >> 1);
        } else {
            // Padding left zero to make it even size #Bug raised by tommy
            return hex2byte("0" + s);
        }
    }

    public static String formatAmount(int amount) {
        String amountStr = String.valueOf(amount);
        try {
            if (amountStr.length() == 1) {
                amountStr = "0.0" + amountStr;
            } else if (amountStr.length() == 2) {
                amountStr = "0." + amountStr;
            } else {
                amountStr = amountStr.substring(0, amountStr.length() - 2) + "." + amountStr.substring(amountStr.length() - 2);
            }
        } catch (Exception e) {
            return amountStr;
        }
        return amountStr;

    }

    public static void main(String[] args) {

        String hex = "9A0C425C5A2F2763B7575E9685F5E02C0CDE9CA3B47956735A52D48D21CF0C6E";
        byte[] hexBytes = hex2byte(hex);
        System.out.println(hexString(hexBytes));

        System.out.println(formatAmount(900001));

    }

    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s = encode;
                return s;
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return "";
    }


    public static String removeTail(String str, String tag) {
        // 如果字符串尾部不为0，返回字符串
        if (!str.substring(str.length() - 1).equals(tag)) {
            return str;
        } else {
            // 否则将字符串尾部删除一位再进行递归
            return removeTail(str.substring(0, str.length() - 1), tag);
        }
    }

}
