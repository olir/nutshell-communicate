/*
 * Copyright 1999-2015 Oliver Rode http://www.serviceflow.de/nutshell
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.serviceflow.nutshell.cl.nio;

import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Container offering Object offering serialization.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: fbcfd2e7a099d4417e57756090446d0f9f3e5ef0 $
 * 
 * 
 */
public class NioObjectContainer implements Transferable {

	private static Kryo kryoInstance = null;
	
	private Object objective = null;
	
	@Override
	public void writeObject(ByteBuffer out) {
		Output output = new ByteBufferOutput(out);
		getKryo().writeClassAndObject(output, objective);
		output.close();
	}

	@Override
	public void readObject(ByteBuffer in) {
		Input input = new ByteBufferInput(in);
		objective = getKryo().readClassAndObject(input);
		input.close();
	}

	public Object getObjective() {
		return objective;
	}

	public void setObjective(Object objective) {
			this.objective = objective;
	}

	/**
	 * This is using the registration method of the kryo library. 
	 * 
	 * @param c Class
	 * @param id unique id for this class
	 * @see https://github.com/EsotericSoftware/kryo
	 */
	public static void register(Class<?> c, int id) {
		getKryo().register(c, id);
	}

	private static Kryo getKryo() {
		if (kryoInstance==null) {
			kryoInstance = new Kryo();
		}
		return kryoInstance;
	}
	
}
