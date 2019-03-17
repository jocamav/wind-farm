package org.jocamav.energyfarm.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface DateUtilsService {
	Timestamp getTimestampFromLocalDate(LocalDate localDate, ZoneId zoneId);
	Timestamp getTimestampFromZonedDateTime(ZonedDateTime zonedDateTime);
}
