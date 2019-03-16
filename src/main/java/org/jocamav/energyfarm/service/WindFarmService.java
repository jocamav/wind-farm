package org.jocamav.energyfarm.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.jocamav.energyfarm.dto.CapacityPerDayDto;
import org.jocamav.energyfarm.dto.EnergyFarmDto;
import org.jocamav.energyfarm.dto.WindFarmDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WindFarmService implements FarmService{

	private static final Logger log = LoggerFactory.getLogger(WindFarmService.class);
	
	@Override
	public EnergyFarmDto getEnergyFarmCapacity(Long id) {
		log.debug(String.format("Getting capacity of farm %d", id));
		WindFarmDto energyFarmDto = new WindFarmDto();
		return energyFarmDto;
	}

}
