package com.lvonce.hermes.prepares;

public class Proxy {

    private Target target;
    public Proxy(Object target) {
        this.target = (Target)target;
    }


    public void __setReloadTarget__(Object target) {
        this.target = (Target)target;
    }

    public Object __getReloadTarget__() {
        return this.target;
    }

    public int add (int x, int y) {
        return this.target.add(x, y);
    }
}