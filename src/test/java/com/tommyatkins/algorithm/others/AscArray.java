package com.tommyatkins.algorithm.others;

import java.util.Objects;

public class AscArray<E extends Comparable<E>> {

	transient AscNode<E> min;

	public AscArray() {
	}

	static class AscNode<E> {
		AscNode<E> previous;
		E item;
		AscNode<E> next;

		AscNode(E x) {
			item = x;
		}
	}

	public void add(E entity) {
		Objects.requireNonNull(entity);
		if (min == null) {
			// first element
			min = new AscNode<E>(entity);
		} else {
			int result = min.item.compareTo(entity);
			if (result < 0) {
				
				
			} else {
			
				
			}
		}
	};

	public boolean contain(E entity) {
		return false;
	}

}
