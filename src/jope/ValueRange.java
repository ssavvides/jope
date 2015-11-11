package jope;

/**
 *
 * @author Savvas Savvides <savvas@purdue.edu>
 *
 */
public class ValueRange {

	long start;
	long end;

	public ValueRange(long s, long e) {
		this.start = s;
		this.end = e;

		if (this.start > this.end)
			throw new RuntimeException("start > end");
	}

	/**
	 * Copy constructor
	 *
	 * @param old
	 */
	public ValueRange(ValueRange old) {
		this.start = old.start;
		this.end = old.end;
	}

	/**
	 * Return the range length, including its start and end
	 *
	 * @return
	 */
	public long size() {
		return this.end - this.start + 1;
	}

	/**
	 * Return a number of bits required to encode any value within the range
	 *
	 * @return
	 */
	public int rangeBitSize() {
		return (int) Math.ceil(Math.log(this.size()) / Math.log(2));
	}

	public boolean contains(long number) {
		return number >= this.start && number <= this.end;
	}

	public static void main(String[] args) {
		ValueRange in = new ValueRange((long) -Math.pow(2, 4), (long) Math.pow(2, 5));
		System.out.println(in.size());
	}
}
