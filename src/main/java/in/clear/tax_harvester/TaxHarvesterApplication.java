package in.clear.tax_harvester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;



@SpringBootApplication
@EnableFeignClients
@ComponentScan({"in.clear.*"})
public class TaxHarvesterApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaxHarvesterApplication.class, args);
	}

}
