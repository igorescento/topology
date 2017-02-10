package ie.nuigalway.topology.domain.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "networklsa")
public class NetworkLsa {

	@Id
	@Column(name="id", nullable = false, unique = true)
	private Long id;

	@Column(name = "type")
	private String type;

	@Column(name = "netmask")
	private Long netmask;

	@Column(name = "routersid")
	private String routersid;
	
	@Column(name = "numrouters")
	private Integer numrouters;
	
	@Column(name = "originator")
	private Long originator;

	@Column(name = "firstaddr")
	private Long firstaddr;
	
	@Column(name = "lastaddr")
	private Long lastaddr;
	
	@Column(name = "networkaddr")
	private Long networkaddr;
	
	@Column(name = "broadcastaddr")
	private Long broadcastaddr;
	
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
	
	public NetworkLsa(){}
}
