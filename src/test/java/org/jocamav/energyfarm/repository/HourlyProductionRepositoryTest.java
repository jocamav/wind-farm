package org.jocamav.energyfarm.repository;

import java.time.ZoneId;
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
		hourlyProductionRepository.save(new HourlyProduction(windFarmMadrid, Calendar.getInstance().getTimeInMillis(), 4.1));
		hourlyProductionRepository.save(new HourlyProduction(windFarmMadrid, Calendar.getInstance().getTimeInMillis(), 4.4));
		hourlyProductionRepository.save(new HourlyProduction(windFarmMadrid, Calendar.getInstance().getTimeInMillis(), 1.1));
		hourlyProductionRepository.save(new HourlyProduction(windFarmMadrid, Calendar.getInstance().getTimeInMillis(), 4.1));
		windFarmZurich = windFarmRepository.save(new WindFarm("Farm D", 13.0, ZoneId.of("Europe/Zurich")));
		hourlyProductionRepository.save(new HourlyProduction(windFarmZurich, Calendar.getInstance().getTimeInMillis(), 4.1));
		hourlyProductionRepository.save(new HourlyProduction(windFarmZurich, Calendar.getInstance().getTimeInMillis(), 3.1));
		windFarmRepository.flush();
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
	
}
