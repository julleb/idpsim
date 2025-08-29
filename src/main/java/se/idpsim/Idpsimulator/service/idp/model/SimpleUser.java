package se.idpsim.Idpsimulator.service.idp.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public final class SimpleUser implements SimulatorUser {
    private String userId;
    private String firstName;
    private String lastName;
}
