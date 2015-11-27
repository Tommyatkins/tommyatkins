package com.tommyatkins.test.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Generic extends Base<String> {
	public static void main(String[] args) {
		Generic c = new Generic();
		System.out.println(c.getGenericType(0).getName());
	}

	Object array;

	public Generic() {
		array = Array.newInstance(getGenericType(0), 100);
	}
}

class Base<T> {
	
	//ParameterizedType.getActualTypeArguments();
	
	public Class<?> getGenericType(int index) {
		Type genType = getClass().getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			throw new RuntimeException("Index out of bounds");
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}
		return (Class<?>) params[index];
	}
}
