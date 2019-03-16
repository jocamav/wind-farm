package org.jocamav.energyfarm.service;

import org.jocamav.energyfarm.dto.EnergyFarmDto;

public interface FarmService {
	EnergyFarmDto getEnergyFarmCapacity(Long id);
}
