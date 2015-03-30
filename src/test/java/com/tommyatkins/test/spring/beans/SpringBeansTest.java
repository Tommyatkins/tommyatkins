package com.tommyatkins.test.spring.beans;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringBeansTest {

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"classpath:spring-config.xml"},false,null);
		ctx.refresh();
		TestBean myBean = ctx.getBean(TestBean.class);
		System.out.println(myBean.toString());
		myBean.setKey("changed-key");
		System.out.println(ctx.getBean(TestBean.class));
	}
}
