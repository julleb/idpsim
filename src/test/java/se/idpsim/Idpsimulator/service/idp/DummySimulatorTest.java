package se.idpsim.Idpsimulator.service.idp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.idpsim.Idpsimulator.TestData;
import se.idpsim.Idpsimulator.service.exception.ServiceException;
import se.idpsim.Idpsimulator.service.idp.model.SamlResponseHtmlForm;
import se.idpsim.Idpsimulator.service.idp.model.SimpleUser;

@SpringBootTest
class DummySimulatorTest {

    @Autowired
    private DummySimulator dummySimulator;

    @Test
    void getSamlMetadata_shouldReturnOk() {
        String metadata = dummySimulator.getSamlMetadata("http://localhost:8080");
        System.out.println(metadata);
    }

    @Test
    void getSamlResponseHtmlForm_shouldThrowError_whenSamlRequestNotExists() {
        SimpleUser simpleUser = SimpleUser.builder()
            .userId("abc123")
            .firstName("Lola")
            .lastName("Kanin")
            .build();
        assertThrows(ServiceException.class, () -> dummySimulator.getSamlResponseHtmlForm(simpleUser));
    }

    @Test
    void getSamlResponseHtmlForm_shouldThrowError_whenBadFormattedSamlRequest() {
        dummySimulator.handleSamlRequest("badRequest", "abcRelay");
        SimpleUser simpleUser = SimpleUser.builder()
            .userId("abc123")
            .firstName("Lola")
            .lastName("Kanin")
            .build();
        assertThrows(ServiceException.class, () -> dummySimulator.getSamlResponseHtmlForm(simpleUser));
    }

    @Test
    void getSamlResponseHtmlForm_shouldReturnOk() {
        String relayState = "relayState123";
        dummySimulator.handleSamlRequest(TestData.ENCODED_SAML_REQUEST_FROM_SKATTEVERKET, relayState);
        SamlResponseHtmlForm htmlForm = dummySimulator.getSamlResponseHtmlForm(SimpleUser.builder()
                .userId("abc123")
                .firstName("Lola")
                .lastName("Kanin")
            .build());

        assertEquals(relayState, htmlForm.getRelayState());
        assertNotNull(htmlForm.getSamlResponse());
        assertEquals("https://sso.skatteverket.se/saml/sp/profile/post/acs", htmlForm.getSubmitUrl());

    }
}
