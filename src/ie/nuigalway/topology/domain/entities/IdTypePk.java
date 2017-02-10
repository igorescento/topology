package ie.nuigalway.topology.domain.entities;

import java.io.Serializable;

import javax.persistence.Column;

public class IdTypePk implements Serializable{

	private static final long serialVersionUID = -1952405332584451032L;

	@Column(name = "id")
	protected Long id;
	
	@Column(name = "type")
	protected String type;

	public IdTypePk() {}

	public IdTypePk(Long id, String type){
		this.id = id;
		this.type = type;
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

	public boolean equals(Object o){
		if(o == null)
			return false;
		if(!(o instanceof IdTypePk))
			return false;

		IdTypePk other = (IdTypePk) o;
		if(this.id != other.id)
			return false;
		if(!this.type.equals(other.type))
			return false;

		return true;
	}

	public int hashCode(){
		return (int) Long.hashCode(id) * type.hashCode();
	}
}
