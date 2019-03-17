package org.jocamav.energyfarm.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DefaultDateUtilsService.class})
public class DefaultDateUtilsServiceTest {
	
	private static final int HOUR_IN_MILLISECS = 1000 * 60 * 60;
	
	@Autowired
	DateUtilsService dateUtilsService;
	
	@Test
	public void getTimestampFromLocalDate() {
		LocalDate localDate = LocalDate.parse("2017-01-01");
		Timestamp timestampInLondon = dateUtilsService.getTimeStampFromLocalDate(localDate, ZoneId.of("Europe/London"));
		Timestamp timestampInMadrid = dateUtilsService.getTimeStampFromLocalDate(localDate, ZoneId.of("Europe/Madrid"));
		Timestamp timestampInKiev = dateUtilsService.getTimeStampFromLocalDate(localDate, ZoneId.of("Europe/Kiev"));

		//The midnight Timestamp for the same day is lower in western countries
		assertThat(timestampInLondon.getTime()).isGreaterThan(timestampInMadrid.getTime());
		assertThat(timestampInLondon.getTime()).isGreaterThan(timestampInKiev.getTime());
		assertThat(timestampInMadrid.getTime()).isGreaterThan(timestampInKiev.getTime());
		
		//The difference should be one hour
		assertThat(timestampInMadrid.getTime() + HOUR_IN_MILLISECS).isEqualTo(timestampInLondon.getTime());
		assertThat(timestampInKiev.getTime() + HOUR_IN_MILLISECS).isEqualTo(timestampInMadrid.getTime());
	}

}
