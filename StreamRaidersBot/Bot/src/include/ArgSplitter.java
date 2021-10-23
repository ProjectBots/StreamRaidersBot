package include;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class ArgSplitter {

	
	private Hashtable<String, Arg> args = new Hashtable<>();
	private Arg def = null;
	private int limit = Integer.MAX_VALUE;
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public static interface Arg {
		public void run(String[] args);
	}
	
	public void registerArg(String name, Arg run) {
		args.put(name, run);
	}
	
	public void registerDefArg(Arg run) {
		def = run;
	}
	
	public void check(String[] in) {
		int c = 0;
		List<String> list = new ArrayList<>();
		for(int i=0; i<in.length && c<limit;) {
			if(in[i++].startsWith("-")) {
				String s = in[i-1];
				if(list.contains(s))
					continue;
				c++;
				String[] sargs = new String[0];
				while(i<in.length) {
					if(in[i].startsWith("-"))
						break;
					sargs = add(sargs, in[i++]);
				}
				
				Arg arg = args.get(s.substring(1));
				if(arg != null)
					arg.run(sargs);
				else if(def != null)
					def.run(insert(0, sargs, s.substring(1)));
				
				list.add(s);
			}
		}
	}
	
	
	private static <T>T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
	
	private static <T>T[] insert(int index, T[] arr, T item) {
		return ArrayUtils.insert(index, arr, item);
	}
	
}
