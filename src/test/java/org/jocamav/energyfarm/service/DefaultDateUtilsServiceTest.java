package org.jocamav.energyfarm.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.TimeZone;

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
	public void getTimestampFromLocalDateInDifferentCountries() {
		LocalDate localDate = LocalDate.parse("2017-01-01");
		Timestamp timestampInLondon = dateUtilsService.getTimestampFromLocalDate(localDate, ZoneId.of("Europe/London"));
		Timestamp timestampInMadrid = dateUtilsService.getTimestampFromLocalDate(localDate, ZoneId.of("Europe/Madrid"));
		Timestamp timestampInKiev = dateUtilsService.getTimestampFromLocalDate(localDate, ZoneId.of("Europe/Kiev"));

		//The midnight Timestamp for the same day is lower in western countries
		assertThat(timestampInLondon.getTime()).isGreaterThan(timestampInMadrid.getTime());
		assertThat(timestampInLondon.getTime()).isGreaterThan(timestampInKiev.getTime());
		assertThat(timestampInMadrid.getTime()).isGreaterThan(timestampInKiev.getTime());
		
		//The difference should be one hour
		assertThat(timestampInMadrid.getTime() + HOUR_IN_MILLISECS).isEqualTo(timestampInLondon.getTime());
		assertThat(timestampInKiev.getTime() + HOUR_IN_MILLISECS).isEqualTo(timestampInMadrid.getTime());
	}
	
	@Test
	public void getTimestampFromZoneInDifferentCountries() {
		ZonedDateTime zonedDateTimeInLondon = LocalDateTime.parse("2017-12-31T23:00").atZone(ZoneId.of("Europe/London"));
		ZonedDateTime zonedDateTimeInMadrid = LocalDateTime.parse("2018-01-01T00:00").atZone(ZoneId.of("Europe/Madrid"));
		ZonedDateTime zonedDateTimeInKiev = LocalDateTime.parse("2018-01-01T01:00").atZone(ZoneId.of("Europe/Kiev"));
		Timestamp timestampInLondon = dateUtilsService.getTimestampFromZonedDateTime(zonedDateTimeInLondon);
		Timestamp timestampInMadrid = dateUtilsService.getTimestampFromZonedDateTime(zonedDateTimeInMadrid);
		Timestamp timestampInKiev = dateUtilsService.getTimestampFromZonedDateTime(zonedDateTimeInKiev);

		//Timestamp should be the same in all the countries
		assertThat(timestampInLondon.getTime()).isEqualTo(timestampInMadrid.getTime());
		assertThat(timestampInLondon.getTime()).isEqualTo(timestampInKiev.getTime());
		assertThat(timestampInMadrid.getTime()).isEqualTo(timestampInKiev.getTime());

		Calendar calendarInLondon = Calendar.getInstance();
		Calendar calendarInMadrid = Calendar.getInstance();
		Calendar calendarInKiev = Calendar.getInstance();
		calendarInLondon.setTimeInMillis(timestampInLondon.getTime());
		calendarInLondon.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/London")));
		calendarInMadrid.setTimeInMillis(timestampInMadrid.getTime());
		calendarInMadrid.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Madrid")));
		calendarInKiev.setTimeInMillis(timestampInKiev.getTime());
		calendarInKiev.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Kiev")));

		assertThat(calendarInLondon.get(Calendar.YEAR)).isEqualTo(2017);
		assertThat(calendarInMadrid.get(Calendar.YEAR)).isEqualTo(2018);
		assertThat(calendarInKiev.get(Calendar.YEAR)).isEqualTo(2018);

		assertThat(calendarInLondon.get(Calendar.MONTH)).isEqualTo(11);
		assertThat(calendarInMadrid.get(Calendar.MONTH)).isEqualTo(0);
		assertThat(calendarInKiev.get(Calendar.MONTH)).isEqualTo(0);

		assertThat(calendarInLondon.get(Calendar.DAY_OF_MONTH)).isEqualTo(31);
		assertThat(calendarInMadrid.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
		assertThat(calendarInKiev.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
	}
	
	@Test
	public void getTimestampWhenHourIsModifiedOneHourLess() {
		int year = 2018;
		int month = 10;
		int day = 28;
		int hour = 2;
		int minute = 59;
		ZonedDateTime dateTimeInMadridBeforeChange = LocalDate
				.parse(String.format("%d-%d-%d",year,month,day))
				.atTime(hour, minute)
				.atZone(ZoneId.of("Europe/Madrid"));
		Timestamp timestampInMadridBeforeChange = dateUtilsService.getTimestampFromZonedDateTime(dateTimeInMadridBeforeChange);
		Timestamp timestampInMadridAfterChange = dateUtilsService.getTimestampFromZonedDateTime(dateTimeInMadridBeforeChange.plusMinutes(1));
		
		assertTimeStampNextMinute(timestampInMadridBeforeChange, timestampInMadridAfterChange);
		Calendar calendarBeforeChange = Calendar.getInstance();
		Calendar calendarAfterChange = Calendar.getInstance();
		calendarBeforeChange.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Madrid")));
		calendarBeforeChange.setTimeInMillis(timestampInMadridBeforeChange.getTime());
		calendarAfterChange.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Madrid")));
		calendarAfterChange.setTimeInMillis(timestampInMadridAfterChange.getTime());
		
		assertDate(year, month, day, calendarBeforeChange, calendarAfterChange);
		
		//Hour remains the same from 2:59 to 2:00
		assertThat(calendarBeforeChange.get(Calendar.HOUR)).isEqualTo(calendarAfterChange.get(Calendar.HOUR));
		assertThat(calendarBeforeChange.get(Calendar.HOUR)).isEqualTo(hour);
		assertThat(calendarBeforeChange.get(Calendar.MINUTE)).isEqualTo(minute);
		assertThat(calendarAfterChange.get(Calendar.MINUTE)).isEqualTo(0);
	}


	@Test
	public void getTimestampWhenHourIsModifiedOneHourMore() {
		int year = 2018;
		int month = 3;
		int day = 25;
		int hour = 1;
		int minute = 59;
		ZonedDateTime dateTimeInMadridBeforeChange = LocalDate
				.parse(String.format("%d-0%d-%d",year,month,day))
				.atTime(hour, minute)
				.atZone(ZoneId.of("Europe/Madrid"));
		Timestamp timestampInMadridBeforeChange = dateUtilsService.getTimestampFromZonedDateTime(dateTimeInMadridBeforeChange);
		Timestamp timestampInMadridAfterChange = dateUtilsService.getTimestampFromZonedDateTime(dateTimeInMadridBeforeChange.plusMinutes(1));
		
		assertTimeStampNextMinute(timestampInMadridBeforeChange, timestampInMadridAfterChange);
		Calendar calendarBeforeChange = Calendar.getInstance();
		Calendar calendarAfterChange = Calendar.getInstance();
		calendarBeforeChange.setTimeInMillis(timestampInMadridBeforeChange.getTime());
		calendarBeforeChange.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Madrid")));
		calendarAfterChange.setTimeInMillis(timestampInMadridAfterChange.getTime());
		calendarAfterChange.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Madrid")));
		
		assertDate(year, month, day, calendarBeforeChange, calendarAfterChange);
		
		//Hour changes from 1:59 to 3:00
		assertThat(calendarBeforeChange.get(Calendar.HOUR)).isEqualTo(hour);
		assertThat(calendarAfterChange.get(Calendar.HOUR)).isEqualTo(hour+2); //2 hours more!
		assertThat(calendarBeforeChange.get(Calendar.MINUTE)).isEqualTo(minute);
		assertThat(calendarAfterChange.get(Calendar.MINUTE)).isEqualTo(0);
	}
	
	private void assertTimeStampNextMinute(Timestamp timestampInMadridBeforeChange,
			Timestamp timestampInMadridAfterChange) {
		assertThat(timestampInMadridBeforeChange.getTime() + 60000).isEqualTo(timestampInMadridAfterChange.getTime());
	}

	private void assertDate(int year, int month, int day, Calendar calendarBeforeChange, Calendar calendarAfterChange) {
		assertThat(calendarBeforeChange.get(Calendar.DAY_OF_MONTH)).isEqualTo(calendarAfterChange.get(Calendar.DAY_OF_MONTH));
		assertThat(calendarBeforeChange.get(Calendar.DAY_OF_MONTH)).isEqualTo(day);
		assertThat(calendarBeforeChange.get(Calendar.MONTH)).isEqualTo(calendarAfterChange.get(Calendar.MONTH));
		assertThat(calendarBeforeChange.get(Calendar.MONTH)).isEqualTo(month-1);
		assertThat(calendarBeforeChange.get(Calendar.YEAR)).isEqualTo(calendarAfterChange.get(Calendar.YEAR));
		assertThat(calendarBeforeChange.get(Calendar.YEAR)).isEqualTo(year);
	}

}
