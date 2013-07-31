package com.neopixl.logger.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;

import com.neopixl.logger.NPLog;
import com.neopixl.logger.inject.NPInjector.Ann;
import com.neopixl.logger.inject.NPInjector.InjectAnn;
import com.neopixl.logger.inject.NPInjector.InjectBundleExtra;
import com.neopixl.logger.inject.NPInjector.InjectBundleExtraAnn;
import com.neopixl.logger.inject.NPInjector.InjectDependency;
import com.neopixl.logger.inject.NPInjector.InjectDependencyAnn;
import com.neopixl.logger.inject.NPInjector.InjectResource;
import com.neopixl.logger.inject.NPInjector.InjectResourceAnn;
import com.neopixl.logger.inject.NPInjector.InjectSystemService;
import com.neopixl.logger.inject.NPInjector.InjectSystemServiceAnn;
import com.neopixl.logger.inject.NPInjector.InjectView;
import com.neopixl.logger.inject.NPInjector.InjectViewAnn;
import com.neopixl.logger.util.NPReflectionUtils;
import com.neopixl.logger.util.NPResourceUtils;
import com.neopixl.logger.util.NPTypeHelper;

public class NPInjectorDelegate {

	public static void setUp(Context ctx) {
		NPDependencyReader.init(ctx);
	}

	public static void tearDown() {
		NPDependencyReader.tearDown();
	}

	public final void inject(Context ctx, View root, Object target) {
		long start = System.currentTimeMillis();
		final Class<?> cls = target.getClass();

		for (FieldSpec<InjectAnn<?>> spec : FieldSpecBuilder.getInjectSpecs(cls)) {
			try {
				Object val = getVal(ctx, root, target, spec.ann, spec.field);
				if (val != null) {
					NPReflectionUtils.setFieldVal(target, spec.field, val);
				}
			} catch (Throwable e) {
				NPLog.w("Failed to inject " + cls.getSimpleName() + "#"
						+ spec.field.getName() + ": " + e.getMessage());
				NPLog.d(e);
			}
		}
		NPLog.i("Injected into " + cls.getSimpleName() + " in "
				+ (System.currentTimeMillis() - start) + " ms.");
	}

	protected Object getVal(Context ctx, View root, Object target, Ann<?> ann,
			Field field) throws Exception {
		Class<?> annType = ann.getClass();
		Object val = null;
		if (annType == InjectDependencyAnn.class) {
			val = NPDependencyReader.getVal(ctx, field.getType());
		} else if (annType == InjectBundleExtraAnn.class) {
			Bundle data = getIntentExtras(target);
			val = BundleExtraReader.getVal((InjectBundleExtraAnn) ann, data);
		} else if (annType == InjectResourceAnn.class) {
			val = ResourceReader.getVal(ctx, (InjectResourceAnn) ann, field);
		} else if (annType == InjectSystemServiceAnn.class) {
			val = SystemServiceReader.getVal(ctx, (InjectSystemServiceAnn) ann,
					field);
		} else if (annType == InjectViewAnn.class) {
			if (root == null) {
				throw new IllegalArgumentException("Null View.");
			}
			val = ViewAndPreferenceReader.getVal(ctx, root,
					(InjectViewAnn) ann, target, field);
		}
		return val;
	}

	protected Bundle getIntentExtras(Object obj) {
		Bundle data = null;
		if (obj instanceof Activity) {
			data = ((Activity) obj).getIntent().getExtras();
		} else if (obj instanceof Service) {
			// TODO
		}
		return data;
	}

	public static class FieldSpec<AnnType extends Ann<?>> {


		public final Field field;
		public final Class<?> arrCollItemType;

		public final AnnType ann;

		public FieldSpec(Field field, Class<?> arrCollItemType, AnnType ann) {
			this.field = field;
			this.arrCollItemType = arrCollItemType;
			this.ann = ann;
		}
	}

	public final static class AnnBuilder {

		static <T extends Annotation> Ann<T>[] getFieldAnns(Class<?> cls, Field f) {
			return toAnns(f.getAnnotations());
		}

		@SuppressWarnings("unchecked")
		private static <T extends Annotation> Ann<T>[] toAnns(
				Annotation[] annotations) {
			ArrayList<Ann<?>> anns = new ArrayList<Ann<?>>();
			for (Annotation annotation : annotations) {
				Ann<?> ann = toAnn(annotation);
				if (ann != null) {
					anns.add(ann);
				}
			}
			return anns.toArray(new Ann[anns.size()]);
		}

