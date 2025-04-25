package com.sinergise.java.util.string;

import java.io.UnsupportedEncodingException;

import com.sinergise.common.util.math.MathUtil;

//TODO: Replace with MessageDigest.getInstance("MD5")...; benchmarking shows no performance difference
public class MD5 {
	private static final byte[] PADDING = {(byte)0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	int                         state0, state1, state2, state3;
	long                        count;
	byte[]                      buffer;
	boolean                     appending;
	
	public MD5() {
		buffer = new byte[64];
		init();
	}
	
	public void init() {
		appending = true;
		
		count = 0;
		
		state0 = 0x67452301;
		state1 = 0xefcdab89;
		state2 = 0x98badcfe;
		state3 = 0x10325476;
	}
	
	public void append(final byte[] input) {
		append(input, 0, input.length);
	}
	
	public void append(final byte[] input, final int offset, final int length) {
		if (!appending) {
			throw new IllegalStateException("Can't append after producing digest");
		}
		
		int i, index, partLen;
		
		index = (int)count & 0x3F;
		
		count += length;
		
		partLen = 64 - index;
		
		if (length >= partLen) {
			System.arraycopy(input, offset, buffer, index, partLen);
			
			transform(buffer, 0);
			
			for (i = partLen; i + 63 < length; i += 64) {
				transform(input, i);
			}
			
			index = 0;
		} else {
			i = 0;
		}
		
		System.arraycopy(input, offset + i, buffer, index, length - i);
	}
	
	private void prefinish() {
		if (appending) {
			final int count0 = (int)(count << 3);
			final int count1 = (int)(count >>> 29);
			final int pos = (int)count & 63;
			
			append(PADDING, 0, pos < 56 ? 56 - pos : 120 - pos);
			
			buffer[56] = (byte)(count0);
			buffer[57] = (byte)((count0 >>> 8));
			buffer[58] = (byte)((count0 >>> 16));
			buffer[59] = (byte)((count0 >>> 24));
			buffer[60] = (byte)(count1);
			buffer[61] = (byte)((count1 >>> 8));
			buffer[62] = (byte)((count1 >>> 16));
			buffer[63] = (byte)((count1 >>> 24));
			transform(buffer, 0);
			
			appending = false;
		}
	}
	
	public byte[] finish() {
		final byte[] digest = new byte[16];
		finish(digest);
		return digest;
	}
	
	public void finish(final byte[] digest) {
		if (appending) {
			prefinish();
		}
		
		if (digest == null || digest.length < 16) {
			throw new IllegalArgumentException();
		}
		
		digest[0] = (byte)(state0);
		digest[1] = (byte)((state0 >>> 8));
		digest[2] = (byte)((state0 >>> 16));
		digest[3] = (byte)((state0 >>> 24));
		digest[4] = (byte)(state1);
		digest[5] = (byte)((state1 >>> 8));
		digest[6] = (byte)((state1 >>> 16));
		digest[7] = (byte)((state1 >>> 24));
		digest[8] = (byte)(state2);
		digest[9] = (byte)((state2 >>> 8));
		digest[10] = (byte)((state2 >>> 16));
		digest[11] = (byte)((state2 >>> 24));
		digest[12] = (byte)(state3);
		digest[13] = (byte)((state3 >>> 8));
		digest[14] = (byte)((state3 >>> 16));
		digest[15] = (byte)((state3 >>> 24));
	}
	
	public String finishHex() {
		if (appending) {
			prefinish();
		}
		
		final StringBuffer sb = new StringBuffer(32);
		MathUtil.intToHexLE(state0, sb);
		MathUtil.intToHexLE(state1, sb);
		MathUtil.intToHexLE(state2, sb);
		MathUtil.intToHexLE(state3, sb);
		return sb.toString();
	}
	
	/*
	 * MD5 basic transformation. Transforms state based on block.
	 */
	private void transform(final byte[] block, final int blockOff) {
		int a = state0, b = state1, c = state2, d = state3;
		
		final int ij = blockOff;
		final int x0 = (block[ij] & 255) | ((block[ij + 1] & 255) << 8) | ((block[ij + 2] & 255) << 16) | (block[ij + 3] << 24);
		final int x1 = (block[ij + 4] & 255) | ((block[ij + 5] & 255) << 8) | ((block[ij + 6] & 255) << 16) | (block[ij + 7] << 24);
		final int x2 = (block[ij + 8] & 255) | ((block[ij + 9] & 255) << 8) | ((block[ij + 10] & 255) << 16) | (block[ij + 11] << 24);
		final int x3 = (block[ij + 12] & 255) | ((block[ij + 13] & 255) << 8) | ((block[ij + 14] & 255) << 16) | (block[ij + 15] << 24);
		final int x4 = (block[ij + 16] & 255) | ((block[ij + 17] & 255) << 8) | ((block[ij + 18] & 255) << 16) | (block[ij + 19] << 24);
		final int x5 = (block[ij + 20] & 255) | ((block[ij + 21] & 255) << 8) | ((block[ij + 22] & 255) << 16) | (block[ij + 23] << 24);
		final int x6 = (block[ij + 24] & 255) | ((block[ij + 25] & 255) << 8) | ((block[ij + 26] & 255) << 16) | (block[ij + 27] << 24);
		final int x7 = (block[ij + 28] & 255) | ((block[ij + 29] & 255) << 8) | ((block[ij + 30] & 255) << 16) | (block[ij + 31] << 24);
		final int x8 = (block[ij + 32] & 255) | ((block[ij + 33] & 255) << 8) | ((block[ij + 34] & 255) << 16) | (block[ij + 35] << 24);
		final int x9 = (block[ij + 36] & 255) | ((block[ij + 37] & 255) << 8) | ((block[ij + 38] & 255) << 16) | (block[ij + 39] << 24);
		final int x10 = (block[ij + 40] & 255) | ((block[ij + 41] & 255) << 8) | ((block[ij + 42] & 255) << 16) | (block[ij + 43] << 24);
		final int x11 = (block[ij + 44] & 255) | ((block[ij + 45] & 255) << 8) | ((block[ij + 46] & 255) << 16) | (block[ij + 47] << 24);
		final int x12 = (block[ij + 48] & 255) | ((block[ij + 49] & 255) << 8) | ((block[ij + 50] & 255) << 16) | (block[ij + 51] << 24);
		final int x13 = (block[ij + 52] & 255) | ((block[ij + 53] & 255) << 8) | ((block[ij + 54] & 255) << 16) | (block[ij + 55] << 24);
		final int x14 = (block[ij + 56] & 255) | ((block[ij + 57] & 255) << 8) | ((block[ij + 58] & 255) << 16) | (block[ij + 59] << 24);
		final int x15 = (block[ij + 60] & 255) | ((block[ij + 61] & 255) << 8) | ((block[ij + 62] & 255) << 16) | (block[ij + 63] << 24);
		
		/* Round 1 */
		a += ((b & c) | ((~b) & d)) + x0 + 0xd76aa478;
		a = ((a << 7) | (a >>> 25)) + b; /* 1 */
		d += ((a & b) | ((~a) & c)) + x1 + 0xe8c7b756;
		d = ((d << 12) | (d >>> 20)) + a; /* 2 */
		c += ((d & a) | ((~d) & b)) + x2 + 0x242070db;
		c = ((c << 17) | (c >>> 15)) + d; /* 3 */
		b += ((c & d) | ((~c) & a)) + x3 + 0xc1bdceee;
		b = ((b << 22) | (b >>> 10)) + c; /* 4 */
		a += ((b & c) | ((~b) & d)) + x4 + 0xf57c0faf;
		a = ((a << 7) | (a >>> 25)) + b; /* 5 */
		d += ((a & b) | ((~a) & c)) + x5 + 0x4787c62a;
		d = ((d << 12) | (d >>> 20)) + a; /* 6 */
		c += ((d & a) | ((~d) & b)) + x6 + 0xa8304613;
		c = ((c << 17) | (c >>> 15)) + d; /* 7 */
		b += ((c & d) | ((~c) & a)) + x7 + 0xfd469501;
		b = ((b << 22) | (b >>> 10)) + c; /* 8 */
		a += ((b & c) | ((~b) & d)) + x8 + 0x698098d8;
		a = ((a << 7) | (a >>> 25)) + b; /* 9 */
		d += ((a & b) | ((~a) & c)) + x9 + 0x8b44f7af;
		d = ((d << 12) | (d >>> 20)) + a; /* 10 */
		c += ((d & a) | ((~d) & b)) + x10 + 0xffff5bb1;
		c = ((c << 17) | (c >>> 15)) + d; /* 11 */
		b += ((c & d) | ((~c) & a)) + x11 + 0x895cd7be;
		b = ((b << 22) | (b >>> 10)) + c; /* 12 */
		a += ((b & c) | ((~b) & d)) + x12 + 0x6b901122;
		a = ((a << 7) | (a >>> 25)) + b; /* 13 */
		d += ((a & b) | ((~a) & c)) + x13 + 0xfd987193;
		d = ((d << 12) | (d >>> 20)) + a; /* 14 */
		c += ((d & a) | ((~d) & b)) + x14 + 0xa679438e;
		c = ((c << 17) | (c >>> 15)) + d; /* 15 */
		b += ((c & d) | ((~c) & a)) + x15 + 0x49b40821;
		b = ((b << 22) | (b >>> 10)) + c; /* 16 */
		
		/* Round 2 */
		a += ((b & d) | (c & (~d))) + x1 + 0xf61e2562;
		a = ((a << 5) | (a >>> 27)) + b; /* 17 */
		d += ((a & c) | (b & (~c))) + x6 + 0xc040b340;
		d = ((d << 9) | (d >>> 23)) + a; /* 18 */
		c += ((d & b) | (a & (~b))) + x11 + 0x265e5a51;
		c = ((c << 14) | (c >>> 18)) + d; /* 19 */
		b += ((c & a) | (d & (~a))) + x0 + 0xe9b6c7aa;
		b = ((b << 20) | (b >>> 12)) + c; /* 20 */
		a += ((b & d) | (c & (~d))) + x5 + 0xd62f105d;
		a = ((a << 5) | (a >>> 27)) + b; /* 21 */
		d += ((a & c) | (b & (~c))) + x10 + 0x02441453;
		d = ((d << 9) | (d >>> 23)) + a; /* 22 */
		c += ((d & b) | (a & (~b))) + x15 + 0xd8a1e681;
		c = ((c << 14) | (c >>> 18)) + d; /* 23 */
		b += ((c & a) | (d & (~a))) + x4 + 0xe7d3fbc8;
		b = ((b << 20) | (b >>> 12)) + c; /* 24 */
		a += ((b & d) | (c & (~d))) + x9 + 0x21e1cde6;
		a = ((a << 5) | (a >>> 27)) + b; /* 25 */
		d += ((a & c) | (b & (~c))) + x14 + 0xc33707d6;
		d = ((d << 9) | (d >>> 23)) + a; /* 26 */
		c += ((d & b) | (a & (~b))) + x3 + 0xf4d50d87;
		c = ((c << 14) | (c >>> 18)) + d; /* 27 */
		b += ((c & a) | (d & (~a))) + x8 + 0x455a14ed;
		b = ((b << 20) | (b >>> 12)) + c; /* 28 */
		a += ((b & d) | (c & (~d))) + x13 + 0xa9e3e905;
		a = ((a << 5) | (a >>> 27)) + b; /* 29 */
		d += ((a & c) | (b & (~c))) + x2 + 0xfcefa3f8;
		d = ((d << 9) | (d >>> 23)) + a; /* 30 */
		c += ((d & b) | (a & (~b))) + x7 + 0x676f02d9;
		c = ((c << 14) | (c >>> 18)) + d; /* 31 */
		b += ((c & a) | (d & (~a))) + x12 + 0x8d2a4c8a;
		b = ((b << 20) | (b >>> 12)) + c; /* 32 */
		
		/* Round 3 */
		a += (b ^ c ^ d) + x5 + 0xfffa3942;
		a = ((a << 4) | (a >>> 28)) + b; /* 33 */
		d += (a ^ b ^ c) + x8 + 0x8771f681;
		d = ((d << 11) | (d >>> 21)) + a; /* 34 */
		c += (d ^ a ^ b) + x11 + 0x6d9d6122;
		c = ((c << 16) | (c >>> 16)) + d; /* 35 */
		b += (c ^ d ^ a) + x14 + 0xfde5380c;
		b = ((b << 23) | (b >>> 9)) + c; /* 36 */
		a += (b ^ c ^ d) + x1 + 0xa4beea44;
		a = ((a << 4) | (a >>> 28)) + b; /* 37 */
		d += (a ^ b ^ c) + x4 + 0x4bdecfa9;
		d = ((d << 11) | (d >>> 21)) + a; /* 38 */
		c += (d ^ a ^ b) + x7 + 0xf6bb4b60;
		c = ((c << 16) | (c >>> 16)) + d; /* 39 */
		b += (c ^ d ^ a) + x10 + 0xbebfbc70;
		b = ((b << 23) | (b >>> 9)) + c; /* 40 */
		a += (b ^ c ^ d) + x13 + 0x289b7ec6;
		a = ((a << 4) | (a >>> 28)) + b; /* 41 */
		d += (a ^ b ^ c) + x0 + 0xeaa127fa;
		d = ((d << 11) | (d >>> 21)) + a; /* 42 */
		c += (d ^ a ^ b) + x3 + 0xd4ef3085;
		c = ((c << 16) | (c >>> 16)) + d; /* 43 */
		b += (c ^ d ^ a) + x6 + 0x04881d05;
		b = ((b << 23) | (b >>> 9)) + c; /* 44 */
		a += (b ^ c ^ d) + x9 + 0xd9d4d039;
		a = ((a << 4) | (a >>> 28)) + b; /* 45 */
		d += (a ^ b ^ c) + x12 + 0xe6db99e5;
		d = ((d << 11) | (d >>> 21)) + a; /* 46 */
		c += (d ^ a ^ b) + x15 + 0x1fa27cf8;
		c = ((c << 16) | (c >>> 16)) + d; /* 47 */
		b += (c ^ d ^ a) + x2 + 0xc4ac5665;
		b = ((b << 23) | (b >>> 9)) + c; /* 48 */
		
		/* Round 4 */
		a += (c ^ (b | (~d))) + x0 + 0xf4292244;
		a = ((a << 6) | (a >>> 26)) + b; /* 49 */
		d += (b ^ (a | (~c))) + x7 + 0x432aff97;
		d = ((d << 10) | (d >>> 22)) + a; /* 50 */
		c += (a ^ (d | (~b))) + x14 + 0xab9423a7;
		c = ((c << 15) | (c >>> 17)) + d; /* 51 */
		b += (d ^ (c | (~a))) + x5 + 0xfc93a039;
		b = ((b << 21) | (b >>> 11)) + c; /* 52 */
		a += (c ^ (b | (~d))) + x12 + 0x655b59c3;
		a = ((a << 6) | (a >>> 26)) + b; /* 53 */
		d += (b ^ (a | (~c))) + x3 + 0x8f0ccc92;
		d = ((d << 10) | (d >>> 22)) + a; /* 54 */
		c += (a ^ (d | (~b))) + x10 + 0xffeff47d;
		c = ((c << 15) | (c >>> 17)) + d; /* 55 */
		b += (d ^ (c | (~a))) + x1 + 0x85845dd1;
		b = ((b << 21) | (b >>> 11)) + c; /* 56 */
		a += (c ^ (b | (~d))) + x8 + 0x6fa87e4f;
		a = ((a << 6) | (a >>> 26)) + b; /* 57 */
		d += (b ^ (a | (~c))) + x15 + 0xfe2ce6e0;
		d = ((d << 10) | (d >>> 22)) + a; /* 58 */
		c += (a ^ (d | (~b))) + x6 + 0xa3014314;
		c = ((c << 15) | (c >>> 17)) + d; /* 59 */
		b += (d ^ (c | (~a))) + x13 + 0x4e0811a1;
		b = ((b << 21) | (b >>> 11)) + c; /* 60 */
		a += (c ^ (b | (~d))) + x4 + 0xf7537e82;
		a = ((a << 6) | (a >>> 26)) + b; /* 61 */
		d += (b ^ (a | (~c))) + x11 + 0xbd3af235;
		d = ((d << 10) | (d >>> 22)) + a; /* 62 */
		c += (a ^ (d | (~b))) + x2 + 0x2ad7d2bb;
		c = ((c << 15) | (c >>> 17)) + d; /* 63 */
		b += (d ^ (c | (~a))) + x9 + 0xeb86d391;
		b = ((b << 21) | (b >>> 11)) + c; /* 64 */
		
		state0 += a;
		state1 += b;
		state2 += c;
		state3 += d;
	}
	
	public static String hash32(final byte[] bytes) {
		final MD5 ctx = new MD5();
		ctx.append(bytes);
		return ctx.finishHex();
	}
	
	public static String hash32(final String input) {
		try {
			return hash32(input.getBytes("UTF-8"));
		} catch(final UnsupportedEncodingException e) {
			throw new InternalError("UTF-8 not supported ?!?");
		}
	}
	
	public static String hash32(final String input, final String encoding) throws UnsupportedEncodingException {
		final byte[] bytes = input.getBytes(encoding);
		
		final MD5 ctx = new MD5();
		ctx.append(bytes);
		return ctx.finishHex();
	}
}
