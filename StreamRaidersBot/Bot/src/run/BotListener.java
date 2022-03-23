package run;

import java.awt.Color;

import com.google.gson.JsonObject;

import program.Raid;

public interface BotListener {
	/**
	 * will be called while initialization if configsV2.json
	 * is not readable or in a wrong format
	 * @return true for trying to reset the config file or false for canceling initialization
	 */
	public boolean configNotReadable();
	
	/**
	 * will be called if SR updates it's data path
	 * @param dataPath the url of it
	 * @param serverTime the current time of the server
	 * @param data the content
	 */
	public default void onDataPathUpdate(String dataPath, String serverTime, JsonObject data) {};

	/**
	 * will be called while loading profiles.
	 * @param loaded amount of profiles loaded
	 * @param failed amount of profiles failed to load
	 * @param total total amount of profiles
	 */
	public default void onConfigLoadStatusUpdate(int loaded, int failed, int total) {};
	
	/**
	 * will be called if a new profile has been added
	 * @param cid profile id
	 */
	public default void onProfileAdded(String cid) {}
	
	/**
	 * will be called if a profile has been deleted
	 * @param cid profile id
	 */
	public default void onProfileRemoved(String cid) {}
	
	/**
	 * will be called if a profile starts getting loaded
	 * @param cid profile id
	 */
	public default void onProfileStartedLoading(String cid) {}
	
	/**
	 * will be called after a profile has been successfully loaded
	 * @param cid profile id
	 * @param pos a instance unique number assigned to the profile starting from 0 and counting up. Can change when restarted, but keeps the order in which the profiles where added.
	 */
	public default void onProfileLoadComplete(String cid, int pos) {}
	
	/**
	 * will be called if a profile failed to load
	 * @param cid profile id
	 * @param pos a instance unique number assigned to the profile starting from 0 and counting up. Can change when restarted, but keeps the order in which the profiles where added.
	 * @param e Exception thrown [{@link program.Run.SilentException}, {@link program.SRR.NotAuthorizedException}, {@link program.SRR.NotAuthorizedException}, {@link program.SRR.OutdatedDataException}, {@link java.lang.Exception}]
	 */
	public default void onProfileLoadError(String cid, int pos, Exception e) {}
	
	/**
	 * will be called if a profile has been unloaded
	 * @param cid profile id
	 */
	public default void onProfileUnloaded(String cid) {}
	
	/**
	 * will be called when a slot of a profile changes between running and stopping
	 * @param cid profile id
	 * @param slot
	 * @param run true => slot is now running, false => slot stopped
	 */
	public default void onProfileChangedRunning(String cid, int slot, boolean run) {}
	
	/**
	 * will be called when the sleep timer updates (every second while profile is sleeping)
	 * @param cid profile id
	 * @param slot
	 * @param time time until next round, already formated in mm:ss format, mm: will be omitted if time < 60s
	 */
	public default void onProfileTimerUpdate(String cid, int slot, String time) {}
	
	/**
	 * will be called if a slot updates
	 * @param cid profile id
	 * @param slot
	 * @param raid the current raid in this slot, null if empty
	 * @param locked if the slot is marked as locked
	 * @param change if the slot is marked to change raid ASAP
	 */
	public default void onProfileUpdateSlot(String cid, int slot, Raid raid, boolean locked, boolean change) {}
	
	/**
	 * will be called if the sync status of a slot updates
	 * @param cid profile id
	 * @param slot this slot
	 * @param slotSyncedTo the slot this slot is synced to, -1 if none
	 */
	public default void onProfileUpdateSlotSync(String cid, int slot, int slotSyncedTo) {}
	
	/**
	 * will be called if a currency is updated
	 * @param cid profile id
	 * @param type currency id
	 * @param amount
	 */
	public default void onProfileUpdateCurrency(String cid, String type, int amount) {}
	
	/**
	 * will be called if the profile is updated
	 * @param cid profile id
	 * @param pn profile name
	 * @param ln current layer name
	 * @param lc current layer color
	 */
	public default void onProfileUpdateGeneral(String cid, String pn, String ln, Color lc) {}
	
}
