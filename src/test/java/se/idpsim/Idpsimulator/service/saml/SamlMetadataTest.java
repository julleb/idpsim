package se.idpsim.Idpsimulator.service.saml;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import se.idpsim.Idpsimulator.utils.KeystoreUtils;

class SamlMetadataTest {

    @BeforeEach
    void setup() {
        SamlUtils.init();
    }

    @Test
    void createSamlMetadata_shouldBeOk() throws Exception {
        KeyStore ks = KeystoreUtils.getKeyStore(SamlSigningService.keyStorePath, "");
        X509Certificate cert = KeystoreUtils.getAsX509Certificate(ks, SamlSigningService.keyStoreAlias);

        SamlMetadata metadata = SamlMetadata.builder().singleSignOnServiceUrl("abc")
            .entityId("yo")
            .singleLogoutService("no")
            .signingCertificate(cert)
            .build();

        Document doc = SamlUtils.toDocument(metadata.getEntityDescriptor());
        String xmlString = SamlUtils.documentToString(doc);
        System.out.println(xmlString);
    }
}
