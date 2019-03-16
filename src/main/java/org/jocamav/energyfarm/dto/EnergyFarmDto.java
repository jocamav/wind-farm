package org.jocamav.energyfarm.dto;

import java.time.ZoneId;

public abstract class EnergyFarmDto {
	private long id;
	private ZoneId zoneId;
	private Double producedEnergy;
	
	public EnergyFarmDto() {
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public ZoneId getZoneId() {
		return zoneId;
	}

	public void setZoneId(ZoneId zoneId) {
		this.zoneId = zoneId;
	}

	public Double getProducedEnergy() {
		return producedEnergy;
	}

	public void setProducedEnergy(Double producedEnergy) {
		this.producedEnergy = producedEnergy;
	}
	
}
