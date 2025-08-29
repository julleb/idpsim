package se.idpsim.Idpsimulator.service.idp;

import java.util.Base64;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import se.idpsim.Idpsimulator.service.idp.model.SamlResponseHtmlForm;
import se.idpsim.Idpsimulator.service.idp.model.SimpleUser;
import se.idpsim.Idpsimulator.service.saml.SamlAssertion;
import se.idpsim.Idpsimulator.service.saml.SamlMetadata;
import se.idpsim.Idpsimulator.service.saml.SamlRequest;
import se.idpsim.Idpsimulator.service.saml.SamlResponse;
import se.idpsim.Idpsimulator.service.saml.SamlSigningService;
import se.idpsim.Idpsimulator.utils.ObjectUtils;

@Service
@Slf4j
public class DummySimulator {

    public static final String ENTITY_ID_SUFFIX = "/dummysimulator/metadata/0" ;
    public static final String SSO_URL_SUFFIX = "/dummysimulator/req/0";
    public static final String SAML_RESPONSE_FORM_URL = "/dummysimulator/resp/0";
    private final SamlSigningService samlSigningService;

    DummySimulator(SamlSigningService samlSigningService) {
        this.samlSigningService = samlSigningService;
    }

    public SamlResponseHtmlForm getSamlResponseHtmlForm(SamlRequest samlRequest, String relayState, SimpleUser simpleUser, String hostUrl) {
        ObjectUtils.requireNonNull(samlRequest, "samlRequest cannot be null");
        ObjectUtils.requireNonNull(simpleUser, "simpleUser cannot be null");

        String entityId = getEntityId(hostUrl);
        List<SamlAssertion> assertions = createAssertions(simpleUser);
        SamlResponse samlResponse = SamlResponse.builder()
            .issuer(entityId)
            .destination(samlRequest.getAssertionConsumerServiceUrl())
            .inResponseTo(samlRequest.getId())
            .audience(samlRequest.getIssuer())
            .nameId(simpleUser.getUserId())
            .assertions(assertions)
            .build();

        samlSigningService.signSamlResponse(samlResponse);

        String samlResponseString = samlResponse.toString();
        String encodedSamlResponse = Base64.getEncoder()
            .encodeToString(samlResponseString.getBytes());

        return SamlResponseHtmlForm.builder()
            .relayState(relayState)
            .samlResponse(encodedSamlResponse)
            .submitUrl(samlRequest.getAssertionConsumerServiceUrl())
            .build();
    }

    private List<SamlAssertion> createAssertions(SimpleUser simpleUser) {
        return List.of(
            SamlAssertion.builder()
                .value(simpleUser.getFirstName())
                .name("firstName")
                .friendlyName("firstName")
                .build(),
            SamlAssertion.builder()
                .value(simpleUser.getLastName())
                .name("lastName")
                .friendlyName("lastName")
                .build()
        );
    }

    public List<SimpleUser> getUsers() {
        return List.of(
            SimpleUser.builder().userId("user1").firstName("Alice").lastName("Andersson").build(),
            SimpleUser.builder().userId("user2").firstName("Bob").lastName("Bengtsson").build()
        );
    }

    private String getEntityId(String hostUrl) {
        return UriComponentsBuilder.fromPath("{hostUrl}" + ENTITY_ID_SUFFIX)
            .buildAndExpand(hostUrl)
            .toString();
    }

    public String getSamlMetadata(String hostUrl) {

        String entityId = getEntityId(hostUrl);

        SamlMetadata samlMetadata = SamlMetadata.builder()
            .entityId(entityId)
            .singleSignOnServiceUrl(hostUrl + SSO_URL_SUFFIX)
            .singleLogoutService("todo") // TODO add SLO url
            .signingCertificate(samlSigningService.getSigningCertificate())
            .build();

        return samlMetadata.toString();
    }
}
