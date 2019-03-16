package org.jocamav.energyfarm.dto;

import java.util.List;

public class WindFarmDto extends EnergyFarmDto{

	List<CapacityPerDayDto> dailyCapacity;
	
	public WindFarmDto() {
		super();
	}

	public List<CapacityPerDayDto> getDailyCapacity() {
		return dailyCapacity;
	}

	public void setDailyCapacity(List<CapacityPerDayDto> dailyCapacity) {
		this.dailyCapacity = dailyCapacity;
	}
	
	
}
