# Hermes
  
This is a hotswap library.
Still in rapid development, comming soon.

## Support:  
	[1] java hotswap.  
	[2] kotlin hotswap.  
	[3] groovy hotswap.  

## Hello World
```java

package com.lvonce.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.lvonce.hermes.EntityFactory.*;

public class App {
	private static Logger logger = LoggerFactory.getLogger(App.class);
	public static void main(String[] args) throws Throwable {
		IFoo foo1 = create(IFoo.class, "com.lvonce.hermes.Test");
		IFoo foo2 = create(IFoo.class, "Foo");
		IFoo foo3 = create(IFoo.class, "com.lvonce.hermes.Hello", "Test");
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					logger.info(foo1.hello("wang wei"));
					logger.info(foo2.hello("wang wei"));
					logger.info(foo3.hello("wang wei"));
					try { 
						Thread.sleep(3000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}
}

```
You can run the example within the example directory by following command:
```
```

## QUICK START
Now you can use maven to integrate hera with your own project:

```
<dependency>
</dependency>
```

## DESIGN


