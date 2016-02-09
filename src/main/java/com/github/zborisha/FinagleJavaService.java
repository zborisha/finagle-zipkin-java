package com.github.zborisha;

import com.twitter.finagle.builder.Server;

public class FinagleJavaService {
	
	public static void main(String[] args) throws Exception {
		Server server = FinagleUtil.getServer();
		System.out.println("Started server!");
		Thread.sleep(2000);
		FinagleUtil.invokeExternalServices();
	}

}
