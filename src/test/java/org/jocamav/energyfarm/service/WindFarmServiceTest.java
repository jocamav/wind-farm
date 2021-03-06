package org.jocamav.energyfarm.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
	
	public enum GenerationType {
		ZERO, CALCULATED, MAX
	}
	
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

	private void setUpMocksFromDates(String dateFromAsStr, String dateToAsStr, GenerationType generationType) {
		localDateFrom = LocalDate.parse(dateFromAsStr);
		localDateTo = LocalDate.parse(dateToAsStr);
		madridFarm = new WindFarm(FARM_ID, "Madrid Farm", 10.0, MADRID_ZONE);
		Mockito.when(windFarmRepository.getOne(FARM_ID)).thenReturn(madridFarm);
		Mockito.when(dateUtilsService.getTimestampFromLocalDate(any(LocalDate.class), eq(MADRID_ZONE))).thenReturn(new Timestamp(0L));
		Mockito.when(dateUtilsService.getNumberOfHoursOfDay(any(LocalDate.class), eq(MADRID_ZONE))).thenReturn(24);
		Mockito.when(dateUtilsService.getNumberOfHoursOfDay(eq(LocalDate.parse("2018-10-28")), eq(MADRID_ZONE))).thenReturn(25);
		Mockito.when(dateUtilsService.getNumberOfHoursOfDay(eq(LocalDate.parse("2018-03-25")), eq(MADRID_ZONE))).thenReturn(23);
		List<HourlyProduction> madridProduction = generateProduction(madridFarm, localDateFrom, localDateTo, generationType);
		Mockito.when(hourlyProductionRepository.findByWindFarmWithTimestampBetween(eq(madridFarm), any(Timestamp.class), any(Timestamp.class)))
			.thenReturn(madridProduction);
	}

	private List<HourlyProduction> generateProduction(WindFarm windFarm, LocalDate dateFrom, LocalDate dateTo, GenerationType generationType) {
		List<HourlyProduction> production = new ArrayList<>();
		LocalDate currentDay = dateFrom;
		Long id = 1L;
		while(!currentDay.isAfter(dateTo)) {
			ZonedDateTime zonedDateTime = currentDay.atStartOfDay().atZone(windFarm.getZoneId());
			while(currentDay.getDayOfMonth() == zonedDateTime.getDayOfMonth()) {
				production.add(getProduction(id, windFarm, zonedDateTime, generationType));
				zonedDateTime = zonedDateTime.plusHours(1);
				id++;
			}
			currentDay = currentDay.plusDays(1L);
		}
		return production;
	}
	
	private HourlyProduction getProduction(Long id, WindFarm windFarm, ZonedDateTime zonedDateTime, GenerationType generationType) {
		return new HourlyProduction.Builder()
			.withId(id)
			.withWindFarm(windFarm)
			.withZonedDateTime(zonedDateTime)
			.withElectricityProduced(generateValue(windFarm, zonedDateTime, generationType))
			.build();
			
	}
	
	private Double generateValue(WindFarm windFarm, ZonedDateTime zonedDateTime, GenerationType generationType) {
		if(GenerationType.CALCULATED.equals(generationType)) {
			return (2.0 * zonedDateTime.getDayOfMonth()) / windFarm.getCapacity();
		}
		else if(GenerationType.MAX.equals(generationType)) {
			return windFarm.getCapacity();
		}
		else if(GenerationType.ZERO.equals(generationType)) {
			return 0.0;
		}
		else {
			return null;
		}
	}
	
	@Test
	public void getWindFarmDtoForTwoMonths() {
		setUpMocksFromDates("2018-01-01", "2018-02-28", GenerationType.CALCULATED);
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);

		assertValues(windFarmDto);
		
	}
	
	@Test
	public void getWindFarmDtoForOneDay() {
		setUpMocksFromDates("2018-01-01", "2018-01-01", GenerationType.CALCULATED);
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);
		
		assertValues(windFarmDto);
		
	}
	
	@Test
	public void getWindFarmDtoForTwoDaysWithChangeOfYear() {
		setUpMocksFromDates("2018-12-31", "2019-01-01", GenerationType.CALCULATED);
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);
		
		assertValues(windFarmDto);
		
	}
	
	@Test
	public void getWindFarmDtoForInvalidRange() {
		setUpMocksFromDates("2019-01-02", "2019-01-01", GenerationType.CALCULATED);
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);
		
		assertMainDataOfDto(windFarmDto);
		assertThat(windFarmDto.getProducedEnergy()).isEqualTo(0.0);
		assertThat(windFarmDto.getDailyCapacity().size()).isEqualTo(0);
		
	}
	
	@Test
	public void getWindFarmDtoForOneMonthWithNoProduction() {
		setUpMocksFromDates("2018-07-01", "2018-07-31", GenerationType.ZERO);
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);
		
		assertMainDataOfDto(windFarmDto);
		assertThat(windFarmDto.getProducedEnergy()).isEqualTo(0.0);
		windFarmDto.getDailyCapacity().forEach( dailyCapacity ->
			assertThat(dailyCapacity.getCapacity()).isEqualTo(0.0)
		);
	}
	

	@Test
	public void getWindFarmDtoForOneMonthWithFullProduction() {
		setUpMocksFromDates("2018-07-01", "2018-07-31", GenerationType.MAX);
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);
		
		assertMainDataOfDto(windFarmDto);
		assertThat(windFarmDto.getProducedEnergy()).isEqualTo(31*24*madridFarm.getCapacity());
		windFarmDto.getDailyCapacity().forEach( dailyCapacity ->
			assertThat(dailyCapacity.getCapacity()).isEqualTo(1.0)
		);
	}
	

	@Test
	public void getWindFarmDtoForOneDayWithOneExtraHour() {
		setUpMocksFromDates("2018-10-28", "2018-10-28", GenerationType.MAX);
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);
		
		assertMainDataOfDto(windFarmDto);
		assertThat(windFarmDto.getProducedEnergy()).isEqualTo(25*madridFarm.getCapacity());
		windFarmDto.getDailyCapacity().forEach( dailyCapacity ->
			assertThat(dailyCapacity.getCapacity()).isEqualTo(1.0)
		);
	}
	
	@Test
	public void getWindFarmDtoForOneDayWithOneHourLess() {
		setUpMocksFromDates("2018-03-25", "2018-03-25", GenerationType.MAX);
		
		WindFarmDto windFarmDto = (WindFarmDto) windFarmService.getEnergyFarmCapacity(1L,localDateFrom,localDateTo);
		
		assertMainDataOfDto(windFarmDto);
		assertThat(windFarmDto.getProducedEnergy()).isEqualTo(23*madridFarm.getCapacity());
		windFarmDto.getDailyCapacity().forEach( dailyCapacity ->
			assertThat(dailyCapacity.getCapacity()).isEqualTo(1.0)
		);
	}
	
	private double expectedResult() {
		Double result = 0.0;
		LocalDate localDate = localDateFrom;
		while(!localDate.isAfter(localDateTo)) {
			result += (generateValue(madridFarm, localDate.atStartOfDay().atZone(madridFarm.getZoneId()), GenerationType.CALCULATED)) * 24;
			localDate= localDate.plusDays(1);
		}
		return result;
	}

	private void assertValues(WindFarmDto windFarmDto) {
		assertMainDataOfDto(windFarmDto);
		assertThat(windFarmDto.getProducedEnergy()).isGreaterThan(0.0);
		assertThat(windFarmDto.getProducedEnergy()).isCloseTo(expectedResult(), Percentage.withPercentage(0.001));
		assertThat(windFarmDto.getDailyCapacity().size()).isGreaterThan(0);
		windFarmDto.getDailyCapacity().forEach( dailyCapacity ->
				assertThat(dailyCapacity.getCapacity()).isLessThan(1.0)
		);
		
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
