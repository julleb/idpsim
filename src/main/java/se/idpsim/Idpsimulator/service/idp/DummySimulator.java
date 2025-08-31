package se.idpsim.Idpsimulator.service.idp;

import java.util.Base64;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import se.idpsim.Idpsimulator.service.exception.ServiceException;
import se.idpsim.Idpsimulator.service.idp.model.SamlResponseHtmlForm;
import se.idpsim.Idpsimulator.service.idp.model.SimpleUser;
import se.idpsim.Idpsimulator.service.saml.SamlAssertion;
import se.idpsim.Idpsimulator.service.saml.SamlMetadata;
import se.idpsim.Idpsimulator.service.saml.SamlRequest;
import se.idpsim.Idpsimulator.service.saml.SamlResponse;
import se.idpsim.Idpsimulator.service.saml.SamlSigningService;
import se.idpsim.Idpsimulator.service.session.UserSessionService;
import se.idpsim.Idpsimulator.utils.HttpServletRequestUtils;
import se.idpsim.Idpsimulator.utils.ObjectUtils;
import se.idpsim.Idpsimulator.web.controller.Constants;

@Service
@Slf4j
@AllArgsConstructor
public class DummySimulator {

    public static final String ENTITY_ID_SUFFIX = "/dummysimulator/metadata/0" ;
    public static final String SSO_URL_SUFFIX = "/dummysimulator/req/0";
    public static final String SAML_RESPONSE_FORM_URL = "/dummysimulator/resp/0";
    private final SamlSigningService samlSigningService;
    private final UserSessionService userSessionService;


    public SamlResponseHtmlForm getSamlResponseHtmlForm(SimpleUser simpleUser) {
        ObjectUtils.requireNonNull(simpleUser, "simpleUser cannot be null");

        String encodedSamlReq = (String) userSessionService.getAttribute(Constants.SAML_REQUEST_PARAM)
            .orElseThrow(() -> new ServiceException(
                "No SAMLRequest found in user session, cannot proceed"));

        String relayState = (String) userSessionService.getAttribute(Constants.RELAY_STATE_PARAM)
            .orElse("");

        userSessionService.invalidateSession();

        SamlRequest samlRequest = SamlRequest.fromString()
            .encodedSamlRequest(encodedSamlReq).build();

        String hostUrl = HttpServletRequestUtils.getHostUrl();
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

    public void handleSamlRequest(String samlRequest, String relayState) {
        userSessionService.createSession();
        userSessionService.addAttribute(Constants.SAML_REQUEST_PARAM, samlRequest);
        userSessionService.addAttribute(Constants.RELAY_STATE_PARAM, relayState);
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
