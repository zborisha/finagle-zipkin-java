package com.github.zborisha;

import java.util.List;

import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.ServiceHealth;

public abstract class ConsulUtil {
	
	private static final String CONSUL_URL_ENV = "FJ_CONSUL_URL";

	public static int discoverServicePortByName(String serviceName) {
		System.out.println("Discovering port for " + serviceName);
		Consul consul = createAgent();
		HealthClient healthClient = consul.healthClient();
		List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances(serviceName).getResponse();
		if (nodes == null || nodes.size() == 0) {
			return -1;
		}
		int port = nodes.get(0).getService().getPort();
		System.out.println("Port for service " + serviceName + " is " + port);
		return port;
	}

	public static String discoverServiceAddressByName(String serviceName) {
		System.out.println("Discovering address for " + serviceName);
		Consul consul = createAgent();
		HealthClient healthClient = consul.healthClient();
		List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances(serviceName).getResponse();
		if (nodes == null || nodes.size() == 0) {
			return null;
		}
		String adr = nodes.get(0).getService().getAddress();
		System.out.println("Address for service " + serviceName + " is " + adr);
		return adr;
	}
	
	private static Consul createAgent() {
		String consulUrl = System.getenv(CONSUL_URL_ENV);
		System.out.println("Consul URL is " + consulUrl);
		Consul consul = Consul.builder().withUrl(consulUrl).build();
		return consul;
	}

}
