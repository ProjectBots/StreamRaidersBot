package include;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Time {

	
private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
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
	
}
