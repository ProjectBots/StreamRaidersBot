package program;

import static org.apache.commons.io.comparator.LastModifiedFileComparator.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import include.NEF;

public class Debug {
	
	public static class Scope {
		private static Scope[] scopes = new Scope[0];
		
		public static String[] isScope(String scope) {
			for(int i=0; i<scopes.length; i++) {
				String[] scs = scopes[i].getScopes();
				if(scs[0].equals(scope))
					return scs;
			}
			return null;
		}
		
		private String[] scope;
		
		public Scope(String[] scopes) {
			this.scope = scopes;
			Scope.scopes = add(Scope.scopes, this);
		}
		public String[] getScopes() {
			return scope;
		}
		
		private static Scope[] add(Scope[] arr, Scope item) {
			Scope[] arr2 = new Scope[arr.length + 1];
			System.arraycopy(arr, 0, arr2, 0, arr.length);
			arr2[arr.length] = item;
			return arr2;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Scope && Arrays.equals(scope, ((Scope) obj).getScopes())) 
				return true;
			return false;
		}
	}

	public static final Scope all = new Scope("all general run srlog srerr loop runerr lowerr caps units".split(" "));
	public static final Scope general = new Scope("general run runerr lowerr".split(" "));
	public static final Scope run = new Scope("run runerr lowerr".split(" "));
	public static final Scope srlog = new Scope("srlog srerr".split(" "));
	public static final Scope srerr = new Scope("srerr".split(" "));
	public static final Scope loop = new Scope("loop".split(" "));
	public static final Scope runerr = new Scope("runerr".split(" "));
	public static final Scope lowerr = new Scope("lowerr".split(" "));
	public static final Scope caps = new Scope("caps".split(" "));
	public static final Scope units = new Scope("units".split(" "));
	
	
	public static class Type {
		private String type;
		public Type(String type) {
			this.type = type;
		}
		public String get() {
			return type;
		}
	}
	
	public static final List<String> severty = Arrays.asList("info warn error fatal".split(" "));
	private static int min_severty = 0;
	
	public static void setMinSeverty(Type type) {
		min_severty = severty.indexOf(type.get());
	}
	
	public static int getSevertyOf(Type type) {
		return severty.indexOf(type.get());
	}
	
	public static final Type info = new Type(severty.get(0));
	public static final Type warn = new Type(severty.get(1));
	public static final Type error = new Type(severty.get(2));
	public static final Type fatal = new Type(severty.get(3));
	
	
	private static JsonArray scopes = new JsonArray();
	private static String path = null;

	private static int maxDebugFiles = 5;
	
	
	public static void setOutputDirectory(String path) {
		Debug.path = path.replace("\\", "/") + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".txt";
		File[] files = new File(path).listFiles();
		if(files != null && files.length >= maxDebugFiles) {
			Arrays.sort(files, LASTMODIFIED_COMPARATOR);
			files[0].delete();
		}
	}
	
	
	
	synchronized public static void addScope(String scope) {
		
		String[] scs = Scope.isScope(scope);
		if(scs == null) {
			System.err.println("[Debug] \"" + scope + "\" is not a scope");
			return;
		}
		
		for(int i=0; i<scs.length; i++) {
			if(!scopes.contains(new JsonPrimitive(scs[i]))) {
				scopes.add(scs[i]);
				
				if(path == null)
					System.out.println("[Debug] added scope " + scs[i]);
				else
					try {
						NEF.save(path, "\n[Debug] added scope " + scs[i], true);
					} catch (IOException e) {
						System.out.println("Debug -> print: err=failed to save to file");
						e.printStackTrace();
					}
			}
		}
	}
	
	public static interface DebugEventHandler {
		public default void onPrintLine(String pre, String msg, Scope scope, Type type, boolean forced) {
			System.out.println(pre+msg);
		};
		public default void onPrintException(String pre, String msg, Exception e, Scope scope, Type type, boolean forced) {
			System.err.println(pre+msg+"\n"+except2Str(e));
		};
		public default void onWriteLine(String path, String pre, String msg, Scope scope, Type type, boolean forced) {
			try {
				NEF.save(path, "\n\n" + pre+msg, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
		public default void onWriteException(String path, String pre, String msg, Exception e, Scope scope, Type type, boolean forced) {
			try {
				NEF.save(path, "\n\n" + pre+msg+"\n"+except2Str(e), true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		};
	}
	
	private static DebugEventHandler deh = new DebugEventHandler() {};
	
	public static void setDebugEventHandler(DebugEventHandler deh) {
		Debug.deh = deh;
	}
	
	public static String print(String in, Scope scope, Type type) {
		return print(in, scope, type, false);
	}
	
	synchronized public static String print(String msg, Scope scope, Type type, boolean force) {
		LocalDateTime now = LocalDateTime.now();
		String pre = "[" + type.get() + "] [" + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + "] [" + scope.getScopes()[0] + "] ";
		if(should(scope, type) || force) {
			if(path == null)
				deh.onPrintLine(pre, msg.replace("\n", "\n\u0009"), scope, type, force);
			else
				deh.onWriteLine(path, pre, msg, scope, type, force);
		}
		return msg;
	}
	
	public static void printException(String msg, Exception e, Scope scope, Type type) {
		printException(msg, e, scope, type, false);
	}
	
	public static void printException(String msg, Exception e, Scope scope, Type type, boolean force) {
		LocalDateTime now = LocalDateTime.now();
		String pre = "[" + type.get() + "] [" + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + "] [" + scope.getScopes()[0] + "] ";
		if(should(scope, type) || force) {
			if(path == null)
				deh.onPrintException(pre, msg, e, scope, type, force);
			else
				deh.onWriteException(path, pre, msg, e, scope, type, force);
		}
	}
	
	
	private static boolean should(Scope scope, Type type) {
		if(scope == null || type == null)
			return false;
		
		if(severty.indexOf(type.get()) < min_severty)
			return false;
		
		String mscope = scope.getScopes()[0];
		if(Debug.scopes.contains(new JsonPrimitive(mscope))) {
			return true;
		}
		return false;
	}
	
	
	
	public static String except2Str(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
}
