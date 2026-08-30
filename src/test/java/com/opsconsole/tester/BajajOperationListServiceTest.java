package com.opsconsole.tester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsconsole.tester.config.BajajTesterProperties;
import com.opsconsole.tester.domain.BajajEnvironment;
import com.opsconsole.tester.dto.OperationListResponseDto;
import com.opsconsole.tester.service.BajajOperationListService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BajajOperationListServiceTest {

    @Test
    void fetchOperations_mockUat_returnsOperationList() {
        BajajTesterProperties properties = new BajajTesterProperties();
        properties.setMockMode(true);
        BajajOperationListService service = new BajajOperationListService(properties, new ObjectMapper());

        OperationListResponseDto response = service.fetchOperations(BajajEnvironment.UAT);

        assertThat(response.environment()).isEqualTo("UAT");
        assertThat(response.mockMode()).isTrue();
        assertThat(response.operations()).isNotEmpty();
        assertThat(response.operations().getFirst().publicUrl()).isNotBlank();
        assertThat(response.operations().getFirst().fullUrl()).contains("sauat.bajajfinserv.in/apis/");
    }
}
