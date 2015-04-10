package com.tommyatkins.test.jndi;

import java.util.Hashtable;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SimpleTest {
	
	public static void main(String[] args) throws NamingException {
		Context ctx = new InitialContext();
		ctx.addToEnvironment("java:tommyatkins", 666);
	}
	
}
