package de.serviceflow.nutshell.cl.intern;

import com.esotericsoftware.kryo.Kryo;

public class KryoFactory {
	private static Kryo kryoInstance = null;

	public static Kryo getKryo() {
		if (kryoInstance == null) {
			kryoInstance = new Kryo();
		}
		return kryoInstance;
	}

	/**
	 * This is using the registration method of the kryo library.
	 * 
	 * @param c
	 *            Class
	 * @param id
	 *            unique id for this class
	 * @see https://github.com/EsotericSoftware/kryo
	 */
	public static void register(Class<?> c, int id) {
		KryoFactory.getKryo().register(c, id);
	}

}