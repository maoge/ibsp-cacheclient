package ibsp.cache.client.protocol;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class ByteUtil {
	public static final String	CHARSET	= "UTF-8";

	/**
	 * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF,
	 * 0xD9}
	 * 
	 * @param src
	 *            String
	 * @return byte[]
	 */
	public static byte[] HexString2Bytes(String src) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		byte[] tmp = src.getBytes();
		for (int i = 0; 2 * i + 1 < tmp.length; i++) {
			b.write( uniteBytes( tmp[ i * 2 ], tmp[ i * 2 + 1 ] ) );
		}
		return b.toByteArray();
	}

	/**
	 * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF
	 * 
	 * @param src0
	 *            byte
	 * @param src1
	 *            byte
	 * @return byte
	 */
	public static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode( "0x" + new String( new byte[] { src0 } ) ).byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode( "0x" + new String( new byte[] { src1 } ) ).byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	/**
	 * 
	 * @param b
	 *            byte[]
	 * @return String
	 */
	public static String Bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			ret += byte2Hex( b[ i ] );
		}
		return ret;
	}

	public static String byte2Hex(byte b) {
		String hex = Integer.toHexString( b & 0xFF );
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		return hex.toUpperCase();
	}

	/**
	 * 字节转化为二进制串
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2String(byte b) {
		char[] c = new char[8];
		for (int i = 0; i < 8; i++) {
			c[ i ] = (char) (((b >> (7 - i)) & 0x01) + '0');
		}
		return new String( c );
	}

	public static String bcdToString(byte[] b) throws Exception {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			int h = ((b[ i ] & 0xff) >> 4);
			// sb.append ((char) h);
			sb.append( intTobcd( h ) );
			int l = (b[ i ] & 0x0f);
			// sb.append ((char) l);
			sb.append( intTobcd( l ) );
		}
		return sb.toString();
	}

	private static char intTobcd(int s) throws Exception {
		char a1 = '0';
		if (s >= 10 && s <= 15) {
			a1 = (char) (s + 'A' - 10);
		} else if (s >= 0 && s <= 9) {
			a1 = (char) (s + '0');
		} else {
			throw new Exception( "错误的bcd char" + a1 );
		}
		return a1;
	}

	private static int bcdtoInt(char a1) throws Exception {
		int s = 0;
		if (a1 >= '0' && a1 <= '9') {
			s = a1 - '0';
		} else if (a1 >= 'A' && a1 <= 'F') {
			s = a1 - 'A' + 10;
		} else if (a1 >= 'a' && a1 >= 'f') {
			s = a1 - 'a' + 10;
		} else {
			throw new Exception( "错误的bcd char" + a1 );
		}
		return s;
	}

	/**
	 * 将String转成BCD码
	 * 
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public static byte[] StrToBCDBytes(String s) throws Exception {

		if (s.length() % 2 != 0) {
			s = "0" + s;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		char[] cs = s.toCharArray();
		for (int i = 0; i < cs.length; i += 2) {
			int high = bcdtoInt( cs[ i ] );
			int low = bcdtoInt( cs[ i + 1 ] );
			baos.write( high << 4 | low );
		}
		return baos.toByteArray();
	}

	/**
	 * 将一个16字节数组转成128二进制数组
	 * 
	 * @param b
	 * @return
	 */
	public static boolean[] getBinaryFromByte(byte[] b) {
		boolean[] binary = new boolean[b.length * 8 + 1];
		String strsum = "";
		for (int i = 0; i < b.length; i++) {
			strsum += getEigthBitsStringFromByte( b[ i ] );
		}
		for (int i = 0; i < strsum.length(); i++) {
			if (strsum.substring( i, i + 1 ).equalsIgnoreCase( "1" )) {
				binary[ i + 1 ] = true;
			} else {
				binary[ i + 1 ] = false;
			}
		}
		return binary;
	}

	public static String getEigthBitsStringFromByte(int b) {
		// if this is a positive number its bits number will be less
		// than 8
		// so we have to fill it to be a 8 digit binary string
		// b=b+100000000(2^8=256) then only get the lower 8 digit
		b |= 256; // mark the 9th digit as 1 to make sure the string
		// has at
		// least 8 digits
		String str = Integer.toBinaryString( b );
		int len = str.length();
		return str.substring( len - 8, len );
	}

	/**
	 * 二进制串转化成字符串数组
	 * 
	 * @param a
	 * @return
	 * @throws Exception
	 */
	public static byte Binary2byte(String bs) throws Exception {
		return Binary2byte( bs.toCharArray() );
	}

	public static byte Binary2byte(char[] str) throws Exception {
		// if(str.length()!=8)
		// throw new Exception("It's not a 8 length string");
		if (str.length != 8) {
			throw new Exception( "bit转化错误[" + str.length + "]" );
		}
		byte b;
		// check if it's a minus number
		if (str[ 0 ] == '1') {
			// get lower 7 digits original code
			str[ 0 ] = '0';
			b = Byte.valueOf( new String( str ), 2 );
			// then recover the 8th digit as 1 equal to plus
			// 1000000
			b |= 128;
		} else {
			b = Byte.valueOf( new String( str ), 2 );
		}
		return b;
	}

	public static byte[][] encodeMany(final String... strs) {
		byte[][] many = new byte[strs.length][];
		for (int i = 0; i < strs.length; i++) {
			many[ i ] = encode( strs[ i ] );
		}
		return many;
	}

	public static byte[] encode(final String str) {
		if (str == null) {
			return null;
		}
		byte[] result = null;
		try {
			result = str.getBytes( CHARSET );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String encode(final byte[] data) {
		if (data == null) {
			return null;
		}
		String result = null;
		try {
			result = new String( data, CHARSET );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

}
