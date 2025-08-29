package se.idpsim.Idpsimulator.service.saml;

import java.util.List;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.MarshallingException;

class SamlServiceTest {

    private SamlService samlService;


    @BeforeEach
    void setup() throws Exception {
        samlService = new SamlService();
    }

    @Test
    void signSamlResponse() throws MarshallingException, TransformerException {
        SamlResponse samlResponse = SamlResponse.builder()
            .inResponseTo("12345")
            .issuer("issuer")
            .audience("myAud")
            .destination("destination")
            .nameId("myUserName")
            .assertions(List.of(SamlAssertion.builder()
                .name("yo")
                .value("abc")
                .build()))
            .build();

        samlService.signSamlResponse(samlResponse);

        String samlResponseString = SamlUtils.samlResponseToString(samlResponse);
        System.out.println(samlResponseString);
    }
}
