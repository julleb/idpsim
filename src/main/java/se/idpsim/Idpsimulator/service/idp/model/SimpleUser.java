package se.idpsim.Idpsimulator.service.idp.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import se.idpsim.Idpsimulator.utils.ObjectUtils;

@Builder
@Getter
@ToString
public final class SimpleUser implements SimulatorUser {
    private String userId;
    private String firstName;
    private String lastName;

    public SimpleUser(String userId, String firstName, String lastName) {
        ObjectUtils.requireNonNull(userId, "userId cannot be null");
        ObjectUtils.requireNonNull(firstName, "firstName cannot be null");
        ObjectUtils.requireNonNull(lastName, "lastName cannot be null");
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
