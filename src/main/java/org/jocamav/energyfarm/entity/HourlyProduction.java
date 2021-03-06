package org.jocamav.energyfarm.entity;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class HourlyProduction {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    @ManyToOne
	private WindFarm windFarm;
	private Timestamp timestamp;
	private Double electricityProduced;
	
	public HourlyProduction() {
	}

	public HourlyProduction(WindFarm windFarm, Timestamp timestamp, Double electricityProduced) {
		super();
		this.windFarm = windFarm;
		this.timestamp = timestamp;
		this.electricityProduced = electricityProduced;
	}

	public WindFarm getWindFarm() {
		return windFarm;
	}

	public void setWindFarm(WindFarm windFarm) {
		this.windFarm = windFarm;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public Double getElectricityProduced() {
		return electricityProduced;
	}

	public void setElectricityProduced(Double electricityProduced) {
		this.electricityProduced = electricityProduced;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return String.format("HourlyProduction [id=%d, windFarm=%d, timestamp=%s, electricityProduced=%f]", 
				id, windFarm.getId(), timestamp, electricityProduced);
	}
	
	public static class Builder {
		private Long id;
		private WindFarm windFarm;
		private ZonedDateTime zonedDateTime;
		private Double electricityProduced;
	
		public Builder withId(Long id) {
			this.id = id;
			return this;
		}
		
		public Builder withWindFarm(WindFarm windFarm) {
			this.windFarm = windFarm;
			return this;
		}

		public Builder withElectricityProduced(Double electricityProduced) {
			this.electricityProduced = electricityProduced;
			return this;
		}
		
		public Builder withZonedDateTime(ZonedDateTime zonedDateTime) {
			this.zonedDateTime = zonedDateTime;
			return this;
		}
		
		public HourlyProduction build() {
			HourlyProduction hourlyProduction =  new HourlyProduction(windFarm, Timestamp.from(zonedDateTime.toInstant()), electricityProduced);
			hourlyProduction.setId(id);
			return hourlyProduction;
		}
	}
	
}
