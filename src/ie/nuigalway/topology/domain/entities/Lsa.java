package ie.nuigalway.topology.domain.entities;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "lsa")
public class Lsa {

	@EmbeddedId
	private IdTypePk IdTypePk;
	
	@Column(name = "instance")
	private String instance;
	
	@Column(name = "area", nullable = true)
	private String area;
	
	@Column(name = "originator")
	private Long originator;
	
	@Column(name = "sequence")
	private String sequence;
	
	@Column(name = "age")
	private Integer age;
	
	@Column(name = "checksum")
	private String checksum;
	
	@Column(name = "options")
	private String options;
	
	@Column(name = "body", columnDefinition = "text")
	private String body;
	
	public IdTypePk getIdTypePk() {
		return IdTypePk;
	}

	public void setIdTypePk(IdTypePk i) {
		this.IdTypePk = i;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public Long getOriginator() {
		return originator;
	}

	public void setOriginator(Long originator) {
		this.originator = originator;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public Lsa(){}
}
