package com.lvonce.hermes;

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;  
import java.lang.reflect.InvocationHandler;  

public class EntityProxy<T> implements InvocationHandler {
	private T target;

	public EntityProxy(T target) {
		this.target = target;
	}	

	public T getProxy() {
		return (T)Proxy.newProxyInstance(
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

	public void setTarget(T target) {
		this.target = target;
	}

	public T getTarget() {
		return this.target;
	}
}
