package com.github.zborisha;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.Server;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;

public class FinagleJavaService {
	
	private static final int REPEAT_COUNT = 100000;
	private static final int WAIT_MILLIS = 1000;

	public static void main(String[] args) throws Exception {
		Server server = FinagleUtil.getServer();
		System.out.println("Started server!");
		Thread.sleep(2000);
		String[] servicesToLookup = FinagleUtil.getServicesToLookup();
		if(servicesToLookup != null && servicesToLookup.length > 0){
			for(int i=0;i<REPEAT_COUNT;i++){
				Thread.sleep(WAIT_MILLIS);
				System.out.println("Iteration " + i + " out of " + REPEAT_COUNT);
				for(String service : servicesToLookup) {
					Service<Request, Response> serviceClient = FinagleUtil.getFinagleClientForServiceByName(service);
					Request r = Request.apply(Method.apply("Get"), "/");
					Response resp = serviceClient.apply(r).apply();
					System.out.println("Response from srv is [" + resp.contentString() + "]");
				}
			}
		}
	}

}
