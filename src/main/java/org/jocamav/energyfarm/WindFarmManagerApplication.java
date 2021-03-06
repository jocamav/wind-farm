package org.jocamav.energyfarm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

import org.jocamav.energyfarm.entity.HourlyProduction;
import org.jocamav.energyfarm.entity.WindFarm;
import org.jocamav.energyfarm.repository.HourlyProductionRepository;
import org.jocamav.energyfarm.repository.WindFarmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;


@SpringBootApplication
public class WindFarmManagerApplication {

	private static final Logger log = LoggerFactory.getLogger(WindFarmManagerApplication.class);
	
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(WindFarmManagerApplication.class, args);
		generateRandomData(ctx);
	}

	private static void generateRandomData(ConfigurableApplicationContext ctx) {

		Environment environment = ctx.getBean(Environment.class);
		String[] profiles = environment.getActiveProfiles();
		for(int i=0; i < profiles.length; i++) {
			log.info(String.format("The active profiles are %s", profiles[i]));
		}
		WindFarmRepository windFarmRepository = ctx.getBean(WindFarmRepository.class);
		HourlyProductionRepository hourlyProductionRepository = ctx.getBean(HourlyProductionRepository.class);
		
		//Just to check the Hostname (issue with slow start time of Spring Boot)
		try {
			log.info(String.format("Hostname: %s", InetAddress.getLocalHost().getHostName()));
		} catch (UnknownHostException e) {
			log.warn(String.format("Hostname exception: %s", e.getMessage()));
		}
		
		//Init the Database
		if(windFarmRepository.findAll().size() == 0 ) {
			log.info("The Database is empty, let's create some data");
			WindFarm windFarmMadrid = windFarmRepository.save(new WindFarm("Farm of Madrid", 10.0, ZoneId.of("Europe/Madrid")));
			WindFarm windFarmZurich = windFarmRepository.save(new WindFarm("Farm of Zurich", 10.0, ZoneId.of("Europe/Zurich")));
			WindFarm windFarmKiev = windFarmRepository.save(new WindFarm("Farm of Kiev", 10.0, ZoneId.of("Europe/Kiev")));
			generateProduction(hourlyProductionRepository, windFarmMadrid);
			log.info(String.format("Created farm %s", windFarmMadrid.toString()));
			generateProduction(hourlyProductionRepository, windFarmZurich);
			log.info(String.format("Created farm %s", windFarmZurich.toString()));
			generateProduction(hourlyProductionRepository, windFarmKiev);
			log.info(String.format("Created farm %s", windFarmKiev.toString()));
		}
		log.info("Ready!!!!");
	}
	
	private static void generateProduction(HourlyProductionRepository hourlyProductionRepository, WindFarm windFarm) {
		LocalDate dateFrom = LocalDate.of(2018, 1, 1); 
		LocalDate dateTo  = LocalDate.of(2019, 1, 1); 
		LocalDate currentDay = dateFrom;
		while(!currentDay.isAfter(dateTo)) {
			ZonedDateTime zonedDateTime = currentDay.atStartOfDay().atZone(windFarm.getZoneId());
			while(currentDay.getDayOfMonth() == zonedDateTime.getDayOfMonth()) {
				hourlyProductionRepository.save(getRandomProduction(windFarm, zonedDateTime));
				zonedDateTime = zonedDateTime.plusHours(1);
			}
			currentDay = currentDay.plusDays(1L);
		}
	}
	
	private static HourlyProduction getRandomProduction(WindFarm windFarm, ZonedDateTime zonedDateTime) {
		return new HourlyProduction.Builder()
			.withWindFarm(windFarm)
			.withZonedDateTime(zonedDateTime)
			.withElectricityProduced(generateRandomValue(windFarm))
			.build();
			
	}
	
	private static Double generateRandomValue(WindFarm windFarm) {
		Random r = new Random();
		return windFarm.getCapacity() * r.nextDouble();
	}

}
