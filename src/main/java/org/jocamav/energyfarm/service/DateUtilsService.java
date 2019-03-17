package org.jocamav.energyfarm.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;

public interface DateUtilsService {
	Timestamp getTimeStampFromLocalDate(LocalDate localDate, ZoneId zoneId);
}
