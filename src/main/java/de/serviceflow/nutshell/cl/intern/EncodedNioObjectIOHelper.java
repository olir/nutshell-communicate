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
package de.serviceflow.nutshell.cl.intern;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Id;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.KryoDataInput;
import com.esotericsoftware.kryo.io.KryoDataOutput;

import de.serviceflow.nutshell.cl.Lookup;
import de.serviceflow.nutshell.cl.LookupRegistry;
import de.serviceflow.nutshell.cl.nio.EncodedNioStruct;
import de.serviceflow.nutshell.cl.nio.NoTransfer;
import de.serviceflow.nutshell.cl.nio.TransferJPAReference;
import de.serviceflow.nutshell.cl.nio.Transferable;

/**
 * Helps with serialization and deserialization of messages. The library has
 * it's own serialization. But this class is using kryo witch provided variable
 * length encoding.
 * <p>
 * It takes in account public fields of the message type. Supported types are
 * all primitive types and ...
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 084ef77aa8b2b76d509025f57bccbf0aa03410ab $
 * 
 * 
 */
public class EncodedNioObjectIOHelper {
	private static final Logger JLOG = Logger
			.getLogger(EncodedNioObjectIOHelper.class.getName());

	/**
	 * 
	 */
	private static Map<Class<?>, EncodedNioObjectIOHelper> classToHelperMap = new HashMap<Class<?>, EncodedNioObjectIOHelper>();

	/**
	 * 
	 */
	private List<AbstractFieldIOHelper> fieldHelperList = new ArrayList<AbstractFieldIOHelper>();

	/**
	 * 
	 */
	private final ByteBufferOutput bbo = new ByteBufferOutput();
	/**
	 * 
	 */
	private final KryoDataOutput kdo = new KryoDataOutput(bbo);

	/**
	 * 
	 */
	private final ByteBufferInput bbi = new ByteBufferInput();
	/**
	 * 
	 */
	private final KryoDataInput kdi = new KryoDataInput(bbi);

	/*
	 * Under traffic called maybe up to ~100.000 per second. If it is to much it
	 * should be replaced my in-memory reference in composite.
	 * 
	 * @param c
	 * 
	 * @return
	 */
	public static EncodedNioObjectIOHelper getInstance(EncodedNioStruct c) {
		Class<?> cClass = c.getClass();
		EncodedNioObjectIOHelper h = classToHelperMap.get(cClass);
		if (h == null) {
			h = new EncodedNioObjectIOHelper(c);
			classToHelperMap.put(cClass, h);
		}
		return h;
	}

