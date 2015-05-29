package com.tommyatkins.design.visitor.demo;

import com.tommyatkins.design.visitor.Subject;
import com.tommyatkins.design.visitor.Vistor;

public class Singer implements Vistor {

	@Override
	public void visit(Subject subject) {
		subject.accept(this);
	}

	public void sing(Song song) {
		this.visit(song);
	}

}
