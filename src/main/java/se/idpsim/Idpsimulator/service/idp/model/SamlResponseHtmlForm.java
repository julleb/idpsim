package se.idpsim.Idpsimulator.service.idp.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SamlResponseHtmlForm {
    private String relayState;
    private String samlResponse;
    private String submitUrl;
}
