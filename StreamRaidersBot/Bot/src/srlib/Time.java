package srlib;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Time {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd [HH][H]:mm:ss").withZone(ZoneOffset.UTC);

	private static long secsoff = Long.MIN_VALUE;
	
	public static long parse(String in) {
		return Instant.from(FORMATTER.parse(in)).getEpochSecond();
	}
	
	public static String format(long in) {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC).format(Instant.ofEpochSecond(in));
	}
	
	/**
	 * updates the value that will be returned by {@link #getSecsOff()}
	 * @param cst StreamRaiders server time
	 */
	public static void updateSecsOff(String cst) {
		secsoff = System.currentTimeMillis() / 1000 - parse(cst);
	}
	
	/**
	 * @return the amount off seconds between StreamRaiders's server time and this machine time
	 */
	public static long getSecsOff() {
		return secsoff;
	}
	
	/**
	 * @return the current StreamRaiders server time or 0 if not updated
	 */
	public static long getServerTime() {
		if(secsoff == Long.MIN_VALUE)
			return 0;
		return System.currentTimeMillis() / 1000 - secsoff;
	}
	
	public static boolean isAfterServerTime(long in) {
		return getServerTime() - in < 0;
	}
	
	public static boolean isAfterServerTime(String in) {
		return isAfterServerTime(parse(in));
	}
	
	public static boolean isBeforeServerTime(long in) {
		return getServerTime() - in > 0;
	}
	
	public static boolean isBeforeServerTime(String in) {
		return isBeforeServerTime(parse(in));
	}
	
	public static long plus(String t, int secs) {
		return parse(t) + secs;
	}
	
	public static long minus(String t, int secs) {
		return parse(t) - secs;
	}
	
	/*
	public static LocalDateTime parse(String in) {
		return LocalDateTime.parse(in, formatter);
	}
	
	public static String parse(LocalDateTime in) {
		return in.format(formatter);
	}
	
	public static String plusMinutes(String in, int min) {
		return parse(parse(in).plusMinutes(min));
	}
	
	public static String plusSeconds(String in, int sec) {
		return parse(parse(in).plusSeconds(sec));
	}
	
	public static boolean isAfter(String a, String b) {
		return parse(a).isAfter(parse(b));
	}
	
	public static boolean isAfter(LocalDateTime a, String b) {
		return a.isAfter(parse(b));
	}
	
	public static boolean isAfter(String a, LocalDateTime b) {
		return parse(a).isAfter(b);
	}
	*/
	
}
