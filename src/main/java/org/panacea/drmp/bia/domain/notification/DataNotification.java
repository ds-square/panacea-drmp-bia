package org.panacea.drmp.bia.domain.notification;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class DataNotification {
    @Schema(description = "Emulation environment name")
    private String environment;
    @Schema(description = "Data collection snapshot id")
    private String snapshotId;
    @Schema(description = "Query identificator")
    private String queryId;
    @Schema(description = "Data collection snapshot timestamp")
    private String snapshotTime;
}
