package org.panacea.drmp.bia;

import com.google.common.collect.HashBasedTable;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.panacea.drmp.bia.domain.businessEntity.BusinessEntityInventory;
import org.panacea.drmp.bia.domain.businessNetworkMapping.BusinessNetworkMapping;
import org.panacea.drmp.bia.domain.businessNetworkMapping.Mapping;
import org.panacea.drmp.bia.domain.configFiles.AttackerType;
import org.panacea.drmp.bia.domain.configFiles.ConfigurationSpecification;
import org.panacea.drmp.bia.domain.cycles.CyclesInventory;
import org.panacea.drmp.bia.domain.cycles.DGCycle;
import org.panacea.drmp.bia.domain.exception.BIAException;
import org.panacea.drmp.bia.domain.impact.ImpactLevel;
import org.panacea.drmp.bia.domain.impact.ImpactToken;
import org.panacea.drmp.bia.domain.notification.DataNotification;
import org.panacea.drmp.bia.domain.query.input.QueryInput;
import org.panacea.drmp.bia.domain.serviceLevel.Dependency;
import org.panacea.drmp.bia.domain.serviceLevel.ServiceLevel;
import org.panacea.drmp.bia.domain.serviceLevel.ServiceLevelImpact;
import org.panacea.drmp.bia.domain.serviceLevel.ServiceLevelInventory;
import org.panacea.drmp.bia.service.BIAInputRequestService;
import org.panacea.drmp.bia.service.BIAOutputRequestService;
import org.panacea.drmp.bia.utils.DoubleKeyMap;
import org.panacea.drmp.bia.utils.TokensBag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class BIACalculator {

    @Autowired
    BIAInputRequestService biaInputRequestService;
    @Autowired
    BIAOutputRequestService biaOutputRequestService;


    private BusinessEntityInventory businessEntityInventory;
    private ServiceLevelInventory serviceLevelInventory;
    private BusinessNetworkMapping businessNetworkMapping;
    private HashBasedTable<String, AttackerType, Double> likelihood2target;
    private ConfigurationSpecification configurationSpecification;
    private QueryInput queryInput;
    private Map<String, ServiceLevelImpact> sl2sli;
    private CyclesInventory cyclesInventory;
    private List<String> targetIdList;

    @Synchronized
    public void computeImpact(DataNotification notification) {
        try {
            this.getInput(notification);
        } catch (BIAException e) {
            log.error(e.getMessage());
        }
//        this.DGCycles = new ArrayList<>();
        this.sl2sli = new HashMap<>();
        this.cyclesInventory = new CyclesInventory();


        List<ServiceLevelImpact> slis = new ArrayList<>();

        this.findCycles();
        this.initSLI();

        //TODO add code to manage different analysis

        if (queryInput.getType().equals("business")) {
            slis = this.businessAnalysis();
        } else if (queryInput.getType().equals("asset")) {
            slis = this.assetsAnalysis();
        } else {
            log.error("Query type " + queryInput.getType() + " not supported");
            return;
        }

        this.manageCycles(slis); // assigns the same properties to each component of each cycle
        this.aggregateImpacts(slis); // Converts tokens to low/medium/high

        biaOutputRequestService.postQueryOutputFile(notification, slis);

        // Stuff below is needed for compatibility with other elements of the chain
        if (queryInput.getType().equals("business")) {
            // This is the return structure
            Map<String,Object> compatibleSlis = new HashMap<>();

            // We need a bunch of extra fields as a header
            compatibleSlis.put("environment ",notification.getEnvironment());
            compatibleSlis.put("fileType","targetsImpact");
            compatibleSlis.put("snapshotId",notification.getSnapshotId());
            compatibleSlis.put("timestamp",notification.getSnapshotTime());

            // Here it gets interesting
            // So, we need a list of key - value pairs
            Map<String,Double> uuidToImpact = new HashMap<>();

            // Scan all SLIs
            for (ServiceLevelImpact sli:slis){
                Map<AttackerType,String> targetMap = sli.getTarget();
                // Given a SLI target, grab NAIVE, ADVANCED and PROFESSIONAL
                for (Map.Entry<AttackerType,String> entry:targetMap.entrySet()) {
                    // Check if the uuid has already been logged
                    if (uuidToImpact.containsKey(entry.getValue())) {
                        // Update with the the maximum impact value
                        if (sli.getImpact() > uuidToImpact.get(entry.getValue())){
                            uuidToImpact.put(entry.getValue(),sli.getImpact());
                        }
                    } else {
                        uuidToImpact.put(entry.getValue(),sli.getImpact());
                    }
                }
            }

            // Great, but we need key: the actual key, value: the actual value
            List<Map<String,Object>> finalSlisList = new ArrayList<>();

            for (Map.Entry<String,Double> entry:uuidToImpact.entrySet()){
                Map<String,Object> entryFormatted = new HashMap<>();
                entryFormatted.put("uuid",entry.getKey());
                entryFormatted.put("impact",entry.getValue());
                finalSlisList.add(entryFormatted);
            }

            // Finally put everything in the return structure
            compatibleSlis.put("targetsnodes",finalSlisList);

            // And send it through the wire
            biaOutputRequestService.postQueryOutputCompatibleFile(notification, compatibleSlis);
        }
    }

    private void getInput(DataNotification notification) {
        this.businessEntityInventory = biaInputRequestService.getBusinessEntityInventoryFile(notification.getSnapshotId());
        log.info("[BIA] GET BusinessEntityInventory from http://172.16.100.131:8107/business-layer/businessEntityInventory");
        this.serviceLevelInventory = biaInputRequestService.getServiceLevelInventoryFile(notification.getSnapshotId());
        log.info("[BIA] GET ServiceLevelInventory from http://172.16.100.131:8107/business-layer/serviceLevelInventory");
        this.businessNetworkMapping = biaInputRequestService.getBusinessNetworkMappingFile(notification.getSnapshotId());
        log.info("[BIA] GET BusinessNetworkMapping from http://172.16.100.131:8107/business-layer/businessNetworkMapping");
        this.businessNetworkMapping.computeMappingHashmap();
        this.likelihood2target = biaInputRequestService.getLikelihoodQueryFile(notification).getTarget2likelihood();
        log.info("[BIA] GET Likelihood values from http://172.16.100.131:8108/persistence/output");
        this.queryInput = biaInputRequestService.getQueryInputFile(notification);
        log.info("[BIA] GET Query with ID \""+ notification.getQueryId()+"\" from http://172.16.100.131:8108/persistence/query/input");
        for (String targetId : queryInput.getTargetIds()) {
            this.targetIdList = new ArrayList<>(this.queryInput.getTargetIds());
        }
        this.serviceLevelInventory.computeMapImpact(this.queryInput);
        this.configurationSpecification = biaInputRequestService.getConfigurationSpecificationFile(notification.getSnapshotId());
        log.info("[BIA] GET attacker parameters from http://172.16.100.131:8107/business-layer/config/");

    }

    private List<ServiceLevelImpact> businessAnalysis() {
        List<ServiceLevelImpact> result = new ArrayList<>();
        Dependency dep;
        ServiceLevelImpact sli;
        Map<AttackerType, String> depTrg;
        if (queryInput.getType().equals("asset")) {
            this.targetIdList = this.getAssetsFromDevices(this.targetIdList);
        }
        for (String slId : this.targetIdList) {
            dep = this.getNextDep(slId);
            sli = this.sl2sli.get(slId);
//            if (dep.getDependencyType().equals("CycleDependencyNode")) {
//                log.error("ECCO");
//            }
            depTrg = this.computeSlTarget(dep);
            try {
                sli.setTarget(this.maxTarget(sli.getTarget(), depTrg));
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            result.add(sli);
        }
        return result;
    }

    private List<ServiceLevelImpact> assetsAnalysis() {
        this.businessAnalysis(); // updates sl2sli
        Set<ServiceLevelImpact> result = new HashSet<>();
        Dependency dep;
        ServiceLevelImpact sli;
        // for each sl with an user defined impact (sli.impact),
        // navigate down the DG and propagate its token
        for (ServiceLevel sl : this.serviceLevelInventory.getServiceLevels()) {
            String slId = sl.getId();
            sli = this.sl2sli.get(slId);
            if (sli.getImpact() == null) continue;
            dep = this.getNextDep(slId);
            result.addAll(this.propagateSlTokens(dep, sli.getTokens()));
        }
        return new ArrayList<>(result);
    }


    private void findCycles() {
        // simply calls the recursive findCyclesRec on each sl
        for (ServiceLevel sl : this.serviceLevelInventory.getServiceLevels()) {
            ArrayList<String> route = new ArrayList<>();
            route.add(sl.getId());
            this.findCyclesRec(route, sl.getDependency());
        }
    }

    private void findCyclesRec(ArrayList<String> route, Dependency dep) {
        route = (ArrayList<String>) route.clone();
        if (dep == null) {
            return;
        }
        if (dep.getDependencyType().equalsIgnoreCase("ServiceLevelDependencyNode")) {
            String slId = dep.getServiceLevelId();
            int cycleBeginning = route.indexOf(slId);
            if (cycleBeginning != -1) {
                route.subList(0, cycleBeginning).clear();
                this.cyclesInventory.addCycle(route, this.findCycleDependencies(route));
            } else {
                route.add(slId);
                this.findCyclesRec(route, this.serviceLevelInventory.getSLById(slId).getDependency());
            }
        } else {
            for (Dependency d : dep.getDependencies()) {
                this.findCyclesRec(route, d);
            }
        }
    }

    private List<Dependency> findCycleDependencies(List<String> elements) {
        List<Dependency> result = new ArrayList<>();
        Dependency d;
        for (String slId : elements) {
            d = this.serviceLevelInventory.getSLById(slId).getDependency();
            if (d != null) result.add(d);
        }
        return result;
    }

    private Dependency getNextDep(String slId) {
        Dependency result = this.cyclesInventory.getDependency(slId);
        if (result == null) {
            ServiceLevel currentSL = this.serviceLevelInventory.getSLById(slId);
            if (currentSL != null) {
                result = currentSL.getDependency();
            }
        }
        return result;
    }

    private void manageCycles(List<ServiceLevelImpact> slis) {
        // assigns the same properties to each component of each cycle
        DGCycle cycle;
        ServiceLevelImpact cycleSli;
        List<ServiceLevelImpact> toRemove = new ArrayList<>();
        for (ServiceLevelImpact sli : slis) {
            if (this.cyclesInventory.isCycle(sli.getServiceLevelId())) {
                toRemove.add(sli);
                continue;
            }
            cycle = this.cyclesInventory.getCycleFromSlId(sli.getServiceLevelId());
            if (cycle != null) {
                cycleSli = this.sl2sli.get(cycle.cycleId);
                sli.setTarget(cycleSli.getTarget());
                sli.setTokens(cycleSli.getTokens());
            }
        }
        slis.removeAll(toRemove);
    }

    private void initSLI() {
//        Map<String,Double> sl2userImpact = StorageGW.getImpacts();
        DoubleKeyMap<String> ep2target = this.chooseEpTargets(); // by target here I mean the last node of an AP

        Double userImpact;
        ServiceLevelImpact sli;
        Map<AttackerType, String> target;
        String slId;
        for (ServiceLevel sl : this.serviceLevelInventory.getServiceLevels()) {
            slId = sl.getId();
            userImpact = sl.getImpact();
            target = ep2target.get(slId);

            sli = new ServiceLevelImpact(slId, userImpact, target);
            if (userImpact != null) sli.getTokens().add(new ImpactToken(userImpact, slId));

            this.sl2sli.put(slId, sli);
        }
        // each cycle gets its own SLI
        for (Map.Entry<String, DGCycle> entry : this.cyclesInventory.getCycles().entrySet()) {
            sli = new ServiceLevelImpact(entry.getKey(), 0.0, null);
            for (String sl : entry.getValue().elements) {
                sli.getTokens().addAll(this.sl2sli.get(sl).getTokens());
                sli.setTarget(this.maxTarget(sli.getTarget(), ep2target.get(sl)));
            }
            this.sl2sli.put(entry.getKey(), sli);
        }
    }

    private DoubleKeyMap<String> chooseEpTargets() {
        DoubleKeyMap<String> ep2trg = new DoubleKeyMap<>();
        String epId, trgId, curTrg;
        for (Mapping m : this.businessNetworkMapping.getMappings()) {
            epId = m.getServiceLevelId();
            trgId = m.getDeviceId();
            // If device isn't target of any ap I skip it
            if (!this.likelihood2target.containsRow(trgId)) continue;
            for (AttackerType a : AttackerType.values()) {
                curTrg = ep2trg.get(epId, a);
                if (curTrg == null || (this.likelihood2target.get(trgId, a) > this.likelihood2target.get(curTrg, a))) {
                    ep2trg.put(epId, a, trgId);
                }
            }
        }
        return ep2trg;
    }

    private Map<AttackerType, String> computeSlTarget(Dependency dep) {
        Map<AttackerType, String> result = null;
        if (dep == null) {
            return result;
        }
        if (dep.getDependencyType().equals("CycleDependencyNode")) {
            ServiceLevelImpact cycle_sli = this.sl2sli.get(dep.getServiceLevelId());
            result = cycle_sli.getTarget();
            for (Dependency d : dep.getDependencies()) {
                Map<AttackerType, String> depTrg = this.computeSlTarget(d);
                result = this.maxTarget(depTrg, result);
            }
            cycle_sli.setTarget(result);
        } else if (dep.getDependencyType().equals("AllDependencyNode")) {
            for (Dependency d : dep.getDependencies()) {
                Map<AttackerType, String> depTrg = this.computeSlTarget(d);
                result = this.maxTarget(depTrg, result);
            }
        } else if (dep.getDependencyType().equals("AnyDependencyNode")) {
            List<Dependency> depList = dep.getDependencies();
            int anySize = depList.size();
            for (int i = 0; i < anySize; i++) {
                Dependency d = depList.get(i);
                Map<AttackerType, String> depTrg = this.computeSlTarget(d);
                result = this.minTarget(depTrg, result);
            }
        } else if (dep.getDependencyType().equals("ServiceLevelDependencyNode")) {
            String slId = dep.getServiceLevelId();
            Dependency nextDep = this.getNextDep(slId);
            ServiceLevelImpact sli = this.sl2sli.get(slId);
            if (nextDep != null) {
                // I take the max target between the current one and the dependency's
                Map<AttackerType, String> depTarget = this.computeSlTarget(nextDep);
                result = this.maxTarget(sli.getTarget(), depTarget);
                sli.setTarget(result);
            } else {
                result = sli.getTarget(); // It's a dead end
            }
        }
        return result;
    }

    private Set<ServiceLevelImpact> propagateSlTokens(Dependency dep, TokensBag tokens) {
        Set<ServiceLevelImpact> result = new HashSet<>();
        if (dep == null) {
            return result;
        }
        if (dep.getDependencyType().equals("CycleDependencyNode")) {
            ServiceLevelImpact sli = this.sl2sli.get(dep.getServiceLevelId());
            sli.getTokens().addAll(tokens);
            for (Dependency d : dep.getDependencies()) {
                result.addAll(this.propagateSlTokens(d, sli.getTokens().clone()));
            }
        } else if (dep.getDependencyType().equals("AllDependencyNode")) {
            for (Dependency d : dep.getDependencies()) {
                result.addAll(this.propagateSlTokens(d, tokens.clone()));
            }
        } else if (dep.getDependencyType().equals("AnyDependencyNode")) {
            List<Dependency> depList = dep.getDependencies();
            int anySize = depList.size();
            for (int i = 0; i < anySize; i++) {
                Dependency d = depList.get(i);
                TokensBag splitTokens = tokens.split(i + 1, anySize);
                result.addAll(this.propagateSlTokens(d, splitTokens));
            }
        } else if (dep.getDependencyType().equals("ServiceLevelDependencyNode")) {
            String slId = dep.getServiceLevelId();
            Dependency nextDep = this.getNextDep(slId);
            ServiceLevelImpact sli = this.sl2sli.get(slId);
            sli.getTokens().addAll(tokens);
            if (this.targetIdList.contains(slId)) result.add(sli);
            result.addAll(this.propagateSlTokens(nextDep, tokens.clone()));

        }
        return result;
    }

    private void aggregateImpacts(List<ServiceLevelImpact> slis) {
        Map<ImpactLevel, Double> impact2threshold = this.configurationSpecification.getImpactThresholdTable();
        Double totalImpact;
        for (ServiceLevelImpact sli : slis) {
            totalImpact = 0.0;
            for (ImpactToken t : sli.getTokens()) {
                if (t.fragments == 1) totalImpact += t.value;
            }
            sli.setImpact(totalImpact);
            for (ImpactLevel i : ImpactLevel.values()) {
                if (totalImpact < impact2threshold.get(i)) {
                    break;
                }
                sli.setImpactLevel(i);
            }
        }
    }

//   ORIGINAL METHOD
//    private Map<AttackerType,String> maxTarget(Map<AttackerType,String> x, Map<AttackerType,String> y) {
//        if (x == null) return y;
//        if (y == null) return x;
//        Map<AttackerType,String> at = new HashMap<>();
//
//        for (AttackerType a : AttackerType.values()) {
//            String xTrg = x.get(a);
//            String yTrg = y.get(a);
//            if (this.trg2lik.get(xTrg, a, 0.0) > this.trg2lik.get(yTrg, a, 0.0)) at.put(a, xTrg);
//            else at.put(a, yTrg);
//        }
//
//        return at;
//    }

    private Map<AttackerType, String> maxTarget(Map<AttackerType, String> x, Map<AttackerType, String> y) {
        if (x == null) return y;
        if (y == null) return x;
        Map<AttackerType, String> at = new HashMap<>();
        for (AttackerType a : AttackerType.values()) {
            String xTrg = x.get(a);
            String yTrg = y.get(a);
            Double xLikelihood = this.likelihood2target.get(xTrg, a);
            Double yLikelihood = this.likelihood2target.get(yTrg, a);
            if (xLikelihood == null && yLikelihood == null) {
                at.put(a, null);
            } else if (xLikelihood == null) {
                at.put(a, yTrg);
            } else if (yLikelihood == null) {
                at.put(a, xTrg);
            } else {
                if (xLikelihood > yLikelihood) {
                    at.put(a, xTrg);
                } else {
                    at.put(a, yTrg);
                }
            }
        }
        return at;
    }

//    ORIGINAL CODE
//    private Map<AttackerType,String> minTarget(Map<AttackerType,String> x, Map<AttackerType,String> y) {
//        if (x == null) return y;
//        if (y == null) return x;
//        Map<AttackerType,String> at = new HashMap<>();
//        for (AttackerType a: AttackerType.values()) {
//            String xTrg = x.get(a);
//            String yTrg = y.get(a);
//            Double xLik = this.trg2lik.get(xTrg, a);
//            Double yLik = this.trg2lik.get(yTrg, a);
//            if (xLik==null) at.put(a, yTrg);
//            else if (yLik==null) at.put(a, xTrg);
//            else if ( xLik < yLik) at.put(a, xTrg);
//            else at.put(a, yTrg);
//        }
//        return at;
//    }

    private Map<AttackerType, String> minTarget(Map<AttackerType, String> x, Map<AttackerType, String> y) {
        if (x == null) return y;
        if (y == null) return x;
        Map<AttackerType, String> at = new HashMap<>();
        for (AttackerType a : AttackerType.values()) {
            String xTrg = x.get(a);
            String yTrg = y.get(a);
            Double xLik = this.likelihood2target.get(xTrg, a);
            Double yLik = this.likelihood2target.get(yTrg, a);
            if (xLik == null && yLik == null) at.put(a, null);
            else if (xLik == null) at.put(a, yTrg);
            else if (yLik == null) at.put(a, xTrg);
            else if (xLik < yLik) at.put(a, xTrg);
            else at.put(a, yTrg);
        }
        return at;
    }

    private List<String> getAssetsFromDevices(Collection<String> devices) {
        List<String> assets = new ArrayList<>();

        List<String> devicePrivilegeList = new ArrayList<>();

        for (String d : devices) {
            if (d.contains("@")) {
                devicePrivilegeList.add(d);
            } else {
                devicePrivilegeList.add("ROOT@" + d);
                devicePrivilegeList.add("USER@" + d);
                devicePrivilegeList.add("NONE@" + d);
            }
        }
        for (String d : devicePrivilegeList) {
            String sl = this.businessNetworkMapping.getDeviceServiceLevelIdMap().get(d);
            if (sl != null) assets.add(sl);
        }
        return assets;
    }

}
