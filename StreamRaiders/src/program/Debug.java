package program;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import include.NEF;

public class Debug {
	
	private static class Scope {
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
	}

	public static final Scope all = new Scope("all general run srlog loop".split(" "));
	public static final Scope general = new Scope("general run".split(" "));
	public static final Scope run = new Scope("run".split(" "));
	public static final Scope srlog = new Scope("srlog".split(" "));
	public static final Scope atm = new Scope("atm".split(" "));
	public static final Scope loop = new Scope("loop".split(" "));
	
	private static JsonArray scopes = new JsonArray();
	private static String path = null;

	public static void setOutputFile(String path) {
		Debug.path = path;
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
						NEF.save(path, NEF.read(path) + "[Debug] added scope " + scs[i]);
					} catch (FileNotFoundException e) {
						try {
							NEF.save(path, "[Debug] added scope " + scs[i]);
						} catch (IOException e1) {
							StreamRaiders.log("Debug -> print: err=failed to save to new file", e1);
						}
					} catch (IOException e) {
						StreamRaiders.log("Debug -> print: err=failed to save to existing file", e);
					}
			}
		}
	}
	
	synchronized public static String print(String in, Scope scope) {
		LocalDateTime now = LocalDateTime.now();
		String ins = "[Debug] [" + now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + "] [" + scope.getScopes()[0] + "] " + in.replace("\n", "\n        ");
		String[] scopes = scope.getScopes();
		for(int i=0; i<scopes.length; i++) {
			if(Debug.scopes.contains(new JsonPrimitive(scopes[i]))) {
				if(path == null)
					System.out.println(ins);
				else
					try {
						NEF.save(path, NEF.read(path) + "\n\n" + ins);
					} catch (FileNotFoundException e) {
						try {
							NEF.save(path, ins);
						} catch (IOException e1) {
							StreamRaiders.log("Debug -> print: err=failed to save to new file", e1);
						}
					} catch (IOException e) {
						StreamRaiders.log("Debug -> print: err=failed to save to existing file", e);
					}
				return in;
			}
		}
		return in;
	}
	
	
}
