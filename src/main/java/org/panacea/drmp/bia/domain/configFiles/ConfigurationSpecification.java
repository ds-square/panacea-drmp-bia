
package org.panacea.drmp.bia.domain.configFiles;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.panacea.drmp.bia.domain.impact.ImpactLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@SuppressWarnings("unused")
@JsonDeserialize(using = ConfigurationSpecificationDeserializer.class)
public class ConfigurationSpecification {

    private List<Attacker> attackers;
    private String environment;
    private String fileType;
    private Map<ImpactLevel, Double> impactThresholdTable;
    private String snapshotId;
    private String snapshotTime;

    public ConfigurationSpecification(String environment, String fileType, String snapshotId, String snapshotTime) {
        this.environment = environment;
        this.fileType = fileType;
        this.snapshotId = snapshotId;
        this.snapshotTime = snapshotTime;
        this.attackers = new ArrayList<>();
        this.impactThresholdTable = new HashMap<>();
    }

    public void addImpactLevel(ImpactLevel impactLevel, Double threshold) {
        this.impactThresholdTable.put(impactLevel, threshold);
    }

    public Double getImpactThreshold(ImpactLevel impactLevel) {
        return this.impactThresholdTable.get(impactLevel);
    }

    public void addAttacker(Attacker a) {
        this.attackers.add(a);
    }
}
