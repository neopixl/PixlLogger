package com.neopixl.logger.inject;

import static android.content.pm.PackageManager.GET_META_DATA;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.neopixl.logger.NPLog;
import com.neopixl.logger.NPConstants.ManifestMeta;

public class NPDependencyReader {

	private static volatile boolean inited = false;
	private static NPAbstractDependencyProvider dependencyProvider;
	private static HashMap<Class<?>, Method> methodRegistry = new HashMap<Class<?>, Method>();

	static void init(Context ctx) {
		if (!inited) {
			synchronized (NPDependencyReader.class) {
				if (!inited) {
					dependencyProvider = getDependencyProvider(ctx);
					if (dependencyProvider != null) {
						Method[] methods = dependencyProvider.getClass()
								.getMethods();
						for (Method method : methods) {
							methodRegistry.put(method.getReturnType(), method);
						}
					}
					inited = true;
				}
			}
		}
	}

	static void tearDown() {
		dependencyProvider = null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getVal(Context ctx, Class<T> cls)
			throws RuntimeException {
		init(ctx);
		T val = null;
		if (dependencyProvider != null) {
			Method method = methodRegistry.get(cls);
			try {
				int paramCount = method.getGenericParameterTypes().length;
				if (paramCount == 0) {
					val = (T) method.invoke(dependencyProvider);
				} else {
					val = (T) method.invoke(dependencyProvider, ctx);
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"No valid DependencyProvider method for "
								+ cls.getName() + ".", e);
			}
		}
		return val;
	}

	private static NPAbstractDependencyProvider getDependencyProvider(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		String className = null;
		try {
			Bundle metaData = pm.getApplicationInfo(ctx.getPackageName(),
					GET_META_DATA).metaData;
			className = metaData.getString(ManifestMeta.DEPENDENCY_PROVIDER);
		} catch (Exception e) {
			NPLog.d(e);
		}
		if (className == null) {
			NPLog.e("No <meta-data android:name=\""
					+ ManifestMeta.DEPENDENCY_PROVIDER
					+ "\" android:value=\"...\"/> in AndroidManifest.xml.");
			return null;
		}
		if (className.startsWith(".")) {
			className = ctx.getPackageName() + className;
		}
		try {
			Class<?> cls = Class.forName(className);
			Constructor<?> constr = cls.getConstructor(Context.class);
			NPAbstractDependencyProvider adp = (NPAbstractDependencyProvider) constr
					.newInstance(ctx.getApplicationContext());
			return adp;
		} catch (Exception e) {
			NPLog.e("Not a valid dependency provider: " + className);
			NPLog.d(e);
			return null;
		}
	}

}
