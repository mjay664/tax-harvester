package in.clear.tax_harvester.client;


import in.clear.tax_harvester.dto.SaveResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;


@Component
@FeignClient(name = "save-service", url = "https://clearsavebackend-dev-http.internal.cleartax.co")
public interface SaveFeignClient {

    @GetMapping(value = "/admin/user-dashboard-v2",
            headers = {
            "Content-type=application/json",
            "Authorization=Basic c2F2ZWFkbWluOnNlYXNvbmlzaGVyZQ=="
    })
    SaveResponse getInvestmentTransactionResponse(String userEmail);
}
