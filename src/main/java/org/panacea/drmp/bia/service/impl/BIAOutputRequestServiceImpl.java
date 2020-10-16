package org.panacea.drmp.bia.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.panacea.drmp.bia.domain.notification.DataNotification;
import org.panacea.drmp.bia.domain.serviceLevel.ServiceLevelImpact;
import org.panacea.drmp.bia.service.BIAOutputRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BIAOutputRequestServiceImpl implements BIAOutputRequestService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${queryOutput.endpoint}")
    private String queryOutputURL;


    @Override
    public void postQueryOutputFile(DataNotification notification, List<ServiceLevelImpact> impactList) {
        HttpEntity<List<ServiceLevelImpact>> requestEntity = new HttpEntity<>(impactList);

        String endPointUrl = queryOutputURL + '/' + notification.getSnapshotId() + "/"+ notification.getQueryId() + "/impact";
//        log.info("[BIA] POST query with id " + notification.getQueryId() + " to " + endPointUrl);
        log.info("[BIA] POST impacts values for query ID \"" + notification.getQueryId() + "\" to http://172.16.100.131:8108/persistence/query/output");
        ResponseEntity<String> response = null;
        RestTemplate restTemplate = new RestTemplate();
        try {
            response = restTemplate
                    .postForEntity(endPointUrl, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            System.out.println("Response from storage service: " + response);
            byte[] bytes = e.getResponseBodyAsByteArray();

            //Convert byte[] to String
            String s = new String(bytes);

            log.error(s);
            e.printStackTrace();
        }

    }


    // Compatibility with higher steps of the chain
    @Override
    public void postQueryOutputCompatibleFile(DataNotification notification, Map<String,Object> impactMap) {
        HttpEntity< Map<String,Object>> requestEntity = new HttpEntity<>(impactMap);

        String endPointUrl = queryOutputURL + '/' + notification.getSnapshotId() + "/"+ notification.getQueryId() + "/targetsImpact";
//        log.info("[BIA] POST query with id " + notification.getQueryId() + " to " + endPointUrl);
        log.info("[BIA] POST compatible impacts values for query ID \"" + notification.getQueryId() + "\" to http://172.16.100.131:8108/persistence/query/output");
        ResponseEntity<String> response = null;
        RestTemplate restTemplate = new RestTemplate();
        try {
            response = restTemplate
                    .postForEntity(endPointUrl, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            System.out.println("Response from storage service: " + response);
            byte[] bytes = e.getResponseBodyAsByteArray();

            //Convert byte[] to String
            String s = new String(bytes);

            log.error(s);
            e.printStackTrace();
        }

    }
}
