package com.lvonce.hermes.prepares;

public class Target {
    int base;
    public Target(int x) {
        this.base = x;
    }

    public int add(int x, int y) {
        return this.base + x + y;
    }

}