package configs;

import java.util.ArrayList;
import java.util.Hashtable;

import configs.captain.CaptainConf;
import configs.captain.layers.CaptainLayerConf;
import configs.captain.layers.units.CaptainUnitConf;
import configs.infos.UnitInfo;
import configs.shared.TimeConf;
import configs.viewer.ViewerConf;
import configs.viewer.layers.ViewerLayerConf;
import configs.viewer.layers.units.ViewerUnitConf;
import include.DeepCopyHashtable;
import run.AbstractProfile;
import run.Manager;
import run.ProfileType;

public class ProfileConf {

	public static final String NONE = "(none)";
	
	public static final Hashtable<String, ProfileConf> pconfs = new Hashtable<>();
	
	public String cookies = "";
	public String name = "";
	
	public ViewerConf vconf = new ViewerConf();
	public CaptainConf cconf = null;
	
	public boolean canCaptain = false;
	
	public String syncedViewer = "(none)";
	public String syncedCaptain = "(none)";
	

	/**
	 * 0 chests<br>
	 * 1 bought<br>
	 * 2 event<br>
	 * {@link AbstractProfile#rew_sources}
	 */
	public final Hashtable<Short, Hashtable<String, Integer>> statsViewer = new Hashtable<>(),
															statsCaptain = new Hashtable<>();
	
	
	/**
	 * 1 time unit equals 5 minutes, starting from Monday 00:00 (=0) to Sunday 23:55 (=2015)
	 * 
	 * @param time time to search for (0 - 2015)
	 * @param pt profile type
	 * @return the layer id for the given time and profile type
	 */
	public String getLayerIdByTime(ProfileType pt, int time) {
		//	2016 = 0, just like 24:00 = 00:00
		if(time >= 7*24*60/5)
			throw new IllegalArgumentException("time cannot exceed 2015, time="+time);
		
		TimeConf[] tconfs = pt == ProfileType.VIEWER ? vconf.tconfs : cconf.tconfs;
		
		//	modified binary search
		int s = 0;
		int e = tconfs.length;
		while(true) {
			int mid = (s+e)/2;
			
			if(time >= tconfs[mid].start && (mid+1 > tconfs.length || time < tconfs[mid+1].start))
				return tconfs[mid].lid;
			
			if(time > tconfs[mid].start)
				s = mid+1;
			else
				e = mid-1;
		}
	}
	
	public static class NotACaptainException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
	
