package jope;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class Hgd {

	private final static double TWO_32 = Math.pow(2, 32) - 1;

	private static final int bdPrecision = 20;
	private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(bdPrecision);

	// some repeated big integer/decimal literals
	private final static BigDecimal decHalf = BigDecimal.valueOf(0.5);
	private final static BigDecimal dec2 = BigDecimal.valueOf(2);
	private final static BigDecimal dec3 = BigDecimal.valueOf(3);
	private final static BigDecimal dec4 = BigDecimal.valueOf(4);
	private final static BigDecimal dec7 = BigDecimal.valueOf(7);
	private final static BigDecimal dec16 = BigDecimal.valueOf(16);
	private final static BigDecimal dec2p = BigDecimal.valueOf(2 * Math.PI);
	private final static BigDecimal log2p = decHalf
			.multiply(BigDecimalUtils.ln(dec2p, bdPrecision));

	/**
	 * Private utility method used to compute the square root of a BigDecimal.
	 */
	private static BigDecimal sqrtNewtonRaphson(BigDecimal c, BigDecimal xn, BigDecimal precision) {
		BigDecimal fx = xn.pow(2).add(c.negate());
		BigDecimal fpx = xn.multiply(new BigDecimal(2));
		BigDecimal xn1 = fx.divide(fpx, 2 * bdPrecision, RoundingMode.HALF_DOWN);
		xn1 = xn.add(xn1.negate());
		BigDecimal currentSquare = xn1.pow(2);
		BigDecimal currentPrecision = currentSquare.subtract(c);
		currentPrecision = currentPrecision.abs();

		if (currentPrecision.compareTo(precision) <= -1)
			return xn1;

		return sqrtNewtonRaphson(c, xn1, precision);
	}

	/**
	 * Uses Newton Raphson to compute the square root of a BigDecimal.
	 */
	private static BigDecimal bigSqrt(BigDecimal c) {
		return sqrtNewtonRaphson(c, BigDecimal.ONE, BigDecimal.ONE.divide(SQRT_PRE));
	}

	/**
	 *
	 * @param coins
	 * @return
	 */
	public static double prngDraw(Coins coins) {

		long out = 0;
		for (int i = 0; i < 32; i++)
			// TODO don't calculate powers every time
			out += coins.next() ? Math.pow(2, i) : 0;

		return out / TWO_32;
	}

	public static BigInteger rhyper(BigInteger kk, BigInteger nn1, BigInteger nn2, Coins coins) {
		if (kk.compareTo(BigInteger.TEN) > 0)
			return hypergeometricHrua(coins, nn1, nn2, kk);
		else
			return hypergeometricHyp(coins, nn1, nn2, kk);
	}

	private static BigInteger hypergeometricHyp(Coins coins, BigInteger good, BigInteger bad,
			BigInteger sample) {

		BigDecimal d1 = new BigDecimal(bad.add(good).subtract(sample));
		BigDecimal d2 = new BigDecimal(bad.min(good));

		BigDecimal Y = d2;
		BigDecimal K = new BigDecimal(sample);

		while (Y.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal U = BigDecimal.valueOf(prngDraw(coins));

			BigDecimal d1K = d1.add(K);
			BigDecimal inner = U.add(Y.divide(d1K, OPE.PRECISION, OPE.RM)).setScale(0,
					RoundingMode.FLOOR);
			Y = Y.subtract(inner);

			K = K.subtract(BigDecimal.ONE);
			if (K.compareTo(BigDecimal.ZERO) == 0)
				break;
		}

		BigInteger Z = d2.subtract(Y).toBigInteger();
		if (good.compareTo(bad) > 0)
			Z = sample.subtract(Z);

		return Z;
	}

	private final static BigDecimal D1 = new BigDecimal("1.7155277699214135");
	private final static BigDecimal D2 = new BigDecimal("0.8989161620588988");

	private static BigInteger hypergeometricHrua(Coins coins, BigInteger good, BigInteger bad,
			BigInteger sampleBI) {

		boolean moreGood;
		BigDecimal badBD = new BigDecimal(bad);
		BigDecimal goodBD = new BigDecimal(good);
		BigDecimal mingoodbad;
		BigDecimal maxgoodbad;
		if (good.compareTo(bad) > 0) {
			moreGood = true;
			mingoodbad = badBD;
			maxgoodbad = goodBD;

		} else {
			moreGood = false;
			mingoodbad = goodBD;
			maxgoodbad = badBD;
		}

		BigDecimal popsize = new BigDecimal(good.add(bad));
		BigDecimal sample = new BigDecimal(sampleBI);
		BigDecimal m = sample.min(popsize.subtract(sample));
		BigDecimal d4 = mingoodbad.divide(popsize, OPE.PRECISION, OPE.RM);
		BigDecimal d5 = BigDecimal.ONE.subtract(d4);
		BigDecimal d6 = m.multiply(d4).add(decHalf);

		BigDecimal d7a = popsize.subtract(m).multiply(sample).multiply(d4).multiply(d5)
				.divide(popsize.subtract(BigDecimal.ONE), OPE.PRECISION, OPE.RM).add(decHalf);
		BigDecimal d7 = bigSqrt(d7a);

		BigDecimal d8 = D1.multiply(d7).add(D2);

		BigDecimal mingoodbadplus1 = mingoodbad.add(BigDecimal.ONE);
		BigDecimal d9 = m.add(BigDecimal.ONE).multiply(mingoodbadplus1)
				.divide(popsize.add(dec2), OPE.PRECISION, OPE.RM);

		BigDecimal d9plus1 = d9.add(BigDecimal.ONE);
		BigDecimal d10 = loggam(d9plus1).add(loggam(mingoodbadplus1.subtract(d9)))
				.add(loggam(m.subtract(d9).add(BigDecimal.ONE)))
				.add(loggam(maxgoodbad.subtract(m).add(d9plus1)));

		BigDecimal d11a = m.min(mingoodbad).add(BigDecimal.ONE);
		BigDecimal d11b = d6.add(d7.multiply(dec16)).setScale(0, RoundingMode.FLOOR);
		BigDecimal d11 = d11a.min(d11b);

		BigDecimal Z;
		while (true) {
			BigDecimal X = BigDecimal.valueOf(prngDraw(coins));
			BigDecimal Y = BigDecimal.valueOf(prngDraw(coins));
			BigDecimal W = d6
					.add(d8.multiply(Y.subtract(decHalf)).divide(X, OPE.PRECISION, OPE.RM));

			if (W.compareTo(BigDecimal.ZERO) < 0 || W.compareTo(d11) >= 0)
				continue;

			Z = W.setScale(0, RoundingMode.FLOOR);

			BigDecimal Zplus1 = Z.add(BigDecimal.ONE);
			BigDecimal Zminus1 = Z.subtract(BigDecimal.ONE);
			BigDecimal T = d10.subtract(loggam(Zplus1).add(loggam(mingoodbad.subtract(Zminus1)))
					.add(loggam(m.subtract(Zminus1)))
					.add(loggam(maxgoodbad.subtract(m).add(Zplus1))));

			if (X.multiply(dec4.subtract(X)).subtract(dec3).compareTo(T) <= 0)
				break;

			if (X.multiply(X.subtract(T)).compareTo(BigDecimal.ONE) >= 0)
				continue;

			if (dec2.multiply(BigDecimalUtils.ln(X, bdPrecision)).compareTo(T) <= 0)
				break;
		}

		if (moreGood)
			Z = m.subtract(Z);

		if (m.compareTo(sample) < 0)
			Z = goodBD.subtract(Z);

		return Z.toBigInteger();
	}

	private final static BigDecimal[] a = new BigDecimal[] {
			BigDecimal.valueOf(8.333333333333333e-02), BigDecimal.valueOf(-2.777777777777778e-03),
			BigDecimal.valueOf(7.936507936507937e-04), BigDecimal.valueOf(-5.952380952380952e-04),
			BigDecimal.valueOf(8.417508417508418e-04), BigDecimal.valueOf(-1.917526917526918e-03),
			BigDecimal.valueOf(6.410256410256410e-03), BigDecimal.valueOf(-2.955065359477124e-02),
			BigDecimal.valueOf(1.796443723688307e-01), BigDecimal.valueOf(-1.39243221690590e+00) };

	private static BigDecimal loggam(BigDecimal x) {

		BigDecimal x0 = x;
		int n = 0;

		if (x.compareTo(BigDecimal.ONE) == 0 || x.compareTo(dec2) == 0)
			return BigDecimal.ZERO;
		else if (x.compareTo(dec7) <= 0) {
			n = (int) (7.0 - x.doubleValue());
			x0 = x.add(new BigDecimal(n));
		}

		BigDecimal x2 = BigDecimal.ONE.divide(x0.multiply(x0), OPE.PRECISION, OPE.RM);

		BigDecimal gl0 = a[9];

		for (int k = 8; k >= 0; k--) {
			gl0 = gl0.multiply(x2);
			gl0 = gl0.add(a[k]);
		}

		BigDecimal gl = gl0.divide(x0, OPE.PRECISION, OPE.RM).add(log2p)
				.add(x0.subtract(decHalf).multiply(BigDecimalUtils.ln(x0, bdPrecision)))
				.subtract(x0);

		if (x.compareTo(dec7) <= 0)
			for (int k = 1; k <= n + 1; k++) {
				x0 = x0.subtract(BigDecimal.ONE);
				gl = gl.subtract(BigDecimalUtils.ln(x0, bdPrecision));
			}

		return gl;
	}

	public static void main(String[] args) {

		System.out.println(bigSqrt(new BigDecimal("213.57")));
		System.out.println(Math.sqrt(213.57));
	}
}