		private static Ann<?> toAnn(Annotation annotation) {
			Class<?> annotationType = annotation.annotationType();
			Ann<?> ann = null;
			// inject
			if (InjectBundleExtra.class == annotationType) {
				ann = new InjectBundleExtraAnn((InjectBundleExtra) annotation);
			} else if (InjectDependency.class == annotationType) {
				ann = new InjectDependencyAnn((InjectDependency) annotation);
			} else if (InjectResource.class == annotationType) {
				ann = new InjectResourceAnn((InjectResource) annotation);
			} else if (InjectSystemService.class == annotationType) {
				ann = new InjectSystemServiceAnn((InjectSystemService) annotation);
			} else if (InjectView.class == annotationType) {
				ann = new InjectViewAnn((InjectView) annotation);
			}

			return ann;
		}
	}

	public final static class FieldSpecBuilder {

		// Inject
		private static final ConcurrentHashMap<Class<?>, FieldSpec<InjectAnn<?>>[]> injectSpecCache = new ConcurrentHashMap<Class<?>, FieldSpec<InjectAnn<?>>[]>();

		@SuppressWarnings("unchecked")
		public static FieldSpec<InjectAnn<?>>[] getInjectSpecs(Class<?> cls) {
			FieldSpec<InjectAnn<?>>[] specs = injectSpecCache.get(cls);
			if (specs == null) {
				ArrayList<FieldSpec<InjectAnn<?>>> list = new ArrayList<FieldSpec<InjectAnn<?>>>();
				List<Field> fields = NPReflectionUtils.listAnnotatedFields(cls);
				for (Field field : fields) {
					for (Ann<?> ann : AnnBuilder.getFieldAnns(cls, field)) {
						if (ann instanceof InjectAnn) {
							list.add(new FieldSpec<InjectAnn<?>>(field, null,
									(InjectAnn<?>) ann));
							break;
						}
					}
				}
				specs = list.toArray(new FieldSpec[list.size()]);
				injectSpecCache.put(cls, specs);
			}
			return specs;
		}
	}



	public static class ResourceReader {

		public static Object getVal(Context ctx, InjectResourceAnn ann, Field field)
				throws Exception {
			Resources res = ctx.getResources();
			Class<?> cls = field.getType();
			Object val = null;
			if (NPTypeHelper.isBoolean(cls)) {
				val = res.getBoolean(ann.id);
			} else if (NPTypeHelper.isInteger(cls)) {
				val = res.getInteger(ann.id);
			} else if (NPTypeHelper.isString(cls)) {
				val = res.getString(ann.id);
			} else if (NPTypeHelper.isDrawable(cls)) {
				val = res.getDrawable(ann.id);
			} else if (NPTypeHelper.isArray(cls)) {
				Class<?> type = cls.getComponentType();
				if (NPTypeHelper.isInteger(type)) {
					val = res.getIntArray(ann.id);
				} else if (NPTypeHelper.isString(type)) {
					val = res.getStringArray(ann.id);
				}
			}
			if (val == null) {
				throw new Exception("Unsupported resource type '" + cls.getName()
						+ "'.");
			} else {
				return val;
			}
		}
	}

	public static class BundleExtraReader {

		static Object getVal(InjectBundleExtraAnn ann, Bundle data) throws Exception {
			Object val = data.get(ann.key);
			if (val == null && !ann.optional) {
				throw new Exception("Bundle missing required key: " + ann.key);
			} else {
				return val;
			}
		}
	}


	public static class SystemServiceReader {

		public static Object getVal(Context ctx, InjectSystemServiceAnn ann, Field field)
				throws Exception {
			String serviceName = ann.name;
			String name = isEmpty(serviceName) ? serviceRegistry.get(field
					.getType()) : serviceName;
			if (name == null) {
				throw new Exception("Unknown service: " + name);
			} else {
				return ctx.getSystemService(name);
			}
		}

		private static final HashMap<Class<?>, String> serviceRegistry = new HashMap<Class<?>, String>();

