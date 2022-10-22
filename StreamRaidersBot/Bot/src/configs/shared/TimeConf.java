package configs.shared;

public class TimeConf implements Comparable<TimeConf> {
	@Override
	public int compareTo(TimeConf o) {
		return o.start - this.start;
	}
	
	public final String lid;
	public final int start;
	public TimeConf(String lid, int start) {
		this.lid = lid;
		this.start = start;
	}
}
