package com.rain.test;


public class Test {
    private static int count = 0;
    private static Object lock = new Object();

    public static void main(String[] args) {


        new Thread(()->{
            for (int j = 0; j < 1000; j++) {
                synchronized (lock) {
                    count++;
                }
            }
        }).start();

        new Thread(()->{
            for (int j = 0; j < 1000; j++) {
                synchronized (lock) {
                    count--;
                }
            }
        }).start();

        System.out.println(count);
    }
}
