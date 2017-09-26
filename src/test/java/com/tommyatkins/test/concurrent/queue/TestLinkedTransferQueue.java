package com.tommyatkins.test.concurrent.queue;

import java.util.Objects;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

public class TestLinkedTransferQueue {

    public static void main(String[] args) throws InterruptedException {
        LinkedTransferQueue<Object> platform = new LinkedTransferQueue<Object>();
        Creater s = new Creater(platform);
        s.setMsg("Hello!");
        Customer c = new Customer(platform);

        new Thread(s).start();

        TimeUnit.SECONDS.sleep(3L);

        new Thread(c).start();

        System.out.println("Main thread end.");

    }
}


class Creater implements Runnable {
    private LinkedTransferQueue<Object> platform;
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Creater(LinkedTransferQueue<Object> platform) {
        Objects.requireNonNull(platform);
        this.platform = platform;
    }

    @Override
    public void run() {
        try {
            while (!platform.hasWaitingConsumer()) {
                System.out.println("No customer is waiting, sleep for 1 second.");
                TimeUnit.SECONDS.sleep(1L);
            }
            System.out.println(String.format("ready to offer msg 【%s】!", msg));
            platform.transfer(msg);
            System.out.println(String.format("【%s】 had been token!", msg));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}


class Customer implements Runnable {
    private LinkedTransferQueue<Object> platform;

    public Customer(LinkedTransferQueue<Object> platform) {
        Objects.requireNonNull(platform);
        this.platform = platform;
    }

    @Override
    public void run() {

        try {
            System.out.println(String.format("had got 【%s】!", platform.take()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
