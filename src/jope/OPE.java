package jope;

public class OPE {

	byte[] key;
	ValueRange inRange;
	ValueRange outRange;

	public OPE() {
		this.key = "key".getBytes();
		this.inRange = new ValueRange((long) -Math.pow(2, 32), (long) Math.pow(2, 32));
		this.outRange = new ValueRange((long) -Math.pow(2, 48), (long) Math.pow(2, 48));
	}

	private long encrypt(long ptxt) {

		if (!this.inRange.contains(ptxt))
			throw new RuntimeException("Plaintext is not within the input range");

		return this.encryptRecursive(ptxt, this.inRange, this.outRange);
	}

	private long encryptRecursive(long ptxt, ValueRange inRange, ValueRange outRange) {

		long inSize = inRange.size();
		long outSize = outRange.size();
		long inEdge = inRange.start - 1;
		long outEdge = outRange.start - 1;
		long mid = outEdge + (long) (Math.ceil(outSize / 2.0));
		// System.out.println(inSize + " " + outSize + " " + inEdge + " " + outEdge + " " + mid);

		assert inSize <= outSize;

		if (inRange.size() == 1) {
			Coins coins = new Coins(ptxt);
			long ctxt = sampleUniform(outRange, coins);
			return ctxt;
		}

		Coins coins = new Coins(mid);

		System.out.println("h");
		long x = sampleHGD(inRange, outRange, mid, coins);
		System.out.println("h2");

		if (ptxt <= x) {
			inRange = new ValueRange(inEdge + 1, x);
			outRange = new ValueRange(outEdge + 1, mid);
		} else {
			inRange = new ValueRange(x + 1, inEdge + inSize);
			outRange = new ValueRange(mid + 1, outEdge + outSize);
		}

		return this.encryptRecursive(ptxt, inRange, outRange);
	}

	private long decrypt(long ctxt) {

		if (!this.outRange.contains(ctxt))
			throw new RuntimeException("Ciphertext is not within the input range");

		return this.decryptRecursive(ctxt, this.inRange, this.outRange);
	}

	private long decryptRecursive(long ctxt, ValueRange inRange, ValueRange outRange) {

		long inSize = inRange.size();
		long outSize = outRange.size();
		long inEdge = inRange.start - 1;
		long outEdge = outRange.start - 1;
		long mid = outEdge + (long) (Math.ceil(outSize / 2.0));

		assert inSize <= outSize;

		if (inRange.size() == 1) {
			long inRangeMin = inRange.start;
			Coins coins = new Coins(inRangeMin);
			long sampledCtxt = sampleUniform(outRange, coins);

			if (sampledCtxt == ctxt)
				return inRangeMin;
			else
				throw new RuntimeException("Invalid ciphertext");
		}

		Coins coins = new Coins(mid);
		long x = sampleHGD(inRange, outRange, mid, coins);

		if (ctxt <= mid) {
			inRange = new ValueRange(inEdge + 1, x);
			outRange = new ValueRange(outEdge + 1, mid);
		} else {
			inRange = new ValueRange(x + 1, inEdge + inSize);
			outRange = new ValueRange(mid + 1, outEdge + outSize);
		}

		return this.decryptRecursive(ctxt, inRange, outRange);
	}

	/**
	 * Uniformly select a number from the range using the bit list as a source of randomness
	 *
	 * @param outRange
	 * @param coins
	 * @return
	 */
	private static long sampleUniform(ValueRange inRange, Coins coins) {

		ValueRange curRange = new ValueRange(inRange);

		assert curRange.size() != 0;

		while (curRange.size() > 1) {
			int mid = (int) (Math.floor((curRange.start + curRange.end) / 2));
			boolean bit = coins.next();

			if (bit == false)
				curRange.end = mid;
			else if (bit == true)
				curRange.start = mid + 1;
			else
				throw new RuntimeException("Unexpected bit value");
		}

		assert curRange.size() == 1;

		return curRange.start;
	}

	private static long sampleHGD(ValueRange inRange, ValueRange outRange, long nSample, Coins coins) {

		long inSize = inRange.size();
		long outSize = outRange.size();

		assert inSize > 0 && outSize > 0;
		assert inSize <= outSize;
		assert outRange.contains(nSample);

		long nSampleIndex = nSample - outRange.start + 1;

		if (inSize == outSize)
			return inRange.start + nSampleIndex - 1;

		long inSampleNum = Hgd.rhyper(nSampleIndex, inSize, outSize, coins);

		if (inSampleNum == 0)
			return inRange.start;
		else if (inSampleNum == inSize)
			return inRange.end;
		else {
			long inSample = inRange.start + inSampleNum;

			assert inRange.contains(inSample);

			return inSample;
		}
	}

	public static void main(String[] args) {
		OPE o = new OPE();

		for (int i = 0; i <= 99999; i++) {

			System.out.println(i);

			long e = o.encrypt(i);
			long d = o.decrypt(e);

			if (d != i)
				throw new RuntimeException("failed: " + i + " " + d);

			// System.out.println(e);
			// System.out.println(d);
		}

		System.out.println("done");

	}
}
