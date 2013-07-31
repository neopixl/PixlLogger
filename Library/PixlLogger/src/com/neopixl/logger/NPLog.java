package com.neopixl.logger;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.neopixl.logger.NPConstants.ManifestMeta;
import com.neopixl.logger.inject.NPInjector;

/**
 * Easy way to log (they are no showed in release mode)
 * @author odemolliens
 * Neopixl
 */
public class NPLog {

	/**
	 * VERBOSE 	Priority constant for the println method; use Log.v.
	 * @param obj
	 */
	public static void v(Object obj) {
		log(VERBOSE, obj);
	}

	/**
	 * DEBUG 	Priority constant for the println method; use Log.d.
	 * @param obj
	 */
	public static void d(Object obj) {
		log(DEBUG, obj);
	}

	/**
	 * INFO 	Priority constant for the println method; use Log.i.
	 * @param obj
	 */
	public static void i(Object obj) {
		log(INFO, obj);
	}

	/**
	 * WARN 	Priority constant for the println method; use Log.w.
	 * @param obj
	 */
	public static void w(Object obj) {
		log(WARN, obj);
	}

	/**
	 * ERROR 	Priority constant for the println method; use Log.e.
	 * @param obj
	 */
	public static void e(Object obj) {
		log(ERROR, obj);
	}

	/**
	 * ASSERT 	Priority constant for the println method.
	 * @param obj
	 */
	public static void wtf(Object obj) {
		log(ASSERT, obj);
	}

	/**
	 * ASSERT 	Priority constant for the println method.
	 */
	public static void wtf() {
		log(ASSERT, "WTF");
	}

	private static final String TAG = "Neopixl";

	private static final int VERBOSE = Log.VERBOSE;
	private static final int DEBUG = Log.DEBUG;
	private static final int INFO = Log.INFO;
	private static final int WARN = Log.WARN;
	private static final int ERROR = Log.ERROR;
	private static final int ASSERT = Log.ASSERT;
	private static final int DISABLE = 1024;

	private static void log(int priority, Object obj) {
		boolean debug = isDebug();
		if (debug || (!debug && priority >= getLogLevel())) {
			String msg;
			if (obj instanceof Throwable) {
				StringWriter sw = new StringWriter();
				((Throwable) obj).printStackTrace(new PrintWriter(sw));
				msg = sw.toString();
			} else {
				msg = String.valueOf(obj);
				if (isEmpty(msg)) {
					msg = "\"\"";
				}
			}
			Log.println(priority, getTag(debug), msg);
		}
	}

	/**
	 * Return true if application is in debug mode
	 * @return	boolean
	 */
	private static boolean isDebug() {
		if (_debug == null) {
			Context ctx = NPInjector.getApplicationContext();
			if (ctx != null) {
				_debug = (ctx.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
			}
		}
		return (_debug != null) ? _debug : true;
	}

	/**
	 * get log level from manifest
	 * @return	int level
	 */
	@SuppressLint("DefaultLocale")
	private static int getLogLevel() {
		if (_logLevel == 0) {
			Context ctx = NPInjector.getApplicationContext();
			if (ctx != null) {
				String logLevelStr = null;
				try {
					Bundle metaData = ctx.getPackageManager()
							.getApplicationInfo(ctx.getPackageName(),
									PackageManager.GET_META_DATA).metaData;
					logLevelStr = metaData.getString(ManifestMeta.LOG_LEVEL)
							.toLowerCase().trim();
				} catch (Exception e) {
					// pass
				}
				if (ManifestMeta.VERBOSE.equals(logLevelStr)) {
					_logLevel = VERBOSE;
				} else if (ManifestMeta.DEBUG.equals(logLevelStr)) {
					_logLevel = DEBUG;
				} else if (ManifestMeta.INFO.equals(logLevelStr)) {
					_logLevel = INFO;
				} else if (ManifestMeta.WARN.equals(logLevelStr)) {
					_logLevel = WARN;
				} else if (ManifestMeta.ERROR.equals(logLevelStr)) {
					_logLevel = ERROR;
				} else if (ManifestMeta.ASSERT.equals(logLevelStr)) {
					_logLevel = ASSERT;
				} else if (ManifestMeta.DISABLE.equals(logLevelStr)) {
					_logLevel = DISABLE;
				} else {
					_logLevel = DISABLE;
					Log.i(TAG,
							"No valid <meta-data android:name=\""
									+ ManifestMeta.LOG_LEVEL
									+ "\" android:value=\"...\"/> in AndroidManifest.xml. Logging disabled.");
				}
			}
		}
		return (_logLevel != 0) ? _logLevel : DISABLE;
	}

	/**
	 * Test if sentence is empty
	 * @param	CharSequence
	 * @return	boolean
	 */
	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}

	private static String getTag(boolean debug) {
		if (debug) {
			StackTraceElement caller = Thread.currentThread().getStackTrace()[5];
			String c = caller.getClassName();
			String className = c.substring(c.lastIndexOf(".") + 1, c.length());
			StringBuilder sb = new StringBuilder(5);
			sb.append(className);
			sb.append(".");
			sb.append(caller.getMethodName());
			sb.append("():");
			sb.append(caller.getLineNumber());
			return sb.toString();
		} else {
			if (_tag == null) {
				Context ctx = NPInjector.getApplicationContext();
				if (ctx != null) {
					_tag = ctx.getPackageName();
				}
			}
			return (_tag != null) ? _tag : TAG;
		}
	}

	private static Boolean _debug;
	private static int _logLevel;
	private static String _tag;

	protected NPLog() {
	}

}