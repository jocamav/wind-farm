package org.jocamav.energyfarm.repository;

import org.jocamav.energyfarm.entity.HourlyProduction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HourlyProductionRepository extends JpaRepository<HourlyProduction, Long> {

}
