package ie.nuigalway.topology.domain.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "routerlsa")
public class RouterLsa {
	
	@Id
	@Column(name="data", nullable = false, unique = true)
	private Long data;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "linktype")
	private String linktype;
	
	@Column(name = "bodyid")
	private Long bodyid;
	
	@Column(name = "id")
	private Long id;
	
	@Column(name = "metric")
	private Integer metric;

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

	public String getLinktype() {
		return linktype;
	}

	public void setLinktype(String linktype) {
		this.linktype = linktype;
	}

	public Long getBodyid() {
		return bodyid;
	}

	public void setBodyid(Long bodyid) {
		this.bodyid = bodyid;
	}

	public Long getData() {
		return data;
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
	
	public RouterLsa() {}
}
