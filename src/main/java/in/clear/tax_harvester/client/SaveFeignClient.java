package in.clear.tax_harvester.client;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "save-service", url = "http://localhost:8082")
public interface SaveFeignClient {

}
