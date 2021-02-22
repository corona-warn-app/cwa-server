package app.coronawarn.server.services.distribution.statistics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StatisticsJsonStringObjectTest {

	StatisticsJsonStringObject statisticsJsonStringObject;

	@Test
	void testGetMethodInfectionsReportedDaily(){
		this.statisticsJsonStringObject = new StatisticsJsonStringObject();
		statisticsJsonStringObject.setInfectionsReportedDaily(70200);
		Assertions.assertEquals(70200, statisticsJsonStringObject.getInfectionsReportedDaily());
	}
	@Test
	void testAppDownloads7DaysGrowthrate(){
		this.statisticsJsonStringObject = new StatisticsJsonStringObject();
		statisticsJsonStringObject.setAppDownloads7daysGrowthrate(10);
		Assertions.assertEquals(10, statisticsJsonStringObject.getAppDownloads7daysGrowthrate());
	}
	@Test
	void testEffectiveDate(){
		this.statisticsJsonStringObject = new StatisticsJsonStringObject();
		statisticsJsonStringObject.setEffectiveDate("10 january");
		Assertions.assertEquals("10 january", statisticsJsonStringObject.getEffectiveDate());
	}

}
