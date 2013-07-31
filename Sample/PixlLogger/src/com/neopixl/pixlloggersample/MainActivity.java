package com.neopixl.pixlloggersample;

import com.neopixl.logger.NPLog;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		NPLog.v("verbose message");
		NPLog.d("debug message");
		NPLog.i("info message");
		NPLog.w("warning message");
		NPLog.e("error message");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
