package org.panacea.drmp.bia.domain.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
public class DataNotificationResponse {
    @Schema(description = "Emulation environment name")
    private String environment;
    @Schema(description = "Data collection snapshot id")
    private String snapshotId;
    @Schema(description = "Data collection snapshot timestamp")
    private String snapshotTime;
}
