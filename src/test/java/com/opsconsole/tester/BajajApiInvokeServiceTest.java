package com.opsconsole.tester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsconsole.tester.config.BajajTesterProperties;
import com.opsconsole.tester.dto.BajajInvokeRequest;
import com.opsconsole.tester.dto.BajajInvokeResponse;
import com.opsconsole.tester.service.BajajApiInvokeService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BajajApiInvokeServiceTest {

    @Test
    void invoke_mockMode_returnsPrettyJsonWithEcho() {
        BajajTesterProperties properties = new BajajTesterProperties();
        properties.setMockMode(true);
        BajajApiInvokeService service = new BajajApiInvokeService(properties, new ObjectMapper());

        BajajInvokeRequest request = new BajajInvokeRequest(
                "UAT",
                "authbre/authorization",
                "19LPRFUYTVERETJY",
                "S5AFOYRNUNZENGJCEQ1W81DJB55QK76M",
                "{\"mobile\":\"9999999999\"}"
        );

        BajajInvokeResponse response = service.invoke(request);

        assertThat(response.mockMode()).isTrue();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.durationMs()).isGreaterThanOrEqualTo(0);
        assertThat(response.decryptedBody()).contains("\"mock\" : true");
        assertThat(response.decryptedBody()).contains("\"mobile\" : \"9999999999\"");
        assertThat(response.error()).isNull();
        assertThat(response.requestUrl()).contains("authbre/authorization");
    }
}
