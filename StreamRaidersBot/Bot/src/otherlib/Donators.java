package otherlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Donators {

	public static class Don {
		public final String name;
		public final int amount;
		public final String text;
		public Don(String name, int amount, String text) {
			this.name = name;
			this.amount = amount;
			this.text = text;
		}
		public String getEur() {
			int cen = amount%100;
			return (amount/100)+"."+(cen < 10 ? "0"+cen : cen)+" €";
		}
	}
	
	public static final List<Don> dons = Collections.unmodifiableList(new ArrayList<Don>() {
		private static final long serialVersionUID = 1L;
		{
			add(new Don("DJ Lively Pants", 14293, "Thanks for such a great way for me to not waste time during the day! And your community on Discord is fantastic. Thanks BearHugsByDay!"));
			add(new Don("Deus", 7096, "Got to support this awesome project +1 time saver"));
			add(new Don("Skyzor", 5266, "Real men support"));
			add(new Don("Info#5598", 3579, ""));
			add(new Don("Obelisk", 2201, "thanks for taking the time from your schedule to make this :)"));
			add(new Don("Snugsel", 2000, ""));
			add(new Don("CeKay", 1500, "Thx for this amazing - for me - timesaving project 3  cekay.de"));
			add(new Don("candyknack", 1258, ""));
			add(new Don("DeathDriver", 1110, "Thanks for creating this master piece"));
			add(new Don("Noa3", 1000, ""));
			add(new Don("CaptainYesz", 791, ""));
			add(new Don("Kain", 529, ""));
			add(new Don("Chris180", 500, ""));
			add(new Don("Volkoff", 436, "I would like to thank the developer for sharing this tool. Donation is the best way we can do to encourage the same. Help the project."));

		}
	});
	
}
