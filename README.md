![Logo](https://raw.github.com/neopixl/PixlLogger/master/Sample/PixlLogger/res/drawable-xxhdpi/small.png ) PixlLogger
==========

The better way to log your Android application ! (tags are setted alone !) 

PixlLogger is an extract of the framework Droid Parts

Screenshot
==========
![Screen1](https://raw.github.com/neopixl/PixlLogger/master/screen_pixllogger_1.png )

How use it ?
==========

1 .  Import [PixlLogger-1.0.0-SNAPSHOT.jar](https://github.com/neopixl/PixlLogger/raw/master/Sample/PixlLogger/libs/PixlLogger-1.0.0-SNAPSHOT.jar "PixlLogger-1.0.0-SNAPSHOT.jar") in your project.

2 .  Add meta-data in your AndroidManifest file:

```xml

<meta-data
android:name="neopixl_dependency_provider"
android:value="com.neopixl.logger.inject.NPDependencyProvider" 
/>

```

3 . Use it

```java
public class TestClass {
 
  @Override
	public void myMethods() {
		NPLog.e("");
	}
}
```

Copyright
==========


	Copyright 2013 Neopixl

	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
	
	file except in compliance with the License. You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software distributed under
	
	the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF 
	
	ANY KIND, either express or implied. See the License for the specific language governing
	
	permissions and limitations under the License.

	A different license may apply to other software included in this package,
	
	including DroidParts. Please consult their respective headers for the terms 
	
	of their individual licenses.

