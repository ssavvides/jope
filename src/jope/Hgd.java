package jope;

public class Hgd {

	public static double prngDraw(Coins coins) {

		long out = 0;
		for (int i = 0; i < 32; i++)
			out += coins.next() ? Math.pow(2, i) : 0;

		return out / (Math.pow(2, 32) - 1);
	}

	/**
	 * Calculates logarithm of i factorial: ln(i!) Uses Stirling's approximation to do so.
	 */
	private static double afc(int i) {

		if (i < 0)
			throw new RuntimeException("value less than 0");
		else if (i == 0)
			return 0;

		double frac12 = 1.0 / 12;
		double frac360 = 1.0 / 360;
		double fracPi = 0.5 * Math.log(2 * Math.PI);

		return (i + 0.5) * Math.log(i) - i + frac12 / i - frac360 / i / i / i + fracPi;
	}

	public static long rhyper(long kk, long nn1, long nn2, Coins coins) {

		if (kk > 10)
			return hypergeometricHrua(coins, nn1, nn2, kk);
		else
			return hypergeometricHyp(coins, nn1, nn2, kk);

	}

	private static long hypergeometricHyp(Coins coins, long good, long bad, long sample) {
		long d1 = bad + good - sample;
		double d2 = Math.min(bad, good);

		double Y = d2;
		long K = sample;

		while (Y > 0) {
			double U = prngDraw(coins);
			Y -= (int) (Math.floor(U + Y / (d1 + K)));
			K -= 1;

			if (K == 0)
				break;
		}

		long Z = (long) (d2 - Y);
		if (good > bad)
			Z = sample - Z;

		return Z;
	}

	private static long hypergeometricHrua(Coins coins, long good, long bad, long sample) {
		double D1 = 1.7155277699214135;
		double D2 = 0.8989161620588988;

		long mingoodbad = Math.min(good, bad);
		long popsize = good + bad;
		long maxgoodbad = Math.max(good, bad);
		long m = Math.min(sample, popsize - sample);
		double d4 = (double) mingoodbad / popsize;
		double d5 = 1.0 - d4;
		double d6 = m * d4 + 0.5;
		double d7 = Math.sqrt((popsize - m) * sample * d4 * d5 / (popsize - 1) + 0.5);
		double d8 = D1 * d7 + D2;
		double d9 = (long) (Math.floor((m + 1) * (mingoodbad + 1) / (popsize + 2)));
		double d10 = loggam(d9 + 1) + loggam(mingoodbad - d9 + 1) + loggam(m - d9 + 1)
				+ loggam(maxgoodbad - m + d9 + 1);
		double d11 = Math.min(Math.min(m, mingoodbad) + 1.0, Math.floor(d6 + 16 * d7));

		long Z = 0;
		while (true) {
			double X = prngDraw(coins);
			double Y = prngDraw(coins);

			System.out.println(X + " " + Y);

			double W = d6 + d8 * (Y - 0.5) / X;

			if (W < 0.0 || W >= d11)
				continue;

			Z = (long) Math.floor(W);
			double T = d10
					- (loggam(Z + 1) + loggam(mingoodbad - Z + 1) + loggam(m - Z + 1) + loggam(maxgoodbad
							- m + Z + 1));

			if ((X * (4.0 - X) - 3.0) <= T)
				break;

			if (X * (X - T) >= 1)
				continue;

			if (2.0 * Math.log(X) <= T)
				break;
		}

		if (good > bad)
			Z = m - Z;

		if (m < sample)
			Z = good - Z;

		return Z;
	}

	private static double loggam(double x) {

		double[] a = new double[] { 8.333333333333333e-02, -2.777777777777778e-03,
				7.936507936507937e-04, -5.952380952380952e-04, 8.417508417508418e-04,
				-1.917526917526918e-03, 6.410256410256410e-03, -2.955065359477124e-02,
				1.796443723688307e-01, -1.39243221690590e+00 };

		x *= 1.0;
		double x0 = x;
		int n = 0;

		if (x == 1.0 || x == 2.0)
			return 0.0;
		else if (x <= 7.0) {
			n = (int) (7 - x);
			x0 = x + n;
		}

		double x2 = 1.0 / (x0 * x0);
		double xp = 2 * Math.PI;
		double gl0 = a[9];

		for (int k = 8; k >= 0; k--) {
			gl0 *= x2;
			gl0 += a[k];
		}

		double gl = gl0 / x0 + 0.5 * Math.log(xp) + (x0 - 0.5) * Math.log(x0) - x0;

		if (x <= 7.0)
			for (int k = 1; k <= n + 1; k++) {
				gl -= Math.log(x0 - 1.0);
				x0 -= 1.0;
			}

		return gl;
	}

	public static void main(String[] args) {

	}
}
