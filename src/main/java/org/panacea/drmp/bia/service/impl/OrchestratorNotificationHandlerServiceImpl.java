package org.panacea.drmp.bia.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.panacea.drmp.bia.BIACalculator;
import org.panacea.drmp.bia.domain.exception.BIAException;
import org.panacea.drmp.bia.domain.notification.DataNotification;
import org.panacea.drmp.bia.domain.notification.DataNotificationResponse;
import org.panacea.drmp.bia.service.OrchestratorNotificationHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Service
public class OrchestratorNotificationHandlerServiceImpl implements OrchestratorNotificationHandlerService {
    public static final String INVALID_NOTIFICATION_ERR_MSG = "Invalid Data Notification Body.";

    @Autowired
    BIACalculator biaCalculator;

    @Override
    @ResponseBody
    public DataNotificationResponse perform(DataNotification notification) throws BIAException {
//        log.info("[BIA] Received Data Notification from Orchestrator: {}", notification);
        log.info("[BIA] Received notification from Risk Estimation Engine (REE)");
        try {
            if (notification.getEnvironment() == null) {
                throw new BIAException("No environment defined for notification.");
            }
            biaCalculator.computeImpact(notification);

            return new DataNotificationResponse(notification.getEnvironment(), notification.getSnapshotId(), notification.getSnapshotTime());
        } catch (BIAException e) {
            log.error("BIAException occurred: ", e);
            throw new BIAException(INVALID_NOTIFICATION_ERR_MSG, e);
        }
    }
}

