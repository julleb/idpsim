package se.idpsim.Idpsimulator.service.idp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import se.idpsim.Idpsimulator.service.saml.SamlMetadata;
import se.idpsim.Idpsimulator.service.saml.SamlResponse;
import se.idpsim.Idpsimulator.service.saml.SamlSigningService;

@Service
@Slf4j
public class DummySimulator {

    public static final String ENTITY_ID_SUFFIX = "/dummysimulator/metadata/0" ;
    public static final String SSO_URL_SUFFIX = "/dummysimulator/acs" ;
    private final SamlSigningService samlSigningService;

    DummySimulator(SamlSigningService samlSigningService) {
        this.samlSigningService = samlSigningService;
    }

    public String getSamlMetadata(String hostUrl) {

        String entityId = UriComponentsBuilder.fromPath("{hostUrl}" + ENTITY_ID_SUFFIX)
            .buildAndExpand(hostUrl)
            .toString();

        SamlMetadata samlMetadata = SamlMetadata.builder()
            .entityId(entityId)
            .singleSignOnServiceUrl(hostUrl + SSO_URL_SUFFIX)
            .singleLogoutService("todo") // TODO add SLO url
            .signingCertificate(samlSigningService.getSigningCertificate())
            .build();

        return samlMetadata.toString();
    }
}
