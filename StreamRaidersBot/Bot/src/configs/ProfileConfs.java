package configs;

import java.util.Hashtable;

import configs.captain.CaptainConf;
import configs.shared.TimeConf;
import configs.viewer.ViewerConf;
import run.ProfileType;

public class ProfileConfs {
	
	public static final Hashtable<String, ProfileConfs> pconfs = new Hashtable<>();
	
	public String cookies = "";
	public String name = "";
	
	public ViewerConf vconf = new ViewerConf();
	public CaptainConf cconf = null;
	
	public boolean canCaptain = false;
	
	public String syncedViewer = "(none)";
	public String syncedCaptain = "(none)";
	
	
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
	
	public void syncProfiles(String cid, String defcid, ProfileType pt) {
		
	}
	
	
}
