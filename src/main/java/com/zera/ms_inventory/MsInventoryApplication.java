package com.zera.ms_inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableScheduling
@org.springframework.boot.context.properties.EnableConfigurationProperties({
        com.zera.ms_inventory.infrastructure.admincore.AdminCoreProperties.class,
        com.zera.ms_inventory.infrastructure.prediction.PredictionProperties.class})
@SpringBootApplication
public class MsInventoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsInventoryApplication.class, args);
	}

}
