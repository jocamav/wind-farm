package org.jocamav.energyfarm.repository;

import java.sql.Timestamp;
import java.util.List;

import org.jocamav.energyfarm.entity.HourlyProduction;
import org.jocamav.energyfarm.entity.WindFarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HourlyProductionRepository extends JpaRepository<HourlyProduction, Long> {
	List<HourlyProduction> findByWindFarm(WindFarm windFarm);
	
	@Query("select hp from HourlyProduction hp where hp.windFarm=?1 and hp.timestamp>=?2 and hp.timestamp<?3")
	List<HourlyProduction> findByWindFarmWithTimestampBetween(WindFarm windFarm, Timestamp timeFrom, Timestamp timeTo);
}
