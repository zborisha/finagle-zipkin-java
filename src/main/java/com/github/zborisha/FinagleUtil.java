package com.github.zborisha;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Random;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.builder.Server;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.http.Status;
import com.twitter.finagle.stats.DefaultStatsReceiver;
import com.twitter.finagle.tracing.Annotation;
import com.twitter.finagle.tracing.Trace;
import com.twitter.finagle.tracing.Tracer;
import com.twitter.finagle.zipkin.thrift.ZipkinTracer;
import com.twitter.util.Future;

public abstract class FinagleUtil {

	private static final int REPEAT_COUNT = 100000;
	private static final int WAIT_MILLIS = 100;

	private static final int DEFAULT_SERVER_PORT = 8910;

	private static final String SERVER_NAME_ENV = "SERVICE_NAME";
	private static final String SERVICES_TO_LOOKUP_ENV = "FJ_SERVICES_TO_LOOKUP_CSV";
	private static final String SERVER_PORT_ENV = "FJ_SERVER_PORT";
	private static final String serverNameSufix = "" + new Random().nextInt(100000);

	public static final Tracer zipkinTracer = ZipkinTracer.mk("localhost", 9410, DefaultStatsReceiver.get(), 1.0f);

	private static String getServerName() {
		String serverName = System.getenv(SERVER_NAME_ENV);
		if (serverName == null || serverName.length() < 1) {
			serverName = "finagle-java-server-" + serverNameSufix;
		}
		return serverName;
	}

	public static Server getServer() {
		String serverName = getServerName();
		return ServerBuilder.safeBuild(getService(),
				ServerBuilder.get().codec(com.twitter.finagle.http.Http.get().enableTracing(true))
						.bindTo(new InetSocketAddress(getPort())).name(serverName).tracer(zipkinTracer));
	}

	private static int getPort() {
		String port = System.getenv(SERVER_PORT_ENV);
		if (port != null && port.length() > 0) {
			return Integer.parseInt(port);
		}
		return DEFAULT_SERVER_PORT;
	}

	public static Service<Request, Response> getFinagleClientForServiceByName(String serviceName) {
		String serverName = getServerName();
		System.out.println("Server name is " + serverName + ", looking up port for " + serviceName);
		int port = ConsulUtil.discoverServicePortByName(serviceName);
		if (port < 0) {
			throw new IllegalStateException("Was not able to find service " + serviceName);
		}
		String address = ConsulUtil.discoverServiceAddressByName(serviceName);
		System.out.println("Will try to invoke service on " + address + ":" + port);
		Service<Request, Response> service = ClientBuilder.safeBuild(ClientBuilder.get().name("client-on-" + serverName)
				.codec(com.twitter.finagle.http.Http.get().enableTracing(true)).hosts(address + ":" + port)
				.failFast(false).tracer(zipkinTracer).hostConnectionLimit(1).retries(2));
		return service;
	}

	public static String[] getServicesToLookup() {
		String services = System.getenv(SERVICES_TO_LOOKUP_ENV);
		if (services == null || services.length() < 1) {
			System.out.println("Did not find " + SERVICES_TO_LOOKUP_ENV + ", will not lookup any service!");
			return new String[] {};
		} else {
			String[] serviceNames = services.split(",");
			System.out.println("Will lookup following services " + Arrays.asList(serviceNames));
			return serviceNames;
		}
	}

	private static Service<Request, Response> getService() {
		return new Service<Request, Response>() {
			@Override
			public Future<Response> apply(Request req) {
				Trace.record("Executing server logic for " + getServerName());
				invokeExternalServicesOnce();
				Response res = Response.apply(Status.Accepted());
				res.setContentString("Hello from Finagle Server @" + System.currentTimeMillis());
				return Future.value(res);
			}
		};
	}

	private static void invokeExternalServicesOnce() {
		String[] servicesToLookup = FinagleUtil.getServicesToLookup();
		if (servicesToLookup != null && servicesToLookup.length > 0) {
			for (String service : servicesToLookup) {
				Trace.record("Invoking external service " + service);
				Service<Request, Response> serviceClient = FinagleUtil.getFinagleClientForServiceByName(service);
				Request r = Request.apply(Method.apply("Get"), "/");
				Response resp = serviceClient.apply(r).apply();
				System.out.println("Response from srv is [" + resp.contentString() + "]");
				Trace.record("Finished invoking external service " + service);
			}
		}
	}

	public static void invokeExternalServices() {
		String[] servicesToLookup = FinagleUtil.getServicesToLookup();
		if (servicesToLookup != null && servicesToLookup.length > 0) {
			for (int i = 0; i < REPEAT_COUNT; i++) {
				try {
					Thread.sleep(WAIT_MILLIS);
				} catch (Exception ignored) {
					// do nothing
				}
				System.out.println("Iteration " + i + " out of " + REPEAT_COUNT);
				invokeExternalServicesOnce();
			}
		}
	}

}
