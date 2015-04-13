package com.tommyatkins.test.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;

import com.tommyatkins.test.reflect.anno.LocalValue;
import com.tommyatkins.test.reflect.anno.MethodParam;

public class AnnoTest {
	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		Class<MyClass> c = MyClass.class;
		Method m = c.getMethod("test", new Class[] { String.class, Object.class });
		Parameter[] p = m.getParameters();
		for (Parameter parameter : p) {
			Annotation[] anns = parameter.getAnnotations();
			for (Annotation annotation : anns) {
				System.out.println(annotation.annotationType().getName());
			}
		}

		TypeVariable<Method>[] tps = m.getTypeParameters();
		for (TypeVariable<Method> typeVariable : tps) {
			System.out.println(typeVariable.getName());
		}
		
	}
}

class MyClass {

	public <T> String test(@MethodParam String param, T t) {
		@LocalValue("tommyatkins")
		String value = param + " - ";
		return value + t.getClass().getName();
	}
}
