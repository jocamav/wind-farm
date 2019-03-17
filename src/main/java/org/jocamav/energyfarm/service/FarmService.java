package org.jocamav.energyfarm.service;

import java.time.LocalDate;

import org.jocamav.energyfarm.dto.EnergyFarmDto;

public interface FarmService {
	EnergyFarmDto getEnergyFarmCapacity(Long id, LocalDate dateFrom, LocalDate dateTo);
}
