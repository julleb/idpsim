package se.idpsim.Idpsimulator.web.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import se.idpsim.Idpsimulator.service.idp.DummySimulator;
import se.idpsim.Idpsimulator.service.session.UserSessionService;
import se.idpsim.Idpsimulator.utils.HttpServletRequestUtils;

@Controller
@Slf4j
@AllArgsConstructor
public class DummySimulatorController {

    private final DummySimulator dummySimulator;
    private final UserSessionService userSessionService;

    @GetMapping(value = DummySimulator.ENTITY_ID_SUFFIX, produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> getMetadata() {
        String hostUrl = HttpServletRequestUtils.getHostUrl();
        return ResponseEntity.ok()
            .body(dummySimulator.getSamlMetadata(hostUrl));
    }

    @RequestMapping(value = DummySimulator.SSO_URL_SUFFIX, produces = MediaType.TEXT_HTML_VALUE, method = {
        RequestMethod.GET, RequestMethod.POST })
    public String handleSamlRequestForm(
        @RequestParam(name = Constants.SAML_REQUEST_PARAM, required = false) String samlRequest,
        @RequestParam(name = Constants.RELAY_STATE_PARAM, required = false) String relayState) {
        log.debug("Received SAMLRequest: {}, RelayState: {}", samlRequest, relayState);

        userSessionService.createSession();
        userSessionService.addAttribute(Constants.SAML_REQUEST_PARAM, samlRequest);
        userSessionService.addAttribute(Constants.RELAY_STATE_PARAM, relayState);

        return "dummy-simulator" ;
    }

    @PostMapping(value = DummySimulator.SAML_RESPONSE_FORM_URL, produces = MediaType.TEXT_HTML_VALUE)
    public String createSamlResponseForm() {
        return null;
    }
}
