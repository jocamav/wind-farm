package org.jocamav.energyfarm.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityNotFoundException;

import org.jocamav.energyfarm.dto.EnergyFarmDto;
import org.jocamav.energyfarm.dto.WindFarmDto;
import org.jocamav.energyfarm.entity.HourlyProduction;
import org.jocamav.energyfarm.entity.WindFarm;
import org.jocamav.energyfarm.repository.HourlyProductionRepository;
import org.jocamav.energyfarm.repository.WindFarmRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {WindFarmService.class})
public class WindFarmServiceTest {
	
	@Autowired
	private WindFarmService windFarmService;

	@MockBean
	private DateUtilsService dateUtilsService;
	
	@MockBean
	private WindFarmRepository windFarmRepository;
	
	@MockBean
	private HourlyProductionRepository hourlyProductionRepository;
	
	private final static Long FARM_ID = 1L;
	private final static ZoneId MADRID_ZONE = ZoneId.of("Europe/Madrid");
	private WindFarm madridFarm;
	private LocalDate localDateFrom;
	private LocalDate localDateTo;
	
	@Before
	public void setUp() {
		localDateFrom = LocalDate.parse("2018-01-01");
		localDateTo = LocalDate.parse("2018-02-28");
		madridFarm = new WindFarm(FARM_ID, "Madrid Farm", 10.0, MADRID_ZONE);
		Mockito.when(windFarmRepository.getOne(FARM_ID)).thenReturn(madridFarm);
		Mockito.when(windFarmRepository.getOne(2L)).thenThrow(new EntityNotFoundException("Farm not found"));
		Mockito.when(dateUtilsService.getTimeStampFromLocalDate(any(LocalDate.class), eq(MADRID_ZONE))).thenReturn(new Timestamp(0L));
		List<HourlyProduction> madridProduction = generateProduction(madridFarm, localDateFrom, localDateTo);
		Mockito.when(hourlyProductionRepository.findByWindFarmWithTimestampBetween(eq(madridFarm), any(Timestamp.class), any(Timestamp.class)))
			.thenReturn(madridProduction);
	}

	private List<HourlyProduction> generateProduction(WindFarm windFarm, LocalDate dateFrom, LocalDate dateTo) {
		List<HourlyProduction> production = new ArrayList<>();
		LocalDate currentDay = dateFrom;
		Long id = 1L;
		while(!currentDay.isAfter(dateTo)) {
			for(int i = 0; i<24; i++) {
				LocalDateTime localDateTime = currentDay.atStartOfDay();
				localDateTime = localDateTime.plusHours(i);
				production.add(getProduction(id, windFarm, localDateTime));
				id++;
			}
			currentDay = currentDay.plusDays(1L);
		}
		return production;
	}
	
	private HourlyProduction getProduction(Long id, WindFarm windFarm, LocalDateTime localDateTime) {
		return new HourlyProduction.Builder()
			.withId(id)
			.withWindFarm(windFarm)
			.withLocalDateTime(localDateTime)
			.withElectricityProduced(generateRandomValue(windFarm))
			.build();
			
	}
	
	private Double generateRandomValue(WindFarm windFarm) {
		Random r = new Random();
		return windFarm.getCapacity() * r.nextDouble();
	}
	
	@Test
	public void getTimestampFromLocalDate() {
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);

		assertThat(windFarmDto).isNotNull();
		assertThat(windFarmDto.getId()).isEqualTo(FARM_ID);
		assertThat(windFarmDto.getZoneId()).isEqualTo(MADRID_ZONE);
		assertThat(windFarmDto.getProducedEnergy()).isGreaterThan(0.0);
		assertThat(windFarmDto.getDailyCapacity().size()).isGreaterThan(0);
		Long daysOfDifference = ChronoUnit.DAYS.between(localDateFrom, localDateTo) + 1;
		assertThat(windFarmDto.getDailyCapacity().size()).isLessThanOrEqualTo(daysOfDifference.intValue());
		
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void getCapacityForNotExistingFarm() {
		
		EnergyFarmDto windFarmDto = windFarmService.getEnergyFarmCapacity(2L,localDateFrom,localDateTo);
		
		assertThat(windFarmDto).isNotNull();
	}

}
