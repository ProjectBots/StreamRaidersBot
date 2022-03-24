package program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Donators {

	public static class Don {
		public final String name;
		public final int amount;
		public final String text;
		public Don(String name, int paypal, int patreon, String text) {
			this.name = name;
			this.amount = paypal + patreon;
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
			add(new Don("DJ Lively Pants", 15000, 0, "Thanks for such a great way for me to not waste time during the day! And your community on Discord is fantastic. Thanks BearHugsByDay!"));
			add(new Don("Skyzor", 5266, 0, "Real men support"));
			add(new Don("Obelisk", 2201, 0, "thanks for taking the time from your schedule to make this :)"));
			add(new Don("Snugsel", 2000, 0, ""));
			add(new Don("CeKay", 1500, 0, "Thx for this amazing - for me - timesaving project 3  cekay.de"));
			add(new Don("candyknack", 0, 1258, ""));
			add(new Don("DeathDriver", 1110, 0, "Thanks for creating this master piece"));
			add(new Don("Noa3", 1000, 0, ""));
			add(new Don("Info#5598", 0, 569, ""));
			add(new Don("CaptainYesz", 0, 791, ""));
			add(new Don("Kain", 0, 529, ""));
			add(new Don("Chris180", 500, 0, ""));
			add(new Don("Volkoff", 436, 0, "I would like to thank the developer for sharing this tool. Donation is the best way we can do to encourage the same. Help the project."));
		}
	});
	
}
