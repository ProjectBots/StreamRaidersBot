package srlib;

import java.util.Hashtable;

import com.google.gson.JsonObject;

import include.Json;
import otherlib.Logger;
import otherlib.Options;
import srlib.store.Store;

public class Reward {
	
	private static JsonObject chest_rews = Json.parseObj(Options.get("rewards"));
	
	public static JsonObject updateChestRews(JsonObject data) {
		chest_rews = data.getAsJsonObject("ChestRewards");
		return chest_rews;
	}
	
	public static final Hashtable<String, String> TYPICAL_CHEST_BASIC_REWARDS = new Hashtable<String, String>() {
		private static final long serialVersionUID = 1L; {
			put("goldbag", Store.gold.get());
			put("eventtoken", "token");
			put("cooldown", Store.meat.get());
			put("potion", Store.potions.get());
			put("epicpotion", Store.potions.get());
	}};
	
	public static Reward genChestReward(String chestReward, String cid, Integer slot) {
		String[] rew = chestReward.split("\\|");
		int amount = chest_rews.getAsJsonObject(rew[0]).get("Quantity").getAsInt();
		String name;
		
		String frew = rew[0].split("_")[0];
		if(TYPICAL_CHEST_BASIC_REWARDS.containsKey(frew)) {
			name = TYPICAL_CHEST_BASIC_REWARDS.get(frew);
		} else if(rew.length == 2) {
			name = rew[1];
		} else if(rew[0].contains("skin")) {
			name = "skin";
		} else {
			Logger.print("Reward -> genChestReward: err=failed to determine reward, reward=" + chestReward, Logger.runerr, Logger.error, cid, slot, true);
			name = "unknown";
		}
		
		return new Reward(name, amount);
	}
	
	

	public final String name;
	public final int amount;
	
	public Reward(String name, int amount) {
		this.name = name;
		this.amount = amount;
	}
	
	
	
}
