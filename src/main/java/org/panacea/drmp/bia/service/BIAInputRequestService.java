package org.panacea.drmp.bia.service;

import org.panacea.drmp.bia.domain.businessEntity.BusinessEntityInventory;
import org.panacea.drmp.bia.domain.businessNetworkMapping.BusinessNetworkMapping;
import org.panacea.drmp.bia.domain.configFiles.ConfigurationSpecification;
import org.panacea.drmp.bia.domain.notification.DataNotification;
import org.panacea.drmp.bia.domain.query.input.QueryInput;
import org.panacea.drmp.bia.domain.query.output.QueryLikelihood;
import org.panacea.drmp.bia.domain.serviceLevel.ServiceLevelInventory;

public interface BIAInputRequestService {

    BusinessEntityInventory getBusinessEntityInventoryFile(String snapshotId);

    ServiceLevelInventory getServiceLevelInventoryFile(String snapshotId);

    BusinessNetworkMapping getBusinessNetworkMappingFile(String snapshotId);

    QueryLikelihood getLikelihoodQueryFile(DataNotification notification);

    QueryInput getQueryInputFile(DataNotification notification);

    ConfigurationSpecification getConfigurationSpecificationFile(String snapshotId);
}
