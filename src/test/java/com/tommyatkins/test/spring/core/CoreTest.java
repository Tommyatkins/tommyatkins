package com.tommyatkins.test.spring.core;

import java.util.Properties;

public class CoreTest {

	public static void main(String[] args) {
		Properties properties = System.getProperties();
		properties.forEach((k, v) -> {
			System.out.println(String.format("%s: %s.", k, v));
		});
		
		
	}
}
