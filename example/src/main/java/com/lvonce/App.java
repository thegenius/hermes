package com.lvonce;

import static com.lvonce.hermes.Hermes.*;

public class App {
    public static void main(String[] args) throws Exception {
		Runnable func = new Runnable() {
			public void run() {
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
		};
		new Thread(func).start();
    }
}
