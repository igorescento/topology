package ie.nuigalway.topology.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnDataModel {

	@JsonProperty
	private String ipaddress;
	
	@JsonProperty
	private String username;
	
	@JsonProperty
	private String password;
	
	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
