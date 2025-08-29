package se.idpsim.Idpsimulator.service.idp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DummySimulatorTest {

    @Autowired
    private DummySimulator dummySimulator;

    @Test
    void getSamlMetadata_shouldReturnOk() {
        String metadata = dummySimulator.getSamlMetadata("http://localhost:8080");
        System.out.println(metadata);
    }
}
