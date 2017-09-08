package com.lvonce.hermes.prepares;

public class TargetNew {
    int base;
    public TargetNew(int x) {
        this.base = x;
    }

    public int add(int x, int y) {
        return this.base * x * y;
    }

}