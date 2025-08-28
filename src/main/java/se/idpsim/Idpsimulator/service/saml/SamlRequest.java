package se.idpsim.Idpsimulator.service.saml;

import java.io.IOException;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.parsers.ParserConfigurationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.xml.sax.SAXException;
import se.idpsim.Idpsimulator.utils.ObjectUtils;

@Builder
@Getter
@AllArgsConstructor
public class SamlRequest {

    private String issuer;
    private String assertionConsumerServiceUrl;
    private String id;

    @Builder(builderMethodName = "fromString", builderClassName = "fromString")
    public SamlRequest(String encodedSamlRequest) {
        ObjectUtils.requireNonEmpty(encodedSamlRequest, "encodedSamlRequest cannot be empty");

        byte [] decodeSamlRequest = SamlUtils.decodeSamlRequest(encodedSamlRequest);

        try {
            AuthnRequest authnRequest = (AuthnRequest) SamlUtils.toXmlObject(decodeSamlRequest);
            issuer = authnRequest.getIssuer().getValue();
            assertionConsumerServiceUrl = authnRequest.getAssertionConsumerServiceURL();
            id = authnRequest.getID();
        } catch (ParserConfigurationException|IOException|SAXException|UnmarshallingException e) {
            throw new IllegalArgumentException("Could not parse SAML request", e);
        }
    }

}
