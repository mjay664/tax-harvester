package in.clear.tax_harvester.client;


import in.clear.tax_harvester.config.FeignConfig;
import in.clear.tax_harvester.dto.SaveResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Component
@FeignClient(name = "saveFeignClient", url = "https://clearsavebackend-prod-http.internal.cleartax.co", configuration = FeignConfig.class)
public interface SaveFeignClient {
    @GetMapping(value = "/admin/user-dashboard-v2", headers = "Content-type=application/json")
    SaveResponse getInvestmentTransactionResponse(@RequestParam("email") String userEmail);
}
