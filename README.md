# Hermes
  
This is a hotswap library.
Still in rapid development, comming soon.

## Support:  
	[1] java hotswap.  
	[2] kotlin hotswap.  
	[3] groovy hotswap.  

## Hello World
Given a project structure  
```
example/
├── pom.xml
├── src
│   ├── main
│   │   ├── groovy
│   │   │   └── com
│   │   │       └── lvonce
│   │   │           └── GroovyFoo.groovy
│   │   ├── java
│   │   │   └── com
│   │   │       └── lvonce
│   │   │           ├── App.java
│   │   │           ├── IFoo.java
│   │   │           └── JavaFoo.java
│   │   └── kotlin
│   │       └── com
│   │           └── lvonce
│   │               └── KotlinFoo.kt
│   └── test
│       └── java
│           └── com
│               └── lvonce
│                   └── AppTest.java

```
  
The main class create objects that implement by java/kotlin/groovy.
```java
package com.lvonce;

import static com.lvonce.hermes.EntityFactory.*;

public class App {
    public static void main(String[] args) throws Exception {
		IFoo foo1 = create(IFoo.class, "com.lvonce.JavaFoo");
		IFoo foo2 = create(IFoo.class, "com.lvonce.KotlinFoo");
		IFoo foo3 = create(IFoo.class, "com.lvonce.GroovyFoo");
		Runnable func = new Runnable() {
			public void run() {
				while (true) {
					System.out.println(foo1.hello("msg"));
					System.out.println(foo2.hello("msg"));
					System.out.println(foo3.hello("msg"));
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}	
		};
		new Thread(func).start();
    }
}

```

You can run the example within the example directory by following command:
```
cd example
mvn clean pacakge
java -jar target/example-1.0-SNAPSHOT-jar-with-dependencies.jar
```
And now you can use your favorite editor to modify JavaFoo.java KotlinFoo.kt and GroovyFoo.groovy.
Then you will see the hotswap magic.

## QUICK START
Now you can use maven to integrate hermes with your own project:

```
<dependency>
	<groupId>com.lvonce</groupId>
	<artifactId>hermes</artifactId>
	<version>0.0.3</version>
</dependency>
```

## DESIGN


