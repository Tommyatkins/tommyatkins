package com.tommyatkins.design.visitor;

public interface Subject {

	public void accept(Vistor vistor);
	
	public Subject getSubject();
}
