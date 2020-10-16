
package org.panacea.drmp.bia.domain.businessEntity;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class BusinessEntityInventory {

    private List<BusinessEntity> businessEntities;
    private String environment;
    private String fileType;
    private String snapshotId;
    private String snapshotTime;

}
