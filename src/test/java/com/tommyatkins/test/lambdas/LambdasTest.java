package com.tommyatkins.test.lambdas;

import java.util.function.Function;

public class LambdasTest {

	public static void main(String[] args) {
		function();
	}

	public static void function() {
		Function<Integer, String> f = (in) -> in + " - return";
		System.out.println(f.apply(108));;
	}
}
