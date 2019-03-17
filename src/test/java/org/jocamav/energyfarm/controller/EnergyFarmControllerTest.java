package org.jocamav.energyfarm.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.jocamav.energyfarm.dto.CapacityPerDayDto;
import org.jocamav.energyfarm.dto.EnergyFarmDto;
import org.jocamav.energyfarm.dto.WindFarmDto;
import org.jocamav.energyfarm.service.FarmService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(EnergyFarmController.class)
@AutoConfigureRestDocs
public class EnergyFarmControllerTest {


	@Autowired
    private MockMvc mockMvc;
	
	@MockBean
	private FarmService farmService;

	@Before
	public void setUp() {
		Mockito.when(farmService.getEnergyFarmCapacity(eq(1L), any(LocalDate.class), any(LocalDate.class))).thenReturn(buildEnergyFarmDto());
		Mockito.when(farmService.getEnergyFarmCapacity(eq(2L), any(LocalDate.class), any(LocalDate.class))).thenThrow(new RuntimeException("Exception"));
	}
	
	private EnergyFarmDto buildEnergyFarmDto() {
		WindFarmDto energyFarmDto = new WindFarmDto();
		energyFarmDto.setId(1L);
		energyFarmDto.setProducedEnergy(20.0);
		energyFarmDto.setZoneId(ZoneId.of("Europe/Madrid"));
		List<CapacityPerDayDto> dailyCapacity = new ArrayList<>();
		dailyCapacity.add(new CapacityPerDayDto(LocalDate.parse("2017-01-02"), 3.5));
		dailyCapacity.add(new CapacityPerDayDto(LocalDate.parse("2017-01-03"), 4.5));
		dailyCapacity.add(new CapacityPerDayDto(LocalDate.parse("2017-01-04"), 5.5));
		dailyCapacity.add(new CapacityPerDayDto(LocalDate.parse("2017-01-05"), 6.5));
		energyFarmDto.setDailyCapacity(dailyCapacity);
		return energyFarmDto;
	}
	
	@Test
	public void getFarmCapacity() throws Exception{

		this.mockMvc.perform(
        		get("/api/farm/capacity/{type}/{id}","wind", "1")
        		.param("dateFrom", "2019-01-01")
        		.param("dateTo", "2019-03-30")
	        	.contentType(MediaType.APPLICATION_JSON)
        	).andDo(print())
        	.andExpect(status().isOk())
        	.andDo(document("{class-name}/{method-name}",
        			requestParameters(
            			parameterWithName("dateFrom").description("Date from in ISO format (YYYY-MM-DD) to get the result"),
            			parameterWithName("dateTo").description("Date to in ISO format (YYYY-MM-DD) to get the result")
        			),
        			pathParameters( 
        				parameterWithName("type").description("Type of farm (e.g. wind)"),
        				parameterWithName("id").description("ID of the farm")
        			),
        			responseFields(
	        			fieldWithPath("id").description("Id of the farm"),
	        			fieldWithPath("zoneId").description("Zone ID of the farm"),
	        			fieldWithPath("producedEnergy").description("Electricity produced of a farm"),
	        			fieldWithPath("dailyCapacity").description("The energy capacity of each day"),
	        			fieldWithPath("dailyCapacity[].day").description("Day to gather the capacity information"),
	        			fieldWithPath("dailyCapacity[].capacity").description(" The capacity factor, the actual amount of electricity produced, divided by the maximum possible amount of electricity")
        			))
        	);
	}
	
	@Test
	public void getFarmCapacityWithMissingParameter() throws Exception{
		this.mockMvc.perform(
        		get("/api/farm/capacity/{type}/{id}","wind", "2")
	        	.contentType(MediaType.APPLICATION_JSON)
        	).andDo(print())
        	.andExpect(status().isUnprocessableEntity())
        	.andDo(document("{class-name}/{method-name}",
    			responseFields(
	        			fieldWithPath("code").description("Code of the error"),
	        			fieldWithPath("description").description("Description of the error")
        		))
        	);
	}
	
	@Test
	public void getFarmCapacityForNotExistingFarm() throws Exception{
		this.mockMvc.perform(
        		get("/api/farm/capacity/{type}/{id}","wind", "2")
        		.param("dateFrom", "2019-01-01")
        		.param("dateTo", "2019-03-30")
	        	.contentType(MediaType.APPLICATION_JSON)
        	).andDo(print())
        	.andExpect(status().isUnprocessableEntity())
        	.andDo(document("{class-name}/{method-name}")
        	);
	}
}
