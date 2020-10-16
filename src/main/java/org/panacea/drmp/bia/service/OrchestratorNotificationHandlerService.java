package org.panacea.drmp.bia.service;

import org.panacea.drmp.bia.domain.notification.DataNotification;
import org.panacea.drmp.bia.domain.notification.DataNotificationResponse;

public interface OrchestratorNotificationHandlerService {

    DataNotificationResponse perform(DataNotification notification);

}
