package com.lvonce.hermes;

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;  
import java.lang.reflect.InvocationHandler;  

public class EntityProxy implements InvocationHandler {
	private Object target;

	public EntityProxy(Object target) {
		this.target = target;
	}	

	public Object getProxy() {
		return Proxy.newProxyInstance(
			Thread.currentThread().getContextClassLoader(),
			this.target.getClass().getInterfaces(),
			this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("---- before ---");
		System.out.println("---- call with target +"+target +" ---");
		Object result = method.invoke(this.target, args);
		System.out.println("---- after ---");
		return result;
	}

	public void __setReloadTarget__(Object target) {
		this.target = target;
	}

	public Object __getReloadTarget__() {
		return this.target;
	}
}
