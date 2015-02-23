package de.serviceflow.nutshell.cl;

import java.util.HashMap;
import java.util.Map;

public class LookupRegistry {
	private static LookupRegistry r;
	
	private final Map<Class<?>, Lookup<?>> lookupMap = new HashMap<Class<?>, Lookup<?>>();
	
	public static LookupRegistry getInstance() {
		if (r ==  null) {
			r = new LookupRegistry();
		}
		return r;
	}
	
	public Lookup<?> getLookup(Class<?> type) {
		return lookupMap.get(type);
	}
	
	public void register(Class<?> type, Lookup<?> l) {
		lookupMap.put(type, l);
	}
}
