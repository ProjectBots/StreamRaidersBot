package otherlib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonObject;


public class Ressources<C> {
	
	public static class Ressource<T> {
		public final T data;
		public final long keepUntil;
		public Ressource(T data, long keepUntil) {
			this.data = data;
			this.keepUntil = keepUntil;
		}
	}
	
	public static interface RessourceDefault<G> {
		public G def(String path);
	}
	
	private final RessourceDefault<Ressource<C>> def;
	private final Hashtable<String, Ressource<C>> res = new Hashtable<>();
	
	public Ressources(RessourceDefault<Ressource<C>> def, Class<C> c) {
		this.def = def == null ? p -> {throw new RuntimeException("("+c.getSimpleName()+") "+p+" was not found in ressources");} : def;
	}
	
	public C get(String path) {
		if(!res.containsKey(path))
			res.put(path, def.def(path));
		return res.get(path).data;
	}
	
	public void add(String path, C item, long keepUntil) {
		res.put(path, new Ressource<C>(item, keepUntil));
	}
	
	
	private static final Hashtable<Class<?>, Ressources<?>> data = new Hashtable<>();
	
	private static final Timer t = new Timer();
	
	public static <T> T get(String path, Class<T> c) {
		@SuppressWarnings("unchecked")
		Ressources<T> v = (Ressources<T>) data.get(c);
		return v.get(path);
	}
	
	public static <T> void addCategory(Class<T> c, RessourceDefault<Ressource<T>> def) {
		Ressources<T> r = new Ressources<>(def, c);
		data.put(c, r);
	}
	
	public static <T> void addItems(Class<T> c, String[] paths, T[] items, long[] keepUntil) {
		@SuppressWarnings("unchecked")
		Ressources<T> r = (Ressources<T>) data.get(c);
		for(int i=0; i<paths.length; i++)
			r.add(paths[i], items[i], keepUntil[i]);
		
	}
	
	
	
	public static void load() throws IOException {
		//	TODO load some stuff
		
		addCategory(JsonObject.class, p -> {throw new RuntimeException(""+p+" was not found in ressources");});
		
		
		
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				for(Class<?> c : data.keySet()) {
					Ressources<?> rs = data.get(c);
					for(String key : new ArrayList<>(rs.res.keySet())) {
						Ressource<?> r = rs.res.get(key);
						
						if(r.keepUntil == -1)
							continue;
						
						if(now < r.keepUntil)
							continue;
						
						rs.res.remove(key);
					}
					
					
				}
			}
		}, 5*60*1000, 5*60*1000);
	}
	
	public static void save() {
		
		
		
		
		
		t.cancel();
	}

}
