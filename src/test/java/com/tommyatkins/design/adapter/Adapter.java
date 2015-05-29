package com.tommyatkins.design.adapter;

public class Adapter extends Source implements Targetable {

	@Override
	public void others() {
		System.out.println("others method..");
	}
	
}
