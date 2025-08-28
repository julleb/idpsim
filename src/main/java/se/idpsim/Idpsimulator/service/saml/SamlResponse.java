package se.idpsim.Idpsimulator.service.saml;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;

@Getter
public class SamlResponse {


    private String issuer;
    private String destination;
    private String inResponseTo;
    private List<SamlAssertion> assertions;
    private Response response;

    @Builder
    public SamlResponse(String issuer, String destination, String inResponseTo,
        List<SamlAssertion> assertions) {
        this.issuer = issuer;
        this.destination = destination;
        this.inResponseTo = inResponseTo;
        this.assertions = assertions;

        buildSamlResponse();
    }

    private void buildSamlResponse() {
        var builderFactory = SamlUtils.getXmlObjectBuilderFactory();

        Issuer issuer = createIssuer(builderFactory);

        response = (Response) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME)
            .buildObject(Response.DEFAULT_ELEMENT_NAME);
        response.setIssuer(issuer);
        response.setInResponseTo(this.inResponseTo);
        response.setID(UUID.randomUUID().toString());
    }

    private Issuer createIssuer(XMLObjectBuilderFactory builderFactory) {
        var issuerBuilder = (IssuerBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        var issuer = issuerBuilder.buildObject();
        issuer.setValue(this.issuer);
        return issuer;
    }
}
