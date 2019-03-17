package org.jocamav.energyfarm.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.jocamav.energyfarm.dto.EnergyFarmDto;
import org.jocamav.energyfarm.dto.WindFarmDto;
import org.jocamav.energyfarm.entity.HourlyProduction;
import org.jocamav.energyfarm.entity.WindFarm;
import org.jocamav.energyfarm.repository.HourlyProductionRepository;
import org.jocamav.energyfarm.repository.WindFarmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WindFarmService implements FarmService{

	private static final Logger log = LoggerFactory.getLogger(WindFarmService.class);
	
	@Autowired
	private DateUtilsService dateUtilsService;
	
	@Autowired
	private WindFarmRepository windFarmRepository;
	
	@Autowired
	private HourlyProductionRepository hourlyProductionRepository;
	
	@Override
	public EnergyFarmDto getEnergyFarmCapacity(Long id, LocalDate dateFrom, LocalDate dateTo) {
		log.info(String.format("Getting capacity of wind farm <%d> from %s to %s", id, dateFrom, dateTo));
		WindFarm windFarm = getWindFarm(id);
		List<HourlyProduction> farmProduction = getProductionFromDatabase(windFarm, dateFrom, dateTo);
		return getWindFarmDtoFromHourlyProduction(windFarm, farmProduction);
	}


	private List<HourlyProduction> getProductionFromDatabase(WindFarm windFarm, LocalDate dateFrom, LocalDate dateTo) {
		Timestamp timeFrom = getTimestampForFarm(dateFrom, windFarm);
		Timestamp timeTo = getTimestampForFarm(dateTo, windFarm);
		return hourlyProductionRepository.findByWindFarmWithTimestampBetween(windFarm, timeFrom, timeTo);
	}
	
	private WindFarm getWindFarm(Long id) {
		return windFarmRepository.getOne(id);
	}

	private Timestamp getTimestampForFarm(LocalDate dateFrom, WindFarm windFarm) {
		return dateUtilsService.getTimeStampFromLocalDate(dateFrom, windFarm.getZoneId());
	}
	
	private WindFarmDto getWindFarmDtoFromHourlyProduction(WindFarm windFarm, List<HourlyProduction> farmProduction) {
		WindFarmDto energyFarmDto = new WindFarmDto();
		energyFarmDto.setId(windFarm.getId());
		energyFarmDto.setZoneId(windFarm.getZoneId());
		Double producedEnergy = 0.0;
		for( HourlyProduction hourlyProduction : farmProduction) {
			log.info(String.format("Adding production:%s", hourlyProduction.toString()));
			producedEnergy += hourlyProduction.getElectricityProduced();
		}
		energyFarmDto.setProducedEnergy(producedEnergy);
		return energyFarmDto;
	}

}
