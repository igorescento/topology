package ie.nuigalway.topology.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TopologyModel {

	@JsonProperty
	private String routerid;
	
	@JsonProperty
	private String routerinterface;
	
	@JsonProperty
	private Integer metric;
	
	@JsonProperty
	private String routersid;
	
	@JsonProperty
	private Integer numrouters;

	@JsonProperty
	private String firstaddr;
	
	@JsonProperty
	private String lastaddr;
	
	@JsonProperty
	private String networkaddr;
	
	@JsonProperty
	private String broadcastaddr;
	
	@JsonProperty
	private String netmask;
	
	public String getRouterid() {
		return routerid;
	}

	public void setRouterid(String routerid) {
		this.routerid = routerid;
	}

	public String getRouterinterface() {
		return routerinterface;
	}

	public void setRouterinterface(String routerinterface) {
		this.routerinterface = routerinterface;
	}

	public Integer getMetric() {
		return metric;
	}

	public void setMetric(Integer metric) {
		this.metric = metric;
	}

	public String getRoutersid() {
		return routersid;
	}

	public void setRoutersid(String routersid) {
		this.routersid = routersid;
	}

	public Integer getNumrouters() {
		return numrouters;
	}

	public void setNumrouters(Integer numrouters) {
		this.numrouters = numrouters;
	}

	public String getFirstaddr() {
		return firstaddr;
	}

	public void setFirstaddr(String firstaddr) {
		this.firstaddr = firstaddr;
	}

	public String getLastaddr() {
		return lastaddr;
	}

	public void setLastaddr(String lastaddr) {
		this.lastaddr = lastaddr;
	}

	public String getNetworkaddr() {
		return networkaddr;
	}

	public void setNetworkaddr(String networkaddr) {
		this.networkaddr = networkaddr;
	}

	public String getBroadcastaddr() {
		return broadcastaddr;
	}

	public void setBroadcastaddr(String broadcastaddr) {
		this.broadcastaddr = broadcastaddr;
	}

	public String getNetmask() {
		return netmask;
	}

	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}
	
	public TopologyModel() {};
}
