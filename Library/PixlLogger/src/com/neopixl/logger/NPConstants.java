package com.neopixl.logger;

public interface NPConstants {

	String UTF8 = "utf-8";

	static interface ManifestMeta {
		//Provider
		String DEPENDENCY_PROVIDER = "neopixl_dependency_provider";
		
		//LogLevel
		String LOG_LEVEL = "neopixl_log_level";
		
		//Type log
		String DISABLE = "disable";
		String VERBOSE = "verbose";
		String DEBUG = "debug";
		String INFO = "info";
		String WARN = "warn";
		String ERROR = "error";
		String ASSERT = "assert";
	}
}
