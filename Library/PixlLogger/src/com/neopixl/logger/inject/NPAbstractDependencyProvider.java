package com.neopixl.logger.inject;

import android.content.Context;

public abstract class NPAbstractDependencyProvider {

	protected final Context ctx;

	public NPAbstractDependencyProvider(Context ctx) {
		this.ctx = ctx.getApplicationContext();
	}

}
