package org.jocamav.energyfarm.controller;

import java.time.LocalDate;

import org.jocamav.energyfarm.dto.EnergyFarmDto;
import org.jocamav.energyfarm.dto.Error;
import org.jocamav.energyfarm.service.FarmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/farm")
public class EnergyFarmController {
	
	private static final Logger log = LoggerFactory.getLogger(EnergyFarmController.class);
	
	@Autowired
	private FarmService farmService;
	
	@GetMapping("/capacity/{type}/{id}")
	public EnergyFarmDto getFarmCapacityPerDay(@PathVariable String type, @PathVariable Long id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
		log.info(String.format("Getting capacity of %s farm <%d> from %s to %s", type, id, dateFrom, dateTo));
		return farmService.getEnergyFarmCapacity(id);
	}
	
	@ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Error handleExceptions(Exception e) {
        return new Error(HttpStatus.UNPROCESSABLE_ENTITY.name(), e.getMessage());
    }

}
