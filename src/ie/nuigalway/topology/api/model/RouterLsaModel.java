package ie.nuigalway.topology.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import ie.nuigalway.topology.api.resources.IPv4Converter;
import ie.nuigalway.topology.domain.entities.RouterLsa;

public class RouterLsaModel {
	
	@JsonProperty
	private Integer seq;

	@JsonProperty
	private Long id;
	
	@JsonProperty
	private String type;
	
	@JsonProperty
	private String linktype;
	
	@JsonProperty
	private Long bodyid;
	
	@JsonProperty
	private Long data;
	
	@JsonProperty
	private Integer metric;
	
	public RouterLsaModel(){ };
	
	public RouterLsaModel(RouterLsa rl){
		this.id = rl.getId();
		this.type = rl.getType();
		this.linktype = rl.getLinktype();
		this.bodyid = rl.getBodyid();
		this.data = rl.getData();
		this.metric = rl.getMetric();
	}
	
	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public String getLinktype() {
		return linktype;
	}

	public void setLinktype(String linktype) {
		this.linktype = linktype;
	}

	public String getBodyid() {
		return IPv4Converter.longToIpv4(bodyid);
	}

	public void setBodyid(Long bodyid) {
		this.bodyid = bodyid;
	}

	public String getData() {
		return IPv4Converter.longToIpv4(data);
	}

	public void setData(Long data) {
		this.data = data;
	}

	public Integer getMetric() {
		return metric;
	}

	public void setMetric(Integer metric) {
		this.metric = metric;
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
	
	public RouterLsa toEntity() {
		RouterLsa rlsa = new RouterLsa();
		
		rlsa.setId(id);
		rlsa.setType(type);
		rlsa.setLinktype(linktype);
		rlsa.setBodyid(bodyid);
		rlsa.setMetric(metric);
		rlsa.setData(data);
	
		return rlsa;
	}
}