		static {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("power", "android.os.PowerManager");
			map.put("window", "android.view.WindowManager");
			map.put("layout_inflater", "android.view.LayoutInflater");
			map.put("account", "android.accounts.AccountManager");
			map.put("activity", "android.app.ActivityManager");
			map.put("alarm", "android.app.AlarmManager");
			map.put("notification", "android.app.NotificationManager");
			map.put("accessibility",
					"android.view.accessibility.AccessibilityManager");
			map.put("keyguard", "android.app.KeyguardManager");
			map.put("location", "android.location.LocationManager");
			map.put("country_detector", "android.location.CountryDetector");
			map.put("search", "android.app.SearchManager");
			map.put("sensor", "android.hardware.SensorManager");
			map.put("storage", "android.os.storage.StorageManager");
			map.put("wallpaper", "android.app.WallpaperManager");
			map.put("vibrator", "android.os.Vibrator");
			map.put("statusbar", "android.app.StatusBarManager");
			map.put("connectivity", "android.net.ConnectivityManager");
			map.put("throttle", "android.net.ThrottleManager");
			map.put("updatelock", "android.os.IUpdateLock");
			map.put("wifi", "android.net.wifi.WifiManager");
			map.put("wifip2p", "android.net.wifi.p2p.WifiP2pManager");
			map.put("servicediscovery", "android.net.nsd.NsdManager");
			map.put("audio", "android.media.AudioManager");
			map.put("media_router", "android.media.MediaRouter");
			map.put("phone", "android.telephony.TelephonyManager");
			map.put("clipboard", "android.text.ClipboardManager");
			map.put("input_method", "android.view.inputmethod.InputMethodManager");
			map.put("textservices", "android.view.textservice.TextServicesManager");
			map.put("appwidget", "android.appwidget.AppWidgetManager");
			map.put("backup", "android.app.backup.IBackupManager");
			map.put("dropbox", "android.os.DropBoxManager");
			map.put("device_policy", "android.app.admin.DevicePolicyManager");
			map.put("uimode", "android.app.UiModeManager");
			map.put("download", "android.app.DownloadManager");
			map.put("nfc", "android.nfc.NfcManager");
			map.put("bluetooth", "android.bluetooth.BluetoothAdapter");
			map.put("sip", "android.net.sip.SipManager");
			map.put("usb", "android.hardware.usb.UsbManager");
			map.put("serial", "android.hardware.SerialManager");
			map.put("input", "android.hardware.input.InputManager");
			map.put("display", "android.hardware.display.DisplayManager");
			map.put("scheduling_policy", "android.os.SchedulingPolicyService");
			map.put("user", "android.os.UserManager");

			for (String serviceName : map.keySet()) {
				String clsName = map.get(serviceName);
				try {
					Class<?> cls = Class.forName(clsName);
					serviceRegistry.put(cls, serviceName);
				} catch (ClassNotFoundException e) {
					NPLog.i(clsName + " service not available.");
				}
			}
		}

		public static boolean isEmpty(CharSequence str) {
			return str == null || str.length() == 0;
		}
	}

	public static class ViewAndPreferenceReader {

		public
		static Object getVal(Context ctx, View rootView, InjectViewAnn ann,
				Object target, Field field) throws Exception {
			boolean isView = View.class.isAssignableFrom(field.getType());
			boolean isPreference = Preference.class.isAssignableFrom(field
					.getType());
			if (!isView && !isPreference) {
				throw new Exception("Not a View or Preference '"
						+ field.getType().getName() + "'.");
			}
			int viewOrPrefId = ann.id;
			if (viewOrPrefId == 0) {
				String fieldName = field.getName();
				if (isView) {
					viewOrPrefId = NPResourceUtils.getResourceId(ctx, fieldName);
				} else {
					viewOrPrefId = NPResourceUtils.getStringId(ctx, fieldName);
				}
			}
			Object val;
			if (isView) {
				val = rootView.findViewById(viewOrPrefId);
			} else {
				val = ((PreferenceActivity) ctx).findPreference(ctx
						.getText(viewOrPrefId));
			}
			if (val != null) {
				if (ann.click) {
					if (isView) {
						if (target instanceof View.OnClickListener) {
							((View) val)
							.setOnClickListener((View.OnClickListener) target);
						} else {
							NPLog.w("Failed to set OnClickListener");
						}
					} else {
						boolean done = false;
						Preference pref = (Preference) val;
						if (target instanceof Preference.OnPreferenceClickListener) {
							pref.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) target);
							done = true;
						}
						if (target instanceof Preference.OnPreferenceChangeListener) {
							pref.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) target);
							done = true;
						}
						if (!done) {
							NPLog.w("Failed to set OnPreferenceClickListener or OnPreferenceChangeListener.");
						}
					}
				}
				return val;
			} else {
				throw new Exception("View or Preference not found for id.");
			}
		}
	}
}
