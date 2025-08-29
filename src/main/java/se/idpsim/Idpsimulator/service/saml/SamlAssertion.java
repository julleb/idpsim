package se.idpsim.Idpsimulator.service.saml;

import lombok.Builder;
import lombok.Getter;
import se.idpsim.Idpsimulator.utils.ObjectUtils;

@Getter
@Builder
public class SamlAssertion {

    private String friendlyName;
    private String name;
    private String value;

    public SamlAssertion(String friendlyName, String name, String value) {
        ObjectUtils.requireNonEmpty(name, "name cannot be empty");
        ObjectUtils.requireNonEmpty(value, "value cannot be empty");
        this.friendlyName = friendlyName;
        this.name = name;
        this.value = value;
    }
}
