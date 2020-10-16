
package org.panacea.drmp.bia.domain.businessEntity;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class BusinessEntity {

    private String id;
    private String name;
    private List<String> serviceLevelsList;
    private String type;

}
