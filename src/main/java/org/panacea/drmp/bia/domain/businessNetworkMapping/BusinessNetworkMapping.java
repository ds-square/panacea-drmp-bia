
package org.panacea.drmp.bia.domain.businessNetworkMapping;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
@SuppressWarnings("unused")
public class BusinessNetworkMapping {

    private String environment;
    private String fileType;
    private List<Mapping> mappings;
    private String snapshotId;
    private String snapshotTime;
    private HashMap<String, String> deviceServiceLevelIdMap;


    public void computeMappingHashmap() {
        this.deviceServiceLevelIdMap = new HashMap<>();
        for (Mapping m : this.mappings) {
            this.deviceServiceLevelIdMap.put(m.getDeviceId(), m.getServiceLevelId());
        }
    }

}
