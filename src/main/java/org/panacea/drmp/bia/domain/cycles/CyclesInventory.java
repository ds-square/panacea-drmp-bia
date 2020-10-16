package org.panacea.drmp.bia.domain.cycles;

import org.panacea.drmp.bia.domain.serviceLevel.Dependency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CyclesInventory {
    private Map<String, DGCycle> id2cycle;
    private Map<String, String> sl2cycleId;
    private int lastCycleId;

    public CyclesInventory() {
        this.id2cycle = new HashMap<>();
        this.sl2cycleId = new HashMap<>();
        this.lastCycleId = 0;
    }

    public void addCycle(List<String> elements, List<Dependency> deps) {
        String newCycle = this.createCycle();
        for (String slId : elements) {
            String existingCycle = this.sl2cycleId.get(slId);
            if (existingCycle == null) {
                this.addToCycle(slId, newCycle);
            } else if (!existingCycle.equals(newCycle)) {
                deps.addAll(this.moveCycle(existingCycle, newCycle));
            }
        }
        this.id2cycle.get(newCycle).setDependencies(deps);
    }

    public Dependency getDependency(String slId) {
        String cycleId = this.sl2cycleId.get(slId);
        if (cycleId == null) {
            return null;
        } else {
            return this.id2cycle.get(cycleId).dependencies;
        }
    }

    public DGCycle getCycleFromSlId(String slId) {
        String cycleId = this.sl2cycleId.get(slId);
        if (cycleId == null) return null;
        return this.id2cycle.get(cycleId);
    }

    public Map<String, DGCycle> getCycles() {
        return this.id2cycle;
    }

    public boolean isCycle(String cycleId) {
        return this.id2cycle.containsKey(cycleId);
    }

    private String createCycle() {
        this.lastCycleId++;
        String newCycleId = Integer.toString(this.lastCycleId);
        this.id2cycle.put(newCycleId, new DGCycle(newCycleId));
        return newCycleId;
    }

    private void addToCycle(String slId, String cycleId) {
        this.id2cycle.get(cycleId).elements.add(slId);
        this.sl2cycleId.put(slId, cycleId);
    }

    private List<Dependency> moveCycle(String oldCycleId, String newCycleId) {
        for (String slId : this.id2cycle.get(oldCycleId).elements) {
            this.addToCycle(slId, newCycleId);
        }
        DGCycle oldCycle = this.id2cycle.get(oldCycleId);
        this.id2cycle.remove(oldCycleId);
        return oldCycle.getDependencies();
    }
}
