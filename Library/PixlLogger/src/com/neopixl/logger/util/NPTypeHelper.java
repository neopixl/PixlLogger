
package com.neopixl.logger.util;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public final class NPTypeHelper {

	public static boolean isByte(Class<?> cls) {
		return (cls == byte.class || cls == Byte.class);
	}

	public static boolean isShort(Class<?> cls) {
		return (cls == short.class || cls == Short.class);
	}

	public static boolean isInteger(Class<?> cls) {
		return (cls == int.class || cls == Integer.class);
	}

	public static boolean isLong(Class<?> cls) {
		return (cls == long.class || cls == Long.class);
	}

	public static boolean isFloat(Class<?> cls) {
		return (cls == float.class || cls == Float.class);
	}

	public static boolean isDouble(Class<?> cls) {
		return (cls == double.class || cls == Double.class);
	}

	public static boolean isBoolean(Class<?> cls) {
		return (cls == boolean.class || cls == Boolean.class);
	}

	public static boolean isCharacter(Class<?> cls) {
		return (cls == char.class || cls == Character.class);
	}

	public static boolean isString(Class<?> cls) {
		return cls == String.class;
	}

	public static boolean isEnum(Class<?> cls) {
		return cls.isEnum();
	}

	public static boolean isUUID(Class<?> cls) {
		return UUID.class.isAssignableFrom(cls);
	}

	public static boolean isDate(Class<?> cls) {
		return Date.class.isAssignableFrom(cls);
	}

	public static boolean isByteArray(Class<?> cls) {
		return cls == byte[].class;
	}

	public static boolean isArray(Class<?> cls) {
		return cls.isArray();
	}

	public static boolean isCollection(Class<?> cls) {
		return Collection.class.isAssignableFrom(cls);
	}

	public static boolean isBitmap(Class<?> cls) {
		return Bitmap.class.isAssignableFrom(cls);
	}

	public static boolean isDrawable(Class<?> cls) {
		return Drawable.class.isAssignableFrom(cls);
	}
	protected NPTypeHelper() {
	}
}
