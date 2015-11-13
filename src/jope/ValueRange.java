package jope;

import java.math.BigInteger;

/**
 *
 * @author Savvas Savvides <savvas@purdue.edu>
 *
 */
public class ValueRange {

	BigInteger start;
	BigInteger end;

	public ValueRange(BigInteger s, BigInteger e) {
		this.start = s;
		this.end = e;

		if (this.start.compareTo(this.end) > 0)
			throw new RuntimeException("start > end");

	}

	/**
	 * Copy constructor
	 *
	 * @param other
	 */
	public ValueRange(ValueRange other) {
		this(other.start, other.end);
	}

	/**
	 * the range length, including its start and end
	 *
	 * @return
	 */
	public BigInteger size() {
		return this.end.subtract(this.start).add(BigInteger.ONE);
	}

	/**
	 * Return a number of bits required to encode any value within the range
	 *
	 * @return
	 */
	public long rangeBitSize() {
		return (long) Math.ceil(Math.log(this.size().longValueExact()) / Math.log(2));
	}

	public boolean contains(BigInteger number) {
		return number.compareTo(this.start) >= 0 && number.compareTo(this.end) <= 0;
	}

}
