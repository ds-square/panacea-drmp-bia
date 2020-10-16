package org.panacea.drmp.bia.domain.cycles;


import org.panacea.drmp.bia.domain.serviceLevel.CycleDependencyNode;
import org.panacea.drmp.bia.domain.serviceLevel.Dependency;

import java.util.ArrayList;
import java.util.List;

public class DGCycle {
    public String cycleId;
    public List<String> elements;
    public CycleDependencyNode dependencies;

    public DGCycle(String cycleId) {
        this.cycleId = cycleId;
        this.elements = new ArrayList<>();
        this.dependencies = new CycleDependencyNode(cycleId);
    }

    public List<Dependency> getDependencies() {
        return this.dependencies.getDependencies();
    }

    public void setDependencies(List<Dependency> deps) {
        this.dependencies.setDependencies(new ArrayList<>());
        for (Dependency d : deps) {
            if (d.getDependencyType().equalsIgnoreCase("AllDependencyNode") ||
                    d.getDependencyType().equalsIgnoreCase("AnyDependencyNode") ||
                    d.getDependencyType().equalsIgnoreCase("CycleDependencyNode")
            ) {
                for (Dependency dd : d.getDependencies()) {
                    if (this.canBeAddedToDeps(dd)) this.dependencies.addDependency(dd);
                }
            } else {
                if (this.canBeAddedToDeps(d)) this.dependencies.addDependency(d);
            }
        }
    }

    private boolean canBeAddedToDeps(Dependency d) {
        if (!(d.getDependencyType().equals("ServiceLevelDependencyNode"))) return false;
        boolean alreadyDep = this.dependencies.getDependencies().contains(d);
        boolean isAnElement = this.elements.contains(d.getServiceLevelId());
        return (!alreadyDep) && (!isAnElement);
    }
}
