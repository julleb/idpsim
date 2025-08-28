package se.idpsim.Idpsimulator.service.saml;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SamlAssertion {

    private String friendlyName;
    private String name;
    private String value;
}
