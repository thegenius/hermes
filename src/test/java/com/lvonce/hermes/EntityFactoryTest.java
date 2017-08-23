package com.lvonce.hermes;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.BeforeClass;

import static org.testng.Assert.*;
import static com.lvonce.hermes.EntityFactory.*;

public class EntityFactoryTest {

	@Test
	public void testCreate() {
		IFoo foo1 = create(IFoo.class, "com.lvonce.hermes.JavaFoo");
		IFoo foo2 = create(IFoo.class, "com.lvonce.hermes.KotlinFoo");
		IFoo foo3 = create(IFoo.class, "com.lvonce.hermes.GroovyFoo");
		assertEquals(foo1.hello("msg"), "java msg");
		assertEquals(foo2.hello("msg"), "kotlin msg");
		assertEquals(foo3.hello("msg"), "groovy msg");
	}

}
