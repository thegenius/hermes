//package com.lvonce.hermes;

import java.io.IOException;


public class PlayGround {

    public static Boolean func() throws Exception {
        try {
            throw new Exception("");
        } catch (IOException e) {
        }
        return true;
    }

    public static void fun2(Object ...args) {
        System.out.println(args == null);
        System.out.println(args.length);
        System.out.println(args.toString());
    }
    public static void main(String[] args) {
        System.out.println("Hello world");
        fun2();
        Boolean test = null;
        try {
            test = func();
        } catch (Exception e) {
            System.out.println(test);
        }
    }
}