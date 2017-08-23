package com.lvonce;

import static com.lvonce.hermes.EntityFactory.*;

public class App {
    public static void main(String[] args) throws Exception {
		Runnable func = new Runnable() {
			public void run() {
				while (true) {
					IFoo foo1 = create(IFoo.class, "com.lvonce.JavaFoo");
					IFoo foo2 = create(IFoo.class, "com.lvonce.KotlinFoo");
					IFoo foo3 = create(IFoo.class, "com.lvonce.GroovyFoo");
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
