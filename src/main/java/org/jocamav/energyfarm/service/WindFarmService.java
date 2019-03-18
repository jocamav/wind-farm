package org.jocamav.energyfarm.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jocamav.energyfarm.dto.CapacityPerDayDto;
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

	private WindFarm getWindFarm(Long id) {
		return windFarmRepository.getOne(id);
	}

	private List<HourlyProduction> getProductionFromDatabase(WindFarm windFarm, LocalDate dateFrom, LocalDate dateTo) {
		Timestamp timeFrom = getTimestampForFarm(dateFrom, windFarm);
		Timestamp timeTo = getTimestampForFarm(dateTo.plusDays(1), windFarm);
		return hourlyProductionRepository.findByWindFarmWithTimestampBetween(windFarm, timeFrom, timeTo);
	}

	private Timestamp getTimestampForFarm(LocalDate dateFrom, WindFarm windFarm) {
		return dateUtilsService.getTimestampFromLocalDate(dateFrom, windFarm.getZoneId());
	}
	
	private WindFarmDto getWindFarmDtoFromHourlyProduction(WindFarm windFarm, List<HourlyProduction> farmProduction) {
		WindFarmDto energyFarmDto = initWindFarmDto(windFarm);
		addDailyProductionInfo(farmProduction, energyFarmDto, windFarm);
		return energyFarmDto;
	}

	private WindFarmDto initWindFarmDto(WindFarm windFarm) {
		WindFarmDto energyFarmDto = new WindFarmDto();
		energyFarmDto.setId(windFarm.getId());
		energyFarmDto.setZoneId(windFarm.getZoneId());
		return energyFarmDto;
	}
	
	private void addDailyProductionInfo(List<HourlyProduction> farmProduction, WindFarmDto energyFarmDto, WindFarm windFarm) {
		
		Double producedEnergy = getTotalEnergyProduced(farmProduction);
		List<CapacityPerDayDto> dailyCapacity = getDailyCapacityFromHourlyProduction(windFarm, farmProduction);

		energyFarmDto.setProducedEnergy(producedEnergy);
		energyFarmDto.setDailyCapacity(dailyCapacity);
	}
	
	private double getTotalEnergyProduced(List<HourlyProduction> farmProduction) {
		return farmProduction.stream()
				.mapToDouble(hourlyProduction -> hourlyProduction.getElectricityProduced())
				.sum();
	}

	private List<CapacityPerDayDto> getDailyCapacityFromHourlyProduction(WindFarm windFarm, List<HourlyProduction> farmProduction) {
		List<CapacityPerDayDto> dailyCapacity =  convertHourlyProductionToDto(farmProduction);
		
		Map<LocalDate, Double> resultGroupByLocaldate = getDailyCapacityGroupByDate(dailyCapacity);
		dailyCapacity = calculateCapacityFactorFromGroupMap(windFarm, resultGroupByLocaldate);
		
		return dailyCapacity;
	}

	private List<CapacityPerDayDto> convertHourlyProductionToDto(List<HourlyProduction> farmProduction) {
		return farmProduction.stream()
			.map(hourlyProduction -> getCapacityPerDayDtoFromEntity(hourlyProduction))
			.collect(Collectors.toList());
	}

	private CapacityPerDayDto getCapacityPerDayDtoFromEntity(HourlyProduction hourlyProduction) {
		return new CapacityPerDayDto(hourlyProduction.getTimestamp().toLocalDateTime().toLocalDate(), 
				hourlyProduction.getElectricityProduced());
	}

	private List<CapacityPerDayDto> calculateCapacityFactorFromGroupMap(WindFarm windFarm, Map<LocalDate, Double> resultGroupByLocaldate) {
		return resultGroupByLocaldate.entrySet().stream()
			.map(entry -> {
				LocalDate localDate = entry.getKey();
				Double energyGeneratedInDay = entry.getValue();
				int numberOfHoursPerDay = dateUtilsService.getNumberOfHoursOfDay(localDate, windFarm.getZoneId());
				return new CapacityPerDayDto(localDate, energyGeneratedInDay/(windFarm.getCapacity()*numberOfHoursPerDay));
			})
			.collect(Collectors.toList());
	}

	private Map<LocalDate, Double> getDailyCapacityGroupByDate(List<CapacityPerDayDto> dailyCapacity) {
		return dailyCapacity.stream()
				.collect(
						Collectors.groupingBy(
								CapacityPerDayDto::getDay,
								Collectors.summingDouble(CapacityPerDayDto::getCapacity)
						)
				);
	}

}
