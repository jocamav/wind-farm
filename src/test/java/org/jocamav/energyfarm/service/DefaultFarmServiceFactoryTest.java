package org.jocamav.energyfarm.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DefaultFarmServiceFactory.class})
public class DefaultFarmServiceFactoryTest {

	@Autowired
	private FarmServiceFactory farmServiceFactory;
	
	@MockBean(name = "windFarmService")
	private FarmService windFarmService;
	
	@Test
	public void getServiceForWindFarm() {
		FarmService farmService = farmServiceFactory.getFarmService("wind");
		assertThat(farmService).isNotNull();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void getServiceForTypeNotImplemented() {
		farmServiceFactory.getFarmService("sun");
	}
	
}
