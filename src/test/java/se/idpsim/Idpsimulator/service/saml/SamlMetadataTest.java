package se.idpsim.Idpsimulator.service.saml;

import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Document;

class SamlMetadataTest {

    @BeforeEach
    void setup() {
        SamlUtils.init();
    }

    @Test
    void test() throws TransformerException, MarshallingException {
        SamlMetadata metadata = SamlMetadata.builder().singleSignOnServiceUrl("abc")
            .entityId("yo")
            .singleLogoutService("no")
            .build();

        Document doc = SamlUtils.toDocument(metadata.getEntityDescriptor());
        String xmlString = SamlUtils.documentToString(doc);
        System.out.println(xmlString);
    }
}
