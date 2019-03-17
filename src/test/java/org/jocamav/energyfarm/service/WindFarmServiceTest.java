package org.jocamav.energyfarm.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityNotFoundException;

import org.assertj.core.data.Percentage;
import org.jocamav.energyfarm.dto.EnergyFarmDto;
import org.jocamav.energyfarm.dto.WindFarmDto;
import org.jocamav.energyfarm.entity.HourlyProduction;
import org.jocamav.energyfarm.entity.WindFarm;
import org.jocamav.energyfarm.repository.HourlyProductionRepository;
import org.jocamav.energyfarm.repository.WindFarmRepository;
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

	private void setUpMocksFromDates(String dateFromAsStr, String dateToAsStr) {
		localDateFrom = LocalDate.parse(dateFromAsStr);
		localDateTo = LocalDate.parse(dateToAsStr);
		madridFarm = new WindFarm(FARM_ID, "Madrid Farm", 10.0, MADRID_ZONE);
		Mockito.when(windFarmRepository.getOne(FARM_ID)).thenReturn(madridFarm);
		Mockito.when(dateUtilsService.getTimestampFromLocalDate(any(LocalDate.class), eq(MADRID_ZONE))).thenReturn(new Timestamp(0L));
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
			.withElectricityProduced(generateRandomValue(windFarm, localDateTime))
			.build();
			
	}
	
	private Double generateRandomValue(WindFarm windFarm, LocalDateTime localDateTime) {
		return (2.0 * localDateTime.getDayOfMonth()) / windFarm.getCapacity();
	}
	
	@Test
	public void getWindFarmDtoForTwoMonths() {
		setUpMocksFromDates("2018-01-01", "2018-02-28");
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);

		assertValues(windFarmDto);
		
	}
	
	@Test
	public void getWindFarmDtoForOneDay() {
		setUpMocksFromDates("2018-01-01", "2018-01-01");
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);
		
		assertValues(windFarmDto);
		
	}
	
	@Test
	public void getWindFarmDtoForTwoDaysWithChangeOfYear() {
		setUpMocksFromDates("2018-12-31", "2019-01-01");
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);
		
		assertValues(windFarmDto);
		
	}
	
	@Test
	public void getWindFarmDtoForInvalidRange() {
		setUpMocksFromDates("2019-01-02", "2019-01-01");
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);
		
		assertMainDataOfDto(windFarmDto);
		assertThat(windFarmDto.getProducedEnergy()).isEqualTo(0.0);
		assertThat(windFarmDto.getDailyCapacity().size()).isEqualTo(0);
		
	}

	private double expectedResult() {
		Double result = 0.0;
		LocalDate localDate = localDateFrom;
		while(!localDate.isAfter(localDateTo)) {
			result += (generateRandomValue(madridFarm, localDate.atStartOfDay())) * 24;
			localDate= localDate.plusDays(1);
		}
		return result;
	}

	private void assertValues(WindFarmDto windFarmDto) {
		assertMainDataOfDto(windFarmDto);
		assertThat(windFarmDto.getProducedEnergy()).isGreaterThan(0.0);
		assertThat(windFarmDto.getProducedEnergy()).isCloseTo(expectedResult(), Percentage.withPercentage(0.001));
		assertThat(windFarmDto.getDailyCapacity().size()).isGreaterThan(0);
		Long daysOfDifference = ChronoUnit.DAYS.between(localDateFrom, localDateTo) + 1;
		assertThat(windFarmDto.getDailyCapacity().size()).isLessThanOrEqualTo(daysOfDifference.intValue());
	}

	private void assertMainDataOfDto(WindFarmDto windFarmDto) {
		assertThat(windFarmDto).isNotNull();
		assertThat(windFarmDto.getId()).isEqualTo(FARM_ID);
		assertThat(windFarmDto.getZoneId()).isEqualTo(MADRID_ZONE);
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void getCapacityForNotExistingFarm() {
		Mockito.when(windFarmRepository.getOne(2L)).thenThrow(new EntityNotFoundException("Farm not found"));
		
		EnergyFarmDto windFarmDto = windFarmService.getEnergyFarmCapacity(2L,localDateFrom,localDateTo);
		
		assertThat(windFarmDto).isNotNull();
	}

}
