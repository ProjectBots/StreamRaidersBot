package program;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;

import include.ArgSplitter;
import include.NEF;
import include.ArgSplitter.Arg;

public class Options {
	
	private static String op = "data/opt.txt";
	
	private static Hashtable<String, String> opt = null;
	
	public static String get(String key) {
		return opt.get(key);
	}
	
	synchronized public static void set(String key, String value) {
		opt.put(key, value);
	}
	
	synchronized public static void save() {
		try {
			NEF.saveOpt(op, opt);
		} catch (IOException e) {
			Debug.printException("Options -> save: err=failed to save options", e, Debug.runerr, Debug.error, null, null, true);
		}
	}
	
	
	private static HashSet<String> args = new HashSet<>();
	
	public static boolean is(String conf) {
		return args.contains(conf);
	}
	
	public static void load() throws IOException {
		opt = NEF.getOpt(op);
		
		ArgSplitter as = new ArgSplitter();
		as.registerArg("debug", new Arg() {
			@Override
			public void run(String[] args) {
				int i = 0;
				if(args[0].startsWith("=")) {
					String s = args[0].substring(1);
					Debug.setOutputDirectory(s);
					i++;
				}
				
				if(i == args.length) 
					Debug.addScope("general");
				else 
					for(; i<args.length; i++)
						Debug.addScope(args[i]);
			}
		});
		as.registerDefArg(new Arg() {
			@Override
			public void run(String[] arg) {
				args.add(arg[0]);
			}
		});
		as.check(NEF.read("data/args.txt").split("(\n| )"));
	}
	
	
	
}
