package org.jocamav.energyfarm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class DefaultFarmServiceFactory implements FarmServiceFactory {

	private final String FARM_WIND_TYPE = "wind";
	
	@Autowired
	@Qualifier("windFarmService")
	private FarmService windFarmService;

	public FarmService getFarmService(String type) {
		if(FARM_WIND_TYPE.equals(type)) {
			return windFarmService;
		}
		else {
			throw new IllegalArgumentException(String.format("Not bean defined for type <%s>", type));
		}
		
	}

}
