package org.panacea.drmp.bia.service;

import org.panacea.drmp.bia.domain.notification.DataNotification;
import org.panacea.drmp.bia.domain.serviceLevel.ServiceLevelImpact;

import java.util.List;
import java.util.Map;

public interface BIAOutputRequestService {
    void postQueryOutputFile(DataNotification notification, List<ServiceLevelImpact> list);
    // Compatibility with higher steps of the chain
    void postQueryOutputCompatibleFile(DataNotification notification, Map<String,Object> map);
}
