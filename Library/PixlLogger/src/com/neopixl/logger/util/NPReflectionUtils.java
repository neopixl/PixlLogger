
package com.neopixl.logger.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.neopixl.logger.NPLog;

public final class NPReflectionUtils {

	public static <ValType> ValType getFieldVal(Object obj, Field field)
			throws IllegalArgumentException {
		try {
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			ValType val = (ValType) field.get(obj);
			return val;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void setFieldVal(Object obj, Field field, Object val)
			throws IllegalArgumentException {
		try {
			field.setAccessible(true);
			field.set(obj, val);
		} catch (Exception e) {
			String valClsName = (val != null) ? val.getClass().getSimpleName()
					: "?";
			NPLog.w("Error assigning <" + valClsName + "> " + val + " to ("
					+ field.getType().getSimpleName() + ") field "
					+ obj.getClass().getSimpleName() + "#" + field.getName()
					+ ": " + e.getMessage());
			throw new IllegalArgumentException(e);
		}
	}

	public static Class<?> classForName(String clsName)
			throws IllegalArgumentException {
		try {
			return Class.forName(clsName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static <InstanceType> InstanceType instantiate(
			Class<InstanceType> cls) throws IllegalArgumentException {
		try {
			return cls.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static Enum<?> instantiateEnum(Class<?> enumClass, String enumStr) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Enum en = Enum.valueOf(enumClass.asSubclass(Enum.class), enumStr);
		return en;
	}

	public static List<Field> listAnnotatedFields(Class<?> cls) {
		ArrayList<Class<?>> clsTree = new ArrayList<Class<?>>();
		boolean enteredDroidParts = false;
		do {
			clsTree.add(0, cls);
			boolean inDroidParts = cls.getName().startsWith("org.droidparts");
			if (enteredDroidParts && !inDroidParts) {
				break;
			} else {
				enteredDroidParts = inDroidParts;
				cls = cls.getSuperclass();
			}
		} while (cls != null);
		ArrayList<Field> fields = new ArrayList<Field>();
		for (Class<?> c : clsTree) {
			for (Field f : c.getDeclaredFields()) {
				if (f.getAnnotations().length > 0) {
					fields.add(f);
				}
			}
		}
		return fields;
	}

}
