package org.jocamav.energyfarm.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;

@Service
public class DefaultDateUtilsService implements DateUtilsService {

	public Timestamp getTimestampFromLocalDate(LocalDate localDate, ZoneId zoneId) {
		ZonedDateTime zonedDateTime = localDate.atStartOfDay(zoneId);
		return getTimestampFromZonedDateTime(zonedDateTime);
	}

	public Timestamp getTimestampFromZonedDateTime(ZonedDateTime zonedDateTime) {
		return Timestamp.from(zonedDateTime.toInstant());
	}

}
