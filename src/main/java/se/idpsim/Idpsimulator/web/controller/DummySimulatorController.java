package se.idpsim.Idpsimulator.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import se.idpsim.Idpsimulator.service.idp.DummySimulator;
import se.idpsim.Idpsimulator.service.idp.model.SimpleUser;
import se.idpsim.Idpsimulator.service.idp.model.UserAction;
import se.idpsim.Idpsimulator.utils.HttpServletRequestUtils;

@Controller
@Slf4j
@AllArgsConstructor
public class DummySimulatorController {

    private final DummySimulator dummySimulator;

    @Operation(summary = "Get SAML2 metadata for the Dummy IdP simulator")
    @GetMapping(value = DummySimulator.ENTITY_ID_SUFFIX, produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> getMetadata() {
        String hostUrl = HttpServletRequestUtils.getHostUrl();
        return ResponseEntity.ok()
            .body(dummySimulator.getSamlMetadata(hostUrl));
    }

    @Operation(summary = "Handle SAML authentication request and present user selection form")
    @RequestMapping(value = DummySimulator.SSO_URL_SUFFIX, produces = MediaType.TEXT_HTML_VALUE, method = {
        RequestMethod.GET, RequestMethod.POST })
    public String handleSamlRequestForm(Model model,
        @RequestParam(name = Constants.SAML_REQUEST_PARAM, required = false) String samlRequest,
        @RequestParam(name = Constants.RELAY_STATE_PARAM, required = false) String relayState) {
        log.debug("Received SAMLRequest: {}, RelayState: {}", samlRequest, relayState);

        dummySimulator.handleSamlRequest(samlRequest, relayState);
        model.addAttribute("users", dummySimulator.getUsers());
        model.addAttribute("formAction", DummySimulator.SAML_RESPONSE_FORM_URL);
        return "dummy-simulator";
    }

    @Operation(summary = "Create SAML response and present auto-submitting HTML form to the Service Provider")
    @PostMapping(value = DummySimulator.SAML_RESPONSE_FORM_URL, produces = MediaType.TEXT_HTML_VALUE)
    public String createSamlResponseForm(@ModelAttribute SimpleUser simpleUser, @ModelAttribute
        UserAction userAction, Model model) {
        log.debug("User selected: {}, action: {}", simpleUser, userAction);
        var htmlForm = dummySimulator.getSamlResponseHtmlForm(simpleUser, userAction);
        model.addAttribute("samlResponse", htmlForm.getSamlResponse());
        model.addAttribute("formAction", htmlForm.getSubmitUrl());
        model.addAttribute("relayState", htmlForm.getRelayState());
        return "saml-response-form";
    }
}
