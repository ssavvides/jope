package jope;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Return a bit string, generated from the given data string
 *
 * @author savvas
 *
 */
public class Coins {

	Cipher cipher = null;
	SecretKeySpec k;
	byte[] counterBA;
	long counter;
	ByteBuffer buffer;

	public Coins(String key, BigInteger d) {

		try {
			// derive a key using the data to use in AES
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			byte[] digest = sha256_HMAC.doFinal(d.toString().getBytes("UTF-8"));

			this.cipher = Cipher.getInstance("AES/CTR/NoPadding"); // PKCS5Padding

			// requires Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy
			// Files
			this.k = new SecretKeySpec(digest, "AES");

			// FIXME: all 0 IV
			this.buffer = ByteBuffer.allocate(Long.BYTES);
			this.counterBA = new byte[16];
			this.counter = 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Encoder encoder = java.util.Base64.getEncoder();
		// String enc = encoder.encodeToString(digest);

	}

	int byteIndex = 16;
	int bitIndex = 8;
	byte[] ctxt = null;
	boolean[] coins = null;

	public boolean next() {

		// if we have consumed all bytes in the ciphertext, encrypt again and reset the byte index
		if (this.byteIndex == 16) {

			// encrypt again an all 0 array of 16 bytes. This should give a different ciphertext
			// every time since we are using counter.
			try {
				this.buffer.putLong(this.counter);
				byte[] b = this.buffer.array();
				this.buffer.clear();
				System.arraycopy(b, 0, this.counterBA, 8, 8);

				IvParameterSpec iv = new IvParameterSpec(this.counterBA);
				this.cipher.init(Cipher.ENCRYPT_MODE, this.k, iv);

				this.ctxt = this.cipher.doFinal(new byte[16]);

				this.counter++;
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// ciphertext should be 16 bytes long.
			if (this.ctxt.length != 16)
				throw new RuntimeException("invalid ctxt");

			// reset the byte index so we start from the first byte again.
			this.byteIndex = 0;
		}

		// if we have exhausted all bits in the byte indicated by byte index
		if (this.bitIndex == 8) {

			// convert current byte and move the index forward.
			this.coins = byteToBoolArray(this.ctxt[this.byteIndex++]);
			this.bitIndex = 0;
		}

		// return current bit and move bit index forward.
		return this.coins[this.bitIndex++];
	}

	/**
	 * Convert a byte to a boolean array
	 *
	 * @param b
	 * @return
	 */
	public static boolean[] byteToBoolArray(byte b) {

		boolean bs[] = new boolean[8];
		bs[0] = ((b & 0x01) != 0);
		bs[1] = ((b & 0x02) != 0);
		bs[2] = ((b & 0x04) != 0);
		bs[3] = ((b & 0x08) != 0);
		bs[4] = ((b & 0x10) != 0);
		bs[5] = ((b & 0x20) != 0);
		bs[6] = ((b & 0x40) != 0);
		bs[7] = ((b & 0x80) != 0);

		return bs;
	}

	public static void main(String[] args) throws Exception {

	}
}