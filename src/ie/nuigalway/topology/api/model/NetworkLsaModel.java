package ie.nuigalway.topology.api.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import ie.nuigalway.topology.api.resources.IPv4Converter;
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
	
	@JsonProperty
	private Long ipavailable;

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
		this.ipavailable = this.lastaddr + 1 - this.firstaddr - this.numrouters;
	}

	public String getId() {
		return IPv4Converter.longToIpv4(id);
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

	public String getNetmask() {
		return IPv4Converter.longToIpv4(netmask);
	}

	public void setNetmask(Long netmask) {
		this.netmask = netmask;
	}

	public String getRoutersid() {
		ArrayList<String> routIp = new ArrayList<>();
		for(String router : routersid.split(",")){
			routIp.add(IPv4Converter.longToIpv4(Long.parseLong(router.trim())));
		}
		return routIp.toString().replace("[", "").replace("]", "");
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

	public String getOriginator() {
		return IPv4Converter.longToIpv4(originator);
	}

	public void setOriginator(Long originator) {
		this.originator = originator;
	}

	public String getFirstaddr() {
		return IPv4Converter.longToIpv4(firstaddr);
	}

	public void setFirstaddr(Long firstAddr) {
		this.firstaddr = firstAddr;
	}

	public String getLastaddr() {
		return IPv4Converter.longToIpv4(lastaddr);
	}

	public void setLastaddr(Long lastAddr) {
		this.lastaddr = lastAddr;
	}

	public String getNetworkaddr() {
		return IPv4Converter.longToIpv4(networkaddr);
	}

	public void setNetworkaddr(Long networkaddr) {
		this.networkaddr = networkaddr;
	}

	public String getBroadcastaddr() {
		return IPv4Converter.longToIpv4(broadcastaddr);
	}

	public void setBroadcastaddr(Long broadcastAddr) {
		this.broadcastaddr = broadcastAddr;
	}
	
	public Long getIpavailable() {
		return ipavailable;
	}

	public void setIpavailable(Long ipavailable) {
		this.ipavailable = ipavailable;
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
