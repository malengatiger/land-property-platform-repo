package com.lip.webserver.data;

import com.lip.states.LandState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;

public class LandTokenDTO {
    private String description;
    private X500Name maintainer;
    private String linearId;
    private String landStateIdentifier;

    public LandTokenDTO(String description, X500Name maintainer, String linearId, String landStateIdentifier) {
        this.description = description;
        this.maintainer = maintainer;
        this.linearId = linearId;
        this.landStateIdentifier = landStateIdentifier;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMaintainer(X500Name maintainer) {
        this.maintainer = maintainer;
    }

    public void setLinearId(String linearId) {
        this.linearId = linearId;
    }

    public void setLandStateIdentifier(String landStateIdentifier) {
        this.landStateIdentifier = landStateIdentifier;
    }

    public LandTokenDTO() {
    }

    public String getDescription() {
        return description;
    }

    public X500Name getMaintainer() {
        return maintainer;
    }

    public String getLinearId() {
        return linearId;
    }

    public String getLandStateIdentifier() {
        return landStateIdentifier;
    }
}
