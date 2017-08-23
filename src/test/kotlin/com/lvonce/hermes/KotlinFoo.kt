package com.lvonce.hermes;

class KotlinFoo : IFoo  {
	override fun hello(msg: String):String {
		return "kotlin " + msg; 
	}
}
