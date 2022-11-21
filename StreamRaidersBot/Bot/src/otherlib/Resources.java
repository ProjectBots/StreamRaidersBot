package otherlib;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.io.FilenameUtils;


public class Resources {
	
	public static class ResourceCategory<C> {
		private final ResourceReader<C> reader;
		private final Hashtable<String, Resource<C>> resources = new Hashtable<>();
		
		
		public ResourceCategory(ResourceReader<C> reader, Class<C> c) {
			if(c == null || reader == null)
				throw new NullPointerException();
			this.reader = reader;
		}
		
		public C get(String name) {
			return getResource(name).data;
		}
		
		public Resource<C> getResource(String name) {
			if(!resources.containsKey(name)) {
				ResourceTemplate<C> temp = reader.read(folder, name);
				if(temp == null)
					return null;
				resources.put(name, temp.create(name));
			}
			return resources.get(name);
		}
		
		public void put(Resource<C> ressource) {
			resources.put(ressource.name, ressource);
		}
		
		public void save(String name) {
			reader.save(folder, getResource(name));
		}
	}
	
	public static interface ResourceReader<G> {
		public ResourceTemplate<G> read(String folder, String path);
		public void save(String folder, Resource<G> resource);
	}
	
	public static class Resource<T> {
		public final String name;
		public final T data;
		public final long keepUntil;
		public Resource(String name, T data, long keepUntil) {
			this.name = name;
			this.data = data;
			this.keepUntil = keepUntil;
		}
	}
	
	public static class ResourceTemplate<R> {
		public final R data;
		public final int keepFor;
		public ResourceTemplate(R data, int keepFor) {
			this.data = data;
			this.keepFor = keepFor;
		}
		public Resource<R> create(String name) {
			return new Resource<R>(name, data, keepFor > 0 ? System.currentTimeMillis() + keepFor : -1);
		}
	}
	
	public static class UnknownExtensionException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public UnknownExtensionException(String reason) {
			super(reason);
		}
	}
	
	public static class UnknownCategoryException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public UnknownCategoryException(String reason) {
			super(reason);
		}
	}
	
	private static String folder = "";
	public static void setFolder(String path) {
		if(!path.endsWith("/"))
			path += "/";
		Resources.folder = path;
	}
	
	private static final Hashtable<Class<?>, ResourceCategory<?>> rescats = new Hashtable<>();
	private static final Hashtable<String, Class<?>> extensions = new Hashtable<>();
	
	private static long checkAgain = System.currentTimeMillis() + 3*60*1000;
	
	
	public static <T> T get(String name) {
		check();
		ResourceCategory<T> v = getCategory(FilenameUtils.getExtension(name));
		return v.get(name);
	}
	
	public static <T> ResourceCategory<T> getCategory(String extension) {
		if(!extensions.containsKey(extension))
			throw new UnknownExtensionException("unknown extension: "+extension);
		return getCategory(extensions.get(extension));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ResourceCategory<T> getCategory(Class<?> c) {
		if(!rescats.containsKey(c))
			throw new UnknownCategoryException("unknown category: "+c.getName());
		check();
		return (ResourceCategory<T>) rescats.get(c);
	}
	
	public static <T> void set(Resource<T> resource) {
		ResourceCategory<T> v = getCategory(FilenameUtils.getExtension(resource.name));
		v.put(resource);
	}
	
	public static <T> void addCategory(Class<T> c, ResourceReader<T> def, String... extensions) {
		for(int i=0; i<extensions.length; i++)
			Resources.extensions.put(extensions[i], c);
		ResourceCategory<T> r = new ResourceCategory<>(def, c);
		rescats.put(c, r);
	}
	
	public static <T> void set(Class<T> c, Resource<T>[] resources) {
		for(int i=0; i<resources.length; i++)
			set(resources[i]);
	}
	
	public static void check() {
		final long now = System.currentTimeMillis();
		if(checkAgain < now) {
			checkAgain = System.currentTimeMillis() + 3*60*1000;
			for(Class<?> c : rescats.keySet()) {
				ResourceCategory<?> rs = rescats.get(c);
				for(String key : new ArrayList<>(rs.resources.keySet())) {
					Resource<?> r = rs.resources.get(key);
					
					if(r.keepUntil == -1)
						continue;
					
					if(r.keepUntil > now)
						continue;
					
					rs.resources.remove(key);
				}
			}
		}
	}

	/**
	 * loads all files and subfiles in the default folder specified by {@link #setFolder(String)}
	 * @return a list of all file paths with unknown extension
	 */
	public static ArrayList<String> loadAllFilesInFolder() {
		return loadAllFilesInFolder("", new File(folder), new ArrayList<>());
	}
	
	private static ArrayList<String> loadAllFilesInFolder(String parents, File folder, ArrayList<String> unknown) {
		for(File file : folder.listFiles()) {
			final String name = file.getName();
			if(file.isDirectory()) {
				loadAllFilesInFolder(parents+name+"/", file, unknown);
				continue;
			}
			try {
				getCategory(FilenameUtils.getExtension(name)).getResource(parents+name);
			} catch (UnknownExtensionException e) {
				unknown.add(parents+name);
			}
		}
		return unknown;
	}
	
	
	
	
}
