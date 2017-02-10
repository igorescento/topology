package ie.nuigalway.topology.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import ie.nuigalway.topology.domain.entities.NetworkLsa;

public class NetworkLsaModel {

	@JsonProperty
	private Long id;

	@JsonProperty
	private String type;

	@JsonProperty
	private Long netmask;
	
	@JsonProperty
	private String routersid;
	
	@JsonProperty
	private Integer numrouters;

	@JsonProperty
	private Long originator;

	@JsonProperty
	private Long firstaddr;
	
	@JsonProperty
	private Long lastaddr;
	
	@JsonProperty
	private Long networkaddr;
	
	@JsonProperty
	private Long broadcastaddr;

	public NetworkLsaModel(){ };

	public NetworkLsaModel(NetworkLsa nl){
		this.id = nl.getId();
		this.type = nl.getType();
		this.netmask = nl.getNetmask();
		this.routersid = nl.getRoutersid();
		this.numrouters = nl.getNumrouters();
		this.originator = nl.getOriginator();
		this.firstaddr = nl.getFirstaddr();
		this.lastaddr = nl.getLastaddr();
		this.networkaddr = nl.getNetworkaddr();
		this.broadcastaddr = nl.getBroadcastaddr();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getNetmask() {
		return netmask;
	}

	public void setNetmask(Long netmask) {
		this.netmask = netmask;
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

	public Long getOriginator() {
		return originator;
	}

	public void setOriginator(Long originator) {
		this.originator = originator;
	}
	
	public Long getFirstaddr() {
		return firstaddr;
	}

	public void setFirstaddr(Long firstAddr) {
		this.firstaddr = firstAddr;
	}

	public Long getLastaddr() {
		return lastaddr;
	}

	public void setLastaddr(Long lastAddr) {
		this.lastaddr = lastAddr;
	}

	public Long getNetworkaddr() {
		return networkaddr;
	}

	public void setNetworkAddr(Long networkAddr) {
		this.networkaddr = networkAddr;
	}

	public Long getBroadcastaddr() {
		return broadcastaddr;
	}

	public void setBroadcastaddr(Long broadcastAddr) {
		this.broadcastaddr = broadcastAddr;
	}
	
	public NetworkLsa toEntity() {
		NetworkLsa nlsa = new NetworkLsa();

		nlsa.setId(id);
		nlsa.setType(type);
		nlsa.setNetmask(netmask);
		nlsa.setRoutersid(routersid);
		nlsa.setNumrouters(numrouters);
		nlsa.setOriginator(originator);
		nlsa.setFirstaddr(firstaddr);
		nlsa.setLastaddr(lastaddr);
		nlsa.setNetworkaddr(networkaddr);
		nlsa.setBroadcastaddr(broadcastaddr);
		
		return nlsa;
	}
}
