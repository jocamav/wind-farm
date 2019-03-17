package org.jocamav.energyfarm.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import org.jocamav.energyfarm.entity.HourlyProduction;
import org.jocamav.energyfarm.entity.WindFarm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
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
		hourlyProductionRepository.save(getHourlyProduction(windFarmMadrid, 4.9, "2018-10-05T01:01:00"));
		hourlyProductionRepository.save(getHourlyProduction(windFarmMadrid, 5.1, "2018-10-05T02:01:00"));
		hourlyProductionRepository.save(getHourlyProduction(windFarmMadrid, 4.1, "2018-10-01T01:01:00"));
		hourlyProductionRepository.save(getHourlyProduction(windFarmMadrid, 4.5, "2018-10-02T01:01:00"));
		windFarmZurich = windFarmRepository.save(new WindFarm("Farm D", 13.0, ZoneId.of("Europe/Zurich")));
		hourlyProductionRepository.save(getHourlyProduction(windFarmZurich, 5.1, "2018-10-05T01:01:00"));
		hourlyProductionRepository.save(getHourlyProduction(windFarmZurich, 5.1, "2018-10-05T02:01:00"));
		windFarmRepository.flush();
	}
	
	private HourlyProduction getHourlyProduction(WindFarm windfarm, double production, String dateAsString) {
		return new HourlyProduction.Builder()
				.withWindFarm(windfarm)
				.withElectricityProduced(production)
				.withLocalDate(dateAsString)
				.build();
	}
	
	private Timestamp getTimeStamp(String dateAsString, ZoneId zoneId) {
		LocalDateTime localDateTime = LocalDateTime.parse(dateAsString);
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
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

		Timestamp timeFrom = getTimeStamp("2018-10-02T00:00:00", windFarmMadrid.getZoneId()); //2018-10-2 00:00
		Timestamp timeTo = getTimeStamp("2018-10-05T02:00:00", windFarmMadrid.getZoneId()); //2018-10-5 02:00
		Collection<HourlyProduction> energyProductionsOfMadrid = hourlyProductionRepository.findByWindFarmWithTimestampBetween(windFarmMadrid, timeFrom, timeTo);
		assertThat(energyProductionsOfMadrid.size()).isEqualTo(2);
		checkOrderOfTimeStamps(energyProductionsOfMadrid);

		timeTo = getTimeStamp("2018-10-05T02:01:00", windFarmMadrid.getZoneId()); //2018-10-5 02:01
		energyProductionsOfMadrid = hourlyProductionRepository.findByWindFarmWithTimestampBetween(windFarmMadrid, timeFrom, timeTo);
		assertThat(energyProductionsOfMadrid.size()).isEqualTo(3);
		checkOrderOfTimeStamps(energyProductionsOfMadrid);
		
		timeFrom = getTimeStamp("2018-10-01T01:02:00", windFarmMadrid.getZoneId()); //2018-10-1 01:02
		energyProductionsOfMadrid = hourlyProductionRepository.findByWindFarmWithTimestampBetween(windFarmMadrid, timeFrom, timeTo);
		assertThat(energyProductionsOfMadrid.size()).isEqualTo(3);
		checkOrderOfTimeStamps(energyProductionsOfMadrid);

		timeFrom = getTimeStamp("2018-10-01T01:01:00", windFarmMadrid.getZoneId()); //2018-10-1 01:01
		energyProductionsOfMadrid = hourlyProductionRepository.findByWindFarmWithTimestampBetween(windFarmMadrid, timeFrom, timeTo);
		assertThat(energyProductionsOfMadrid.size()).isEqualTo(4);
		checkOrderOfTimeStamps(energyProductionsOfMadrid);
	}
	
	private void checkOrderOfTimeStamps(Collection<HourlyProduction> energyProductions) {
		long previousTimestamp = 0;
		for(HourlyProduction hourlyProduction : energyProductions) {
			assertThat(hourlyProduction.getTimestamp().getTime()).isGreaterThan(previousTimestamp);
			previousTimestamp = hourlyProduction.getTimestamp().getTime();
		}
	}
	
}
