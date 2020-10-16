
package org.panacea.drmp.bia.domain.serviceLevel;

import lombok.Data;
import org.panacea.drmp.bia.domain.query.input.QueryInput;
import org.panacea.drmp.bia.domain.query.input.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@SuppressWarnings("unused")
public class ServiceLevelInventory {

    private String environment;
    private String fileType;
    private List<ServiceLevel> serviceLevels;
    private String snapshotId;
    private String snapshotTime;

    private Map<String, ServiceLevel> serviceLevelMap;


    public void computeMapImpact(QueryInput queryInput) {
        HashMap<String, Double> targetMap = new HashMap<>();
        this.serviceLevelMap = new HashMap<>();
        for (Target t : queryInput.getTargets()) {
            Double impact = t.getImpact();
            if (impact == 0.0) {
                continue;
            }
            targetMap.put(t.getTargetId(), impact);
        }
        for (ServiceLevel sl : this.serviceLevels) {
            this.serviceLevelMap.put(sl.getId(), sl);
            if (targetMap.containsKey(sl.getId())) {
                sl.setImpact(targetMap.get(sl.getId()));
            }
        }
    }

    public ServiceLevel getSLById(String slId) {
        return this.serviceLevelMap.get(slId);
    }

}
