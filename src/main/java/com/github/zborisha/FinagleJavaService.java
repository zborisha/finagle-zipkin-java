package com.github.zborisha;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.twitter.finagle.builder.Server;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;

public class FinagleJavaService {

	public static void main(String[] args) throws Exception {
		Server server = FinagleUtil.getServer();
		System.out.println("Started server!");
		exportMetrics();
		Thread.sleep(2000);
		FinagleUtil.invokeExternalServices();
	}

	private static final void exportMetrics() {
		int port = FinagleUtil.getPort() + 1;
		org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(port);
		System.out.println("Will expose metrics on port " + port);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
		// Put your application setup code here.
		try {
			server.start();
			server.join();
			System.out.println("Successfully exposed metrics on port " + port);
			DefaultExports.initialize();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

}
