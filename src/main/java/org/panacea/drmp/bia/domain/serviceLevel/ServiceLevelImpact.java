package org.panacea.drmp.bia.domain.serviceLevel;


import lombok.Data;
import org.panacea.drmp.bia.domain.configFiles.AttackerType;
import org.panacea.drmp.bia.domain.impact.ImpactLevel;
import org.panacea.drmp.bia.utils.TokensBag;

import java.util.HashMap;
import java.util.Map;

@Data
public class ServiceLevelImpact {
    private String serviceLevelId;
    private Double impact;
    private ImpactLevel impactLevel;
    private TokensBag tokens;
    private Map<AttackerType, String> target;

    public ServiceLevelImpact(String sl, Double impact, Map<AttackerType, String> target) {
        this.serviceLevelId = sl;
        this.impact = impact;
        this.target = target;
        this.tokens = new TokensBag();
        if (target == null) {
            this.target = new HashMap<>();
            for (AttackerType a : AttackerType.values()) {
                this.target.put(a, null);
            }
        } else {
            this.target = target;
        }
    }

    @Override
    public String toString() {
        return this.serviceLevelId + "|" + this.impact + "|" + this.impactLevel + "|" + this.target + "|" + this.tokens.toString();
    }

}
