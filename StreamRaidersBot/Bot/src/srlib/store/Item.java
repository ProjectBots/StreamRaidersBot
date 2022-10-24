package srlib.store;

import com.google.gson.JsonObject;

public class Item {
	@Override
	public String toString() {
		return new StringBuffer("{")
				.append(uid)
				.append(" ")
				.append(name)
				.append(" ")
				.append(quantity)
				.append(" @")
				.append(price)
				.append(" ")
				.append(purchased ? "purchased (" : "(")
				.append(section)
				.append(")}")
				.toString();
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Item))
			return false;
		return ((Item) obj).uid.equals(uid);
	}
	
	public final String name;
	public final int price;
	public final int quantity;
	public final boolean purchased;
	public final String section;
	public final String uid;
	
	public Item(JsonObject pack, boolean purchased) {
		name = pack.get("Item").getAsString();
		price = pack.get("BasePrice").getAsInt();
		quantity = pack.get("Quantity").getAsInt();
		this.purchased = purchased;
		section = pack.get("Section").getAsString();
		uid = pack.get("Uid").getAsString();
	}
}