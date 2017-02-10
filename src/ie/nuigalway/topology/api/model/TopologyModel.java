package ie.nuigalway.topology.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TopologyModel {

	@JsonProperty
	private Long routerid;
	
	@JsonProperty
	private Long routerinterface;
	
	@JsonProperty
	private Integer metric;
	
	@JsonProperty
	private String routersid;
	
	@JsonProperty
	private Integer numrouters;

	@JsonProperty
	private Long firstaddr;
	
	@JsonProperty
	private Long lastaddr;
	
	@JsonProperty
	private Long networkaddr;
	
	@JsonProperty
	private Long broadcastaddr;
	
	@JsonProperty
	private Long netmask;
	
	public Long getRouterid() {
		return routerid;
	}

	public void setRouterid(Long routerid) {
		this.routerid = routerid;
	}

	public Long getRouterinterface() {
		return routerinterface;
	}

	public void setRouterinterface(Long routerinterface) {
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

	public Long getFirstaddr() {
		return firstaddr;
	}

	public void setFirstaddr(Long firstaddr) {
		this.firstaddr = firstaddr;
	}

	public Long getLastaddr() {
		return lastaddr;
	}

	public void setLastaddr(Long lastaddr) {
		this.lastaddr = lastaddr;
	}

	public Long getNetworkaddr() {
		return networkaddr;
	}

	public void setNetworkaddr(Long networkaddr) {
		this.networkaddr = networkaddr;
	}

	public Long getBroadcastaddr() {
		return broadcastaddr;
	}

	public void setBroadcastaddr(Long broadcastaddr) {
		this.broadcastaddr = broadcastaddr;
	}

	public Long getNetmask() {
		return netmask;
	}

	public void setNetmask(Long netmask) {
		this.netmask = netmask;
	}
	
	public TopologyModel() {};
	
	
}