	/**
	 * 
	 * @param o
	 */
	private EncodedNioObjectIOHelper(EncodedNioStruct c) {
		Class<?> messageClass = c.getClass();

		Field fields[] = messageClass.getFields();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if (!field.isAnnotationPresent(NoTransfer.class)) {
				if (field.isAnnotationPresent(TransferJPAReference.class)) {
					Class<?> type = field.getType();
					Field tfields[] = type.getFields();
					Field idfield = null;
					for (Field tfield : tfields) {
						if (tfield.isAnnotationPresent(Id.class)) {
							idfield = tfield;
							break;
						}
					}
					if (idfield != null) {
						Class<?> idtype = idfield.getType();
						if (idtype == Integer.class || idtype == Integer.TYPE) {
							fieldHelperList.add(new JPAReferenceIOHelper(field,
									idfield));
						} else {
							throw new Error(
									"TransferJPAReference failed for class "
											+ messageClass.getName()
											+ " on field "
											+ field.getName()
											+ ": Refer to "
											+ type.getName()
											+ " invalid because Id annotated field "
											+ idfield.getName()
											+ " is not of type int. Unsupported type: "
											+ idtype.getName());
						}
					}
				} else {
					if (JLOG.isLoggable(Level.FINER)) {
						JLOG.finer("." + field.getName()
								+ " found annotation on " + messageClass);
					}

					Class<?> type = field.getType();
					if (type.isArray()) {
						throw new Error("classic arrays not supported. class="
								+ messageClass.getClass().getName() + " field="
								+ field.getName());
						// type = type.getComponentType();
						// if (type.isArray()) {
						// throw new Error(
						// "Multi-demensional arrays not supported. class="
						// + messageClass.getClass().getName()
						// + " field=" + field.getName());
						// }
					}
					if (type == Boolean.class || type == Boolean.TYPE) {
						fieldHelperList.add(new BooleanIOHelper(field));
					} else if (type == Byte.class || type == Byte.TYPE) {
						fieldHelperList.add(new OctetIOHelper(field));
					} else if (type == Short.class || type == Short.TYPE) {
						fieldHelperList.add(new ShortIOHelper(field));
					} else if (type == Integer.class || type == Integer.TYPE) {
						fieldHelperList.add(new IntIOHelper(field));
					} else if (type == Long.class || type == Long.TYPE) {
						fieldHelperList.add(new LongIOHelper(field));
					} else if (type == Float.class || type == Float.TYPE) {
						fieldHelperList.add(new FloatIOHelper(field));
					} else if (type == Double.class || type == Double.TYPE) {
						fieldHelperList.add(new DoubleIOHelper(field));
					} else if (Transferable.class.isAssignableFrom(type)) {
						fieldHelperList.add(new TransferableIOHelper(field));
					} else {
						throw new Error("Field type '" + type.getName()
								+ "' not supported. class="
								+ messageClass.getClass().getName() + " field="
								+ field.getName());
					}
				}
			}
		}
	}

	public void writeObject(EncodedNioStruct m, ByteBuffer out) {
//		if (!out.isDirect()) {
//			throw new Error("Non-direct buffer!");
//		}
		bbo.setBuffer(out /* , out.capacity() */);

		for (AbstractFieldIOHelper h : fieldHelperList) {
			if (h instanceof TransferableIOHelper) {
				bbo.flush();
				((TransferableIOHelper) h).writeField(m, out);
				bbo.setBuffer(out);
			} else {
				h.writeField(m, kdo);
			}
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL2)) {
				try {
					JLOG.log(SessionObject.MSG_TRACE_LEVEL2,
							"write " + h.field.getName() + "=" + h.field.get(m)
									+ " remaining " + out.remaining());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		bbo.flush();
	}

	public void readObject(EncodedNioStruct m, ByteBuffer in) {
//		if (!in.isDirect()) {
//			throw new Error("Non-direct buffer!");
//		}
		bbi.setBuffer(in);

		for (AbstractFieldIOHelper h : fieldHelperList) {
			if (h instanceof TransferableIOHelper) {
				((TransferableIOHelper) h).readField(m, in);
				bbi.setBuffer(in);
			} else {
				h.readField(m, kdi);
			}
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL2)) {
				try {
					JLOG.log(SessionObject.MSG_TRACE_LEVEL2,
							"read " + h.field.getName() + "=" + h.field.get(m)
									+ " remaining " + in.remaining());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static abstract class AbstractFieldIOHelper {
		protected Field field;
		protected boolean isArray;

		protected AbstractFieldIOHelper(Field field) {
			this.field = field;
			isArray = field.getType().isArray();
			if (JLOG.isLoggable(Level.FINER)) {
				JLOG.finer("MessageIOHelper: +field " + field + " isArray="
						+ isArray);
			}
		}

		void writeField(Transferable t, KryoDataOutput out) {
		}

		void readField(Transferable t, KryoDataInput in) {
		}

	}

	private static class OctetIOHelper extends AbstractFieldIOHelper {
		OctetIOHelper(Field field) {
			super(field);
		}

		@Override
		void readField(Transferable t, KryoDataInput in) {
			try {
				field.setByte(t, in.readByte());
			} catch (Exception e) {
				throw new Error("read error on " + t + " at " + field, e);
			}
		}

		@Override
		void writeField(Transferable t, KryoDataOutput out) {
			try {
				out.writeByte(field.getByte(t));
			} catch (Exception e) {
				throw new Error("write error on " + t + " at " + field, e);
			}
		}

	}

	private static class ShortIOHelper extends AbstractFieldIOHelper {
		ShortIOHelper(Field field) {
			super(field);
		}

		@Override
		void readField(Transferable t, KryoDataInput in) {
			try {
				field.setShort(t, in.readShort());
			} catch (Exception e) {
				throw new Error("read error on " + t + " at " + field, e);
			}
		}

		@Override
		void writeField(Transferable t, KryoDataOutput out) {
			try {
				out.writeShort(field.getShort(t));
			} catch (Exception e) {
				throw new Error("write error on " + t + " at " + field, e);
			}
		}

	}

	private static class IntIOHelper extends AbstractFieldIOHelper {
		IntIOHelper(Field field) {
			super(field);
		}

		@Override
		void readField(Transferable t, KryoDataInput in) {
			try {
				field.setInt(t, in.readInt());
			} catch (Exception e) {
				throw new Error("read error on " + t + " at " + field, e);
			}
		}

		@Override
		void writeField(Transferable t, KryoDataOutput out) {
			try {
				out.writeInt(field.getInt(t));
			} catch (Exception e) {
				throw new Error("write error on " + t + " at " + field, e);
			}
		}

	}

	private static class LongIOHelper extends AbstractFieldIOHelper {
		LongIOHelper(Field field) {
			super(field);
		}

		@Override
		void readField(Transferable t, KryoDataInput in) {
			try {
				field.setLong(t, in.readLong());
			} catch (Exception e) {
				throw new Error("read error on " + t + " at " + field, e);
			}
		}

		@Override
		void writeField(Transferable t, KryoDataOutput out) {
			try {
				out.writeLong(field.getLong(t));
			} catch (Exception e) {
				throw new Error("write error on " + t + " at " + field, e);
			}
		}

	}

	private static class FloatIOHelper extends AbstractFieldIOHelper {
		FloatIOHelper(Field field) {
			super(field);
		}

		@Override
		void readField(Transferable t, KryoDataInput in) {
			try {
				field.setFloat(t, in.readFloat());
			} catch (Exception e) {
				throw new Error("read error on " + t + " at " + field, e);
			}
		}

		@Override
		void writeField(Transferable t, KryoDataOutput out) {
			try {
				out.writeFloat(field.getFloat(t));
			} catch (Exception e) {
				throw new Error("write error on " + t + " at " + field, e);
			}
		}
	}

	private static class DoubleIOHelper extends AbstractFieldIOHelper {
		DoubleIOHelper(Field field) {
			super(field);
		}

		@Override
		void readField(Transferable t, KryoDataInput in) {
			try {
				field.setDouble(t, in.readDouble());
			} catch (Exception e) {
				throw new Error("read error on " + t + " at " + field, e);
			}
		}

		@Override
		void writeField(Transferable t, KryoDataOutput out) {
			try {
				out.writeDouble(field.getDouble(t));
			} catch (Exception e) {
				throw new Error("write error on " + t + " at " + field, e);
			}
		}
	}

	private static class BooleanIOHelper extends AbstractFieldIOHelper {
		BooleanIOHelper(Field field) {
			super(field);
		}

		@Override
		void readField(Transferable t, KryoDataInput in) {
			try {
				field.setBoolean(t, in.readBoolean());
			} catch (Exception e) {
				throw new Error("read error on " + t + " at " + field, e);
			}
		}

		@Override
		void writeField(Transferable t, KryoDataOutput out) {
			try {
				out.writeBoolean(field.getBoolean(t));
			} catch (Exception e) {
				throw new Error("write error on " + t + " at " + field, e);
			}
		}

	}

	private static class TransferableIOHelper extends AbstractFieldIOHelper {
		TransferableIOHelper(Field field) {
			super(field);
		}

		void readField(Transferable t, ByteBuffer in) {
			try {
				((Transferable) field.get(t)).readObject(in);
			} catch (Exception e) {
				throw new Error("read error on " + t + " at " + field, e);
			}
		}

		void writeField(Transferable t, ByteBuffer out) {
			try {
				((Transferable) field.get(t)).writeObject(out);
			} catch (Exception e) {
				throw new Error("write error on " + t + " at " + field, e);
			}
		}

	}

	private static class JPAReferenceIOHelper extends AbstractFieldIOHelper {
		protected Field idfield;

		JPAReferenceIOHelper(Field field, Field idfield) {
			super(field);
			this.idfield = idfield;
		}

		@Override
		void readField(Transferable t, KryoDataInput in) {
			try {
				Object entity = field.get(t);
				int id = in.readInt();
				if (entity == null) {
					Class<?> type = field.getType();
					Lookup<?> lookup = LookupRegistry.getInstance().getLookup(
							type);
					if (lookup != null) {
						entity = lookup.lookup(id);
						if (entity == null) {
							JLOG.warning("lookup failed on " + type
									+ " for id " + id);
						}
					} else {
						throw new Error("error on " + t + " at " + field
								+ ": No Lookup registred for " + type);
					}
				}
				if (entity != null) {
					idfield.setInt(entity, id);
				}
				field.set(t, entity);
			} catch (Exception e) {
				throw new Error("read error on " + t + " at " + field, e);
			}
		}

		@Override
		void writeField(Transferable t, KryoDataOutput out) {
			try {
				Object entity = field.get(t);
				int id = idfield.getInt(entity);
				out.writeInt(id);
			} catch (Exception e) {
				throw new Error("write error on " + t + " at " + field, e);
			}
		}

	}

}
