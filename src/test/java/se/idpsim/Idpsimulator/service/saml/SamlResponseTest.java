package se.idpsim.Idpsimulator.service.saml;

import java.util.List;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.MarshallingException;

class SamlResponseTest {

    @Test
    void createSamlResponse() throws MarshallingException, TransformerException {
        SamlResponse samlResponse = SamlResponse.builder().inResponseTo("12345")
            .issuer("issuer")
            .audience("myAud")
            .destination("destination")
            .assertions(List.of(SamlAssertion.builder().name("yo").value("abc").build()))
            .build();

        String samlResponseString = SamlUtils.samlResponseToString(samlResponse);
        System.out.println(samlResponseString);
    }
}
