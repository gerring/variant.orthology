package org.jax.gweaver.variant.orthology.transaction;

/**
 * Simple object for timing line node adding.
 * 
 * @author gerrim
 *
 */
public class TimeInfo implements AutoCloseable {

	private long start;
	private long stop;
	private long count;
	
	public TimeInfo() {
		this.start = System.currentTimeMillis();
		this.count = 0;
		this.stop = Integer.MIN_VALUE;
	}
	
	public long increment(int ignored) {
		return increment();
	}
	
	public long increment() {
		return ++count;
	}
	
	public void stop() {
		stop = System.currentTimeMillis();
	}
	
	public void close() {
		stop();
	}

	/**
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(long start) {
		this.start = start;
	}

	/**
	 * @return the stop
	 */
	public long getStop() {
		return stop;
	}

	/**
	 * @param stop the stop to set
	 */
	public void setStop(long stop) {
		this.stop = stop;
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(long count) {
		this.count = count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (count ^ (count >>> 32));
		result = prime * result + (int) (start ^ (start >>> 32));
		result = prime * result + (int) (stop ^ (stop >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeInfo other = (TimeInfo) obj;
		if (count != other.count)
			return false;
		if (start != other.start)
			return false;
		if (stop != other.stop)
			return false;
		return true;
	}

	public long getTime() {
		return stop-start;
	}
	
	
}
