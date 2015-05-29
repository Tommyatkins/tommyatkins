package com.tommyatkins.design.visitor.demo;

import com.tommyatkins.design.visitor.Subject;
import com.tommyatkins.design.visitor.Vistor;

public class Song implements Subject {

	@Override
	public void accept(Vistor vistor) {
		getSubject().accept(vistor);
	}

	@Override
	public Subject getSubject() {
		return this;
	}

}
