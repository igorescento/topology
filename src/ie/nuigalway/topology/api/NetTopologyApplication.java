package ie.nuigalway.topology.api;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("api")
public class NetTopologyApplication extends ResourceConfig {

	public NetTopologyApplication() {
		property(ServerProperties.APPLICATION_NAME, "topology");
		property(ServerProperties.PROVIDER_PACKAGES, "ie.nuigalway.topology.api");

		//Enable automatic conversion to JSON with jackson
		register(JacksonFeature.class);
	}
}
