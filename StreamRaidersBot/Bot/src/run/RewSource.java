package run;

public enum RewSource {
	CHESTS(0), BOUGHT(1), EVENT(2);
	
	public final byte b;
	private RewSource(int i) {
		b = (byte) i;
	}
}
