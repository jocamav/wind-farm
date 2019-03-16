package org.jocamav.energyfarm.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.hibernate.SessionFactory;
import org.jocamav.energyfarm.entity.HourlyProduction;
import org.jocamav.energyfarm.entity.WindFarm;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@DataJpaTest
public class HourlyProductionRepositoryTest {

	private static final Logger log = LoggerFactory.getLogger(HourlyProductionRepositoryTest.class);
	
	@Autowired
	private WindFarmRepository windFarmRepository;
	
	@Autowired
	private HourlyProductionRepository hourlyProductionRepository;
	
	private WindFarm windFarmMadrid;
	private WindFarm windFarmZurich;
	
	@Before
	public void saveSomeFarms() {

		// save a few of farms
		windFarmMadrid = windFarmRepository.save(new WindFarm("Farm A", 10.0, ZoneId.of("Europe/Madrid")));
		hourlyProductionRepository.save(getHourlyProduction(windFarmMadrid, 4.1, 2018, 10, 1, 1, 1));
		hourlyProductionRepository.save(getHourlyProduction(windFarmMadrid, 4.5, 2018, 10, 2, 1, 1));
		hourlyProductionRepository.save(getHourlyProduction(windFarmMadrid, 4.9, 2018, 10, 5, 1, 1));
		hourlyProductionRepository.save(getHourlyProduction(windFarmMadrid, 5.1, 2018, 10, 5, 2, 1));
		windFarmZurich = windFarmRepository.save(new WindFarm("Farm D", 13.0, ZoneId.of("Europe/Zurich")));
		hourlyProductionRepository.save(getHourlyProduction(windFarmZurich, 5.1, 2018, 10, 5, 1, 1));
		hourlyProductionRepository.save(getHourlyProduction(windFarmZurich, 5.1, 2018, 10, 5, 2, 1));
		windFarmRepository.flush();
	}
	
	private HourlyProduction getHourlyProduction(WindFarm windfarm, double production,int year, int month, int day, int hour, int minute) {
		return new HourlyProduction(windfarm, getTimeStamp(year, month, day, hour, minute, windfarm.getZoneId()), production);
	}
	
	private Timestamp getTimeStamp(int year, int month, int day, int hour, int minute, ZoneId zoneId) {
		ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zoneId);
		return Timestamp.from(zonedDateTime.toInstant());
	}
	
	@Test
	public void getAllWindFarms() {

		log.info("WindFarm found with findAll():");
		log.info("-------------------------------");
		Collection<HourlyProduction> allEnergyProductions = hourlyProductionRepository.findAll();
		assertThat(allEnergyProductions.size()).isEqualTo(6);
		for (HourlyProduction energyProduction : allEnergyProductions) {
			log.info(energyProduction.toString());
			log.info("\tFarm: " +  energyProduction.getWindFarm().toString());
		}
		log.info("");

	}
	
	@Test
	public void findByWindFarm() {

		Collection<HourlyProduction> energyProductionsOfMadrid = hourlyProductionRepository.findByWindFarm(windFarmMadrid);
		assertThat(energyProductionsOfMadrid.size()).isEqualTo(4);

		Collection<HourlyProduction> energyProductionsOfZurich = hourlyProductionRepository.findByWindFarm(windFarmZurich);
		assertThat(energyProductionsOfZurich.size()).isEqualTo(2);

	}

	@Test
	public void findByWindFarmAndTimestamp() {

		Timestamp timeFrom = getTimeStamp(2018, 10, 2, 0, 0, windFarmMadrid.getZoneId()); //2018-10-2 00:00
		Timestamp timeTo = getTimeStamp(2018, 10, 5, 2, 0, windFarmMadrid.getZoneId()); //2018-10-5 02:00
		Collection<HourlyProduction> energyProductionsOfMadrid = hourlyProductionRepository.findByWindFarmWithTimestampBetween(windFarmMadrid, timeFrom, timeTo);
		assertThat(energyProductionsOfMadrid.size()).isEqualTo(2);

		timeTo = getTimeStamp(2018, 10, 5, 2, 1, windFarmMadrid.getZoneId()); //2018-10-5 02:01
		energyProductionsOfMadrid = hourlyProductionRepository.findByWindFarmWithTimestampBetween(windFarmMadrid, timeFrom, timeTo);
		assertThat(energyProductionsOfMadrid.size()).isEqualTo(3);

		timeFrom = getTimeStamp(2018, 10, 1, 1, 2, windFarmMadrid.getZoneId()); //2018-10-1 01:02
		energyProductionsOfMadrid = hourlyProductionRepository.findByWindFarmWithTimestampBetween(windFarmMadrid, timeFrom, timeTo);
		assertThat(energyProductionsOfMadrid.size()).isEqualTo(3);

		timeFrom = getTimeStamp(2018, 10, 1, 1, 1, windFarmMadrid.getZoneId()); //2018-10-1 01:02
		energyProductionsOfMadrid = hourlyProductionRepository.findByWindFarmWithTimestampBetween(windFarmMadrid, timeFrom, timeTo);
		assertThat(energyProductionsOfMadrid.size()).isEqualTo(4);
	}
	
}
