package com.tommyatkins.design.visitor.demo;

public class DemoTest {

	public static void main(String[] args) {
		Song song = new 時間よとまれ();
		Singer singer  = new Singer();
		singer.sing(song);
	}
}
