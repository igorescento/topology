package ie.nuigalway.topology.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import ie.nuigalway.topology.api.resources.IPv4Converter;
import ie.nuigalway.topology.domain.entities.Lsa;

public class LsaModelConverted {

	@JsonProperty
	private String id;

	@JsonProperty
	private String instance;

	@JsonProperty
	private String area;

	@JsonProperty
	private String type;

	@JsonProperty
	private String originator;

	@JsonProperty
	private String sequence;

	@JsonProperty
	private Integer age;

	@JsonProperty
	private String checksum;

	@JsonProperty
	private String options;

	@JsonProperty
	private String body;

	public LsaModelConverted(){ };

	public LsaModelConverted(Lsa l){
		this.id = IPv4Converter.longToIpv4(l.getIdTypePk().getId());
		this.instance = l.getInstance();
		this.area = l.getArea();
		this.type = l.getIdTypePk().getType();
		this.originator = IPv4Converter.longToIpv4(l.getOriginator());
		this.sequence = l.getSequence();
		this.age = l.getAge();
		this.checksum = l.getChecksum();
		this.options = l.getOptions();
		this.body = l.getBody();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOriginator() {
		return originator;
	}

	public void setOriginator(String originator) {
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
}
