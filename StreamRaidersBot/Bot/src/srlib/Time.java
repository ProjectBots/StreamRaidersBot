package srlib;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Time {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd [HH][H]:mm:ss").withZone(ZoneOffset.UTC);

	private static long secsoff = Long.MIN_VALUE;
	
	/**
	 * parses the specified String and returns the amount of seconds since 1970-01-01 00:00:00<br>
	 * the input has to be in this format: yyyy-MM-dd [HH][H]:mm:ss
	 * @param in String to be parsed
	 * @return the amount of seconds since 1970-01-01 00:00:00
	 */
	public static long parse(String in) {
		return Instant.from(FORMATTER.parse(in)).getEpochSecond();
	}
	
	/**
	 * @param in seconds since 1970-01-01 00:00:00
	 * @return a readable date and time string
	 */
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
	
	/**
	 * @param in seconds since 1970-01-01 00:00:00.
	 * @return true if in is greater than server time
	 */
	public static boolean isAfterServerTime(long in) {
		return getServerTime() < in;
	}
	
	/**
	 * @param in seconds since 1970-01-01 00:00:00.
	 * @return true if in is greater than server time
	 */
	public static boolean isAfterServerTime(String in) {
		return isAfterServerTime(parse(in));
	}
	
	/**
	 * @param in seconds since 1970-01-01 00:00:00.
	 * @return true if in is less than server time
	 */
	public static boolean isBeforeServerTime(long in) {
		return getServerTime() > in;
	}
	
	/**
	 * @param in seconds since 1970-01-01 00:00:00.
	 * @return true if in is less than server time
	 */
	public static boolean isBeforeServerTime(String in) {
		return isBeforeServerTime(parse(in));
	}
	
	/**
	 * parses t and adds secs' seconds
	 * @param t String to be parsed
	 * @param secs seconds to be added
	 * @return the result
	 */
	public static long plus(String t, int secs) {
		return parse(t) + secs;
	}
	
	/**
	 * parses t and subtracts secs' seconds
	 * @param t String to be parsed
	 * @param secs seconds to be subtracted
	 * @return the result
	 */
	public static long minus(String t, int secs) {
		return parse(t) - secs;
	}
	
	
}