	/**
	 * syncs a profile to another.<br>
	 * this function will not notify any profiles.<br>
	 * use {@link Manager#syncProfiles}
	 * @param pid
	 * @param defpid
	 * @param pt
	 */
	public static void syncProfiles(String pid, String defpid, ProfileType pt) {
		final ProfileConf pc = pconfs.get(pid);
		final ProfileConf dpc;
		
		switch (pt) {
		case CAPTAIN:
			if(pc.canCaptain != (defpid == null || pconfs.get(defpid).canCaptain))
				throw new NotACaptainException();
			
			if(defpid == null) {
				dpc = pconfs.get(pc.syncedCaptain);
				pc.syncedCaptain = "(none)";
				
				pc.cconf = pc.cconf.clone();
				
				remUnitsCaptain(pc.cconf.lconfs, defpid, true);
				remUnitsCaptain(dpc.cconf.lconfs, pid, false);
			} else {
				if(!pc.syncedCaptain.equals("(none)"))
					syncProfiles(pid, null, pt);
				
				dpc = pconfs.get(defpid);
				
				//	merge units
				for(String key : dpc.cconf.lconfs.keySet())
					dpc.cconf.lconfs.get(key).uconfs
						.putAllIfAbsent(pc.cconf.lconfs
							.get(pc.cconf.lconfs.containsKey(key)?key:"(default)")
							.uconfs);
				
				pc.cconf = dpc.cconf;
			}
			break;
		case VIEWER:
			if(defpid == null) {
				dpc = pconfs.get(pc.syncedViewer);
				pc.syncedViewer = "(none)";
				
				pc.vconf = pc.vconf.clone();
				
				remUnitsViewer(pc.vconf.lconfs, defpid, true);
				remUnitsViewer(dpc.vconf.lconfs, pid, false);
			} else {
				if(!pc.syncedCaptain.equals("(none)"))
					syncProfiles(pid, null, pt);
				
				dpc = pconfs.get(defpid);

				//	merge units
				for(String key : dpc.vconf.lconfs.keySet())
					dpc.vconf.lconfs.get(key).uconfs
						.putAllIfAbsent(pc.vconf.lconfs
							.get(pc.vconf.lconfs.containsKey(key)?key:"(default)")
							.uconfs);
				
				pc.vconf = dpc.vconf;
			}
			break;
		}
		
	}
	
	
	public void syncUnit(String cid, ProfileType pt, String lid, String unitId, String defUnitId) {
		
		switch(pt) {
		case CAPTAIN:
			DeepCopyHashtable<String, CaptainUnitConf> uconfsc = cconf.lconfs.get(lid).uconfs;
			
			CaptainUnitConf uc = uconfsc.get(unitId);
			
			if(defUnitId == null || defUnitId.equals("(none)")) {
				uc.conf = uc.conf.clone();
				uc.sync = "(none)";
			} else {
				CaptainUnitConf duc = uconfsc.get(defUnitId);
				String dusc = defUnitId;
				if(!duc.sync.equals(NONE)) {
					dusc = duc.sync;
					duc = uconfsc.get(duc.sync);
				}
				
				for(CaptainUnitConf uc_ : uconfsc.values()) {
					if(uc_.sync.equals(unitId)) {
						uc_.conf = duc.conf;
						uc_.sync = dusc;
					}
				}
				
				uc.conf = duc.conf;
				uc.sync = dusc;
			}
			break;
		case VIEWER:
			DeepCopyHashtable<String, ViewerUnitConf> uconfsv = vconf.lconfs.get(lid).uconfs;
			
			ViewerUnitConf uv = uconfsv.get(unitId);
			
			if(defUnitId == null || defUnitId.equals("(none)")) {
				uv.conf = uv.conf.clone();
				uv.sync = "(none)";
			} else {
				ViewerUnitConf duv = uconfsv.get(defUnitId);
				String dusv = defUnitId;
				if(!duv.sync.equals(NONE)) {
					dusv = duv.sync;
					duv = uconfsv.get(duv.sync);
				}
				
				for(ViewerUnitConf uv_ : uconfsv.values()) {
					if(uv_.sync.equals(unitId)) {
						uv_.conf = duv.conf;
						uv_.sync = dusv;
					}
				}
				
				uv.conf = duv.conf;
				uv.sync = dusv;
			}
			break;
		}
		
	}
	
	
	private static void remUnitsCaptain(DeepCopyHashtable<String, CaptainLayerConf> lays, final String pid, boolean invert) {
		for(CaptainLayerConf clc : lays.values()) {
			DeepCopyHashtable<String, CaptainUnitConf> units = clc.uconfs;
			for(String key : new ArrayList<>(units.keySet())) {
				if(!key.matches("\\d+"))
					continue;
				
				int id = Integer.parseInt(key);
				
				if(UnitInfo.uinfos.get(id).pid.equals(pid) == invert)
					continue;
				
				units.remove(key);
			}
		}
	}
	
	private static void remUnitsViewer(DeepCopyHashtable<String, ViewerLayerConf> lays, final String pid, boolean invert) {
		for(ViewerLayerConf vlc : lays.values()) {
			DeepCopyHashtable<String, ViewerUnitConf> units = vlc.uconfs;
			for(String key : new ArrayList<>(units.keySet())) {
				if(!key.matches("\\d+"))
					continue;
				
				int id = Integer.parseInt(key);
				
				if(UnitInfo.uinfos.get(id).pid.equals(pid) == invert)
					continue;
				
				units.remove(key);
			}
		}
	}
	
	
}
