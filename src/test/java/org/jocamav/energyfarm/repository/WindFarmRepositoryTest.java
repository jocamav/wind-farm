package org.jocamav.energyfarm.repository;

import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

import org.hibernate.SessionFactory;
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
public class WindFarmRepositoryTest {

	private static final Logger log = LoggerFactory.getLogger(WindFarmRepositoryTest.class);
	
	@Autowired
	private WindFarmRepository windFarmRepository;
	
	@Before
	public void testA_saveSomeWindFarms() {

		ZoneId defaultZoneId = ZoneId.of("Europe/Madrid");
		// save a few of farms
		windFarmRepository.save(new WindFarm("Farm A", 10.0, defaultZoneId));
		windFarmRepository.save(new WindFarm("Farm B", 11.0, defaultZoneId));
		windFarmRepository.save(new WindFarm("Farm C", 12.0, defaultZoneId));
		windFarmRepository.save(new WindFarm("Farm D", 13.0, ZoneId.of("Europe/Zurich")));
		windFarmRepository.save(new WindFarm("Farm E", 14.0, ZoneId.of("Europe/Kiev")));
		windFarmRepository.flush();
	}
	
	@Test
	public void getAllWindFarms() {

		log.info("WindFarm found with findAll():");
		log.info("-------------------------------");
		Collection<WindFarm> allWindFarms = windFarmRepository.findAll();
		assertThat(allWindFarms.size()).isEqualTo(5);
		for (WindFarm windFarm : allWindFarms) {
			log.info(windFarm.toString());
		}
		log.info("");

	}
	
	@Test
	public void getOneSingleWindFarm() {
		Collection<WindFarm> allWindFarms = windFarmRepository.findAll();
		Long id = allWindFarms.iterator().next().getId();
		WindFarm windFarm = windFarmRepository.getOne(id);
		log.info("Customer found with findById(1L):");
		log.info("--------------------------------");
		log.info(windFarm.toString());
		log.info("");
		
		assertThat(windFarm.getId()).isEqualTo(id);
		assertThat(windFarm.getZoneId()).isNotNull();

	}
}
