package jope;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class OPE {

	final static int PRECISION = 10;
	final static RoundingMode RM = RoundingMode.HALF_UP;

	String key;
	ValueRange inRange;
	ValueRange outRange;

	public OPE() {

		this.key = "key";

		this.inRange = new ValueRange(new BigInteger("2").pow(32).negate(),
				new BigInteger("2").pow(32));
		this.outRange = new ValueRange(new BigInteger("2").pow(48).negate(),
				new BigInteger("2").pow(48));

		// this.inRange = new ValueRange(BigInteger.ZERO, new BigInteger("100"));
		// this.outRange = new ValueRange(BigInteger.ZERO, new BigInteger("200"));
	}

	private BigInteger encrypt(BigInteger ptxt) {

		if (!this.inRange.contains(ptxt))
			throw new RuntimeException("Plaintext is not within the input range");

		return this.encryptRecursive(ptxt, this.inRange, this.outRange);
	}

	private BigInteger encryptRecursive(BigInteger ptxt, ValueRange inRange, ValueRange outRange) {

		BigInteger inSize = inRange.size();
		BigInteger outSize = outRange.size();

		assert inSize.compareTo(outSize) <= 0;

		if (inRange.size().compareTo(BigInteger.ONE) == 0) {
			Coins coins = new Coins(this.key, ptxt);
			return sampleUniform(outRange, coins);
		}

		BigInteger inEdge = inRange.start.subtract(BigInteger.ONE);
		BigInteger outEdge = outRange.start.subtract(BigInteger.ONE);

		BigDecimal two = new BigDecimal("2");
		BigInteger m = new BigDecimal(outSize).divide(two, PRECISION, RoundingMode.CEILING)
				.toBigInteger();
		BigInteger mid = outEdge.add(m);

		Coins coins = new Coins(this.key, mid);

		BigInteger x = sampleHGD(inRange, outRange, mid, coins);

		if (ptxt.compareTo(x) <= 0) {
			inRange = new ValueRange(inEdge.add(BigInteger.ONE), x);
			outRange = new ValueRange(outEdge.add(BigInteger.ONE), mid);
		} else {
			inRange = new ValueRange(x.add(BigInteger.ONE), inEdge.add(inSize));
			outRange = new ValueRange(mid.add(BigInteger.ONE), outEdge.add(outSize));
		}

		return this.encryptRecursive(ptxt, inRange, outRange);
	}

	private BigInteger decrypt(BigInteger ctxt) {

		if (!this.outRange.contains(ctxt))
			throw new RuntimeException("Ciphertext is not within the input range");

		return this.decryptRecursive(ctxt, this.inRange, this.outRange);
	}

	private BigInteger decryptRecursive(BigInteger ctxt, ValueRange inRange, ValueRange outRange) {

		BigInteger inSize = inRange.size();
		BigInteger outSize = outRange.size();

		assert inSize.compareTo(outSize) <= 0;

		if (inRange.size().compareTo(BigInteger.ONE) == 0) {
			BigInteger inRangeMin = inRange.start;
			Coins coins = new Coins(this.key, inRangeMin);
			BigInteger sampledCtxt = sampleUniform(outRange, coins);

			if (sampledCtxt.compareTo(ctxt) == 0)
				return inRangeMin;
			else
				throw new RuntimeException("Invalid ciphertext");

		}

		BigInteger inEdge = inRange.start.subtract(BigInteger.ONE);
		BigInteger outEdge = outRange.start.subtract(BigInteger.ONE);
		BigDecimal two = new BigDecimal("2");
		BigInteger m = new BigDecimal(outSize).divide(two, PRECISION, RoundingMode.CEILING)
				.toBigInteger();
		BigInteger mid = outEdge.add(m);

		Coins coins = new Coins(this.key, mid);
		BigInteger x = sampleHGD(inRange, outRange, mid, coins);

		if (ctxt.compareTo(mid) <= 0) {
			inRange = new ValueRange(inEdge.add(BigInteger.ONE), x);
			outRange = new ValueRange(outEdge.add(BigInteger.ONE), mid);
		} else {
			inRange = new ValueRange(x.add(BigInteger.ONE), inEdge.add(inSize));
			outRange = new ValueRange(mid.add(BigInteger.ONE), outEdge.add(outSize));
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
	private static BigInteger sampleUniform(ValueRange inRange, Coins coins) {

		ValueRange curRange = new ValueRange(inRange);

		assert curRange.size().compareTo(BigInteger.ZERO) != 0;

		while (curRange.size().compareTo(BigInteger.ONE) > 0) {

			// System.out.println(curRange.start + " " + curRange.end);

			BigInteger mid = curRange.start.add(curRange.end).divide(new BigInteger("2"));

			boolean bit = coins.next();
			if (bit == false)
				curRange.end = mid;
			else if (bit == true)
				curRange.start = mid.add(BigInteger.ONE);
			else
				throw new RuntimeException("Unexpected bit value");
		}

		assert curRange.size().compareTo(BigInteger.ZERO) != 0;

		return curRange.start;
	}

	private static BigInteger sampleHGD(ValueRange inRange, ValueRange outRange,
			BigInteger nSample, Coins coins) {

		BigInteger inSize = inRange.size();
		BigInteger outSize = outRange.size();

		assert inSize.compareTo(BigInteger.ZERO) > 0 && outSize.compareTo(BigInteger.ZERO) > 0;
		assert inSize.compareTo(outSize) <= 0;
		assert outRange.contains(nSample);

		BigInteger nSampleIndex = nSample.subtract(outRange.start).add(BigInteger.ONE);

		if (inSize.compareTo(outSize) == 0)
			return inRange.start.add(nSampleIndex).subtract(BigInteger.ONE);

		BigInteger inSampleNum = Hgd.rhyper(nSampleIndex, inSize, outSize, coins);

		if (inSampleNum.compareTo(BigInteger.ZERO) == 0)
			return inRange.start;
		else if (inSampleNum.compareTo(inSize) == 0)
			return inRange.end;
		else {
			BigInteger inSample = inRange.start.add(inSampleNum);

			assert inRange.contains(inSample);

			return inSample;
		}
	}

	public static void main(String[] args) {
		OPE o = new OPE();

		for (int i = -99999; i <= 99999; i++) {

			BigInteger p = new BigInteger("" + i);

			BigInteger e = o.encrypt(p);
			BigInteger d = o.decrypt(e);

			if (d.compareTo(p) != 0)
				throw new RuntimeException("failed: " + p + " " + d);

			if (i % 10 == 0)
				System.out.println(e + " " + d);
		}

		System.out.println("done");

	}
}
