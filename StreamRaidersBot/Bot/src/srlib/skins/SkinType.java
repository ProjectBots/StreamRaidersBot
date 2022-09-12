package srlib.skins;

import java.util.HashMap;

public enum SkinType {
	EPIC("Epic"),FULL("Full"), HEAD("Head"), FLAG_BEARER("Flag Bearer");
	public final String str;
	private SkinType(String str) {
		this.str = str;
	}
	private final static HashMap<String, SkinType> from_string;
	static {
	    from_string = new HashMap<>();
	    for(SkinType value : values()) {
	        from_string.put(value.toString(), value);
	        from_string.put(value.str, value);
	    }
	}
	public static SkinType parseString(String arg) {
		return from_string.get(arg);
	}
}