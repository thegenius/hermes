# Hermes
[![travis-ci](https://www.travis-ci.org/thegenius/hermes.svg?branch=master)](https://travis-ci.org/thegenius/hermes)
[![codecov](https://codecov.io/gh/thegenius/hermes/branch/master/graph/badge.svg)](https://codecov.io/gh/thegenius/hermes)
[![maven-central](https://img.shields.io/badge/maven-0.0.3-green.svg)](http://search.maven.org/#search%7Cga%7C1%7Chera)
[![apache-license](https://img.shields.io/badge/license-Apache--2.0-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)  

This is a hotswap library.  
Still in rapid development, welcome to join us.

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
│   │   ├── java
│   │   │   └── com
│   │   │       └── lvonce
│   │   │           ├── App.java
│   │   │           ├── IFoo.java
│   │               └── JavaFoo.java
│   └── test
│       └── java
│           └── com
│               └── lvonce
│                   └── AppTest.java

```
  
The main class create objects that implement by java/kotlin/groovy.
```java
package com.lvonce;

import static com.lvonce.hermes.Hermes.*;

public class App {
    public static void main(String[] args) throws Exception {
        while (true) {
            try {
                Thread.sleep(1000);
                Object foo1 = create("com.lvonce.JavaFoo");
                System.out.println(invoke(foo1, "hello", "msg"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }	
    }
}

```

You can run the example within the example directory by following command:
```
cd example
mvn clean pacakge
java -jar target/example-1.0-SNAPSHOT-jar-with-dependencies.jar
```
And now you can use your favorite editor to modify JavaFoo.java.
Then you will see the hotswap magic.

## QUICK START
Now you can use maven to integrate hermes with your own project:

```
<dependency>
	<groupId>com.lvonce</groupId>
	<artifactId>hermes</artifactId>
	<version>0.0.4</version>
</dependency>
```

## API
```
    public static Object create(String className, Object... args)
    public static Object invoke(Object target, String methodName, Object... args)
```

## DESIGN


