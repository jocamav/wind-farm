package org.jocamav.energyfarm.dto;

import java.time.LocalDate;

public class CapacityPerDayDto {

	private LocalDate day;
	private Double capacity;
	
	
	
	public CapacityPerDayDto() {
	}
	

	public CapacityPerDayDto(LocalDate day, Double capacity) {
		super();
		this.day = day;
		this.capacity = capacity;
	}

	

	public LocalDate getDay() {
		return day;
	}


	public void setDay(LocalDate day) {
		this.day = day;
	}


	public Double getCapacity() {
		return capacity;
	}

	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}
}
