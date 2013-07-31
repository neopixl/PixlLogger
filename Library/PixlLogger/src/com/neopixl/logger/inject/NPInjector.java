package com.neopixl.logger.inject;


import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.view.View;

public class NPInjector {

	public static Context getApplicationContext() {
		return appCtx;
	}

	public static NPInjector get() {
		return Holder.INJECTOR;
	}

	public void setUp(Context ctx) {
		setContext(ctx);
		NPInjectorDelegate.setUp(appCtx);
	}

	public void tearDown() {
		NPInjectorDelegate.tearDown();
		appCtx = null;
	}

	public <T> T getDependency(Context ctx, Class<T> cls)
			throws RuntimeException {
		setContext(ctx);
		return NPDependencyReader.getVal(ctx, cls);
	}

	public void inject(Activity act) {
		setContext(act);
		View root = act.findViewById(android.R.id.content).getRootView();
		delegate.inject(act, root, act);
	}

	public void inject(Service serv) {
		setContext(serv);
		delegate.inject(serv, null, serv);
	}

	public void inject(Context ctx, Object target) {
		setContext(ctx);
		delegate.inject(ctx, null, target);
	}

	public void inject(Dialog dialog, Object target) {
		View root = dialog.findViewById(android.R.id.content).getRootView();
		inject(root, target);
	}

	public void inject(View view, Object target) {
		Context ctx = view.getContext();
		setContext(ctx);
		delegate.inject(ctx, view, target);
	}

	private static volatile Context appCtx;
	private final NPInjectorDelegate delegate;

	static class Holder {
		static final NPInjector INJECTOR = new NPInjector();
	}

	private NPInjector() {
		NPInjectorDelegate fragmentsDelegate = null;
		delegate = (fragmentsDelegate != null) ? fragmentsDelegate
				: new NPInjectorDelegate();
	}

	private static void setContext(Context ctx) {
		if (appCtx == null) {
			appCtx = ctx.getApplicationContext();
		}
	}

	@Retention(RUNTIME)
	@Target(FIELD)
	public static @interface InjectBundleExtra {
		String key();
		boolean optional() default false;
	}

	@Retention(RUNTIME)
	@Target(FIELD)
	public static @interface InjectDependency {
	}

	@Retention(RUNTIME)
	@Target(FIELD)
	public static @interface InjectResource {
		int value();
	}

	@Retention(RUNTIME)
	@Target(FIELD)
	public static @interface InjectSystemService {
		String value() default "";
	}

	@Retention(RUNTIME)
	@Target(FIELD)
	public static @interface InjectView {
		int id() default 0;
		boolean click() default false;
	}

	public static abstract class Ann<T extends Annotation> {

		private final Class<T> cls;

		public Ann(Class<T> cls) {
			this.cls = cls;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof Ann) {
				return cls.equals(((Ann<?>) o).cls);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return cls.hashCode();
		}

		@Override
		public String toString() {
			return cls.getSimpleName();
		}
	}

	public static final class InjectBundleExtraAnn extends InjectAnn<InjectBundleExtra> {

		public final String key;
		public final boolean optional;

		public InjectBundleExtraAnn(InjectBundleExtra annotation) {
			super(InjectBundleExtra.class);
			key = annotation.key();
			optional = annotation.optional();
		}
	}

	public static final class InjectDependencyAnn extends InjectAnn<InjectDependency> {

		public InjectDependencyAnn(InjectDependency annotation) {
			super(InjectDependency.class);
		}
	}

	public static final class InjectViewAnn extends InjectAnn<InjectView> {

		public final int id;
		public final boolean click;

		public InjectViewAnn(InjectView annotation) {
			super(InjectView.class);
			id = annotation.id();
			click = annotation.click();
		}
	}


	public static final class InjectSystemServiceAnn extends
	InjectAnn<InjectSystemService> {

		public final String name;

		public InjectSystemServiceAnn(InjectSystemService annotation) {
			super(InjectSystemService.class);
			name = annotation.value();
		}
	}

	public static final class InjectResourceAnn extends InjectAnn<InjectResource> {

		public final int id;

		public InjectResourceAnn(InjectResource annotation) {
			super(InjectResource.class);
			id = annotation.value();
		}
	}

	public static class InjectAnn<T extends Annotation> extends Ann<T> {

		public InjectAnn(Class<T> cls) {
			super(cls);
		}
	}
}
