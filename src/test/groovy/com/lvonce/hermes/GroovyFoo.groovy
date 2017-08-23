package com.lvonce.hermes;

import com.lvonce.hermes.IFoo;

class GroovyFoo implements IFoo {
	public String hello(String msg) {
		return "groovy " + msg;
	}
}
