package se.idpsim.Idpsimulator.service.saml;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.MarshallingException;

class SamlResponseTest {

    @BeforeEach
    void setup() {
        OpenSamlConfig.init();
    }

    @Test
    void createSamlResponse() throws MarshallingException, TransformerException {
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

        String samlResponseString = SamlUtils.samlResponseToString(samlResponse);
        System.out.println(samlResponseString);
    }

    @Test
    void createSamlResponse_shouldThrowError_whenMissingFields() {
        var builder = SamlResponse.builder()
            .inResponseTo("12345")
            .issuer("issuer")
            .audience("myAud")
            .destination("destination")
            .nameId("myUserName")
            .assertions(List.of(SamlAssertion.builder()
                .name("yo")
                .value("abc")
                .build()));

        builder.build();

        assertThrows(IllegalArgumentException.class, () -> builder.inResponseTo(null).build());
        assertThrows(IllegalArgumentException.class, () -> builder.issuer(null).build());
        assertThrows(IllegalArgumentException.class, () -> builder.audience(null).build());
        assertThrows(IllegalArgumentException.class, () -> builder.destination(null).build());
        assertThrows(IllegalArgumentException.class, () -> builder.nameId(null).build());
        assertThrows(IllegalArgumentException.class, () -> builder.assertions(null).build());
        
    }
}
