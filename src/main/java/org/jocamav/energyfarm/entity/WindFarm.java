package org.jocamav.energyfarm.entity;

import java.time.ZoneId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class WindFarm {
	
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private Double capacity;
    private ZoneId zoneId;
    
	public WindFarm() {
	}
	
	public WindFarm(Long id, String name, Double capacity, ZoneId zoneId) {
		this.id = id;
		this.name = name;
		this.capacity = capacity;
		this.zoneId = zoneId;
	}
	
	public WindFarm(String name, Double capacity, ZoneId zoneId) {
		this.name = name;
		this.capacity = capacity;
		this.zoneId = zoneId;
	}

	public WindFarm(Long id, ZoneId zoneId) {
		super();
		this.id = id;
		this.zoneId = zoneId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ZoneId getZoneId() {
		return zoneId;
	}

	public void setZoneId(ZoneId zoneId) {
		this.zoneId = zoneId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getCapacity() {
		return capacity;
	}

	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}

	@Override
	public String toString() {
		return String.format("WindFarm [id=%s, name=%s, capacity=%s, zoneId=%s]", id, name, capacity, zoneId);
	}
    
}
