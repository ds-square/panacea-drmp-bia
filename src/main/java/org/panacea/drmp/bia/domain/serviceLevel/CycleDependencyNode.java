package org.panacea.drmp.bia.domain.serviceLevel;

import java.util.ArrayList;

public class CycleDependencyNode extends Dependency {
    public String cycleId;

    public CycleDependencyNode(String cycleId) {
        super();
        this.cycleId = cycleId;
        this.dependencies = new ArrayList<>();
        this.setDependencyType("CycleDependencyNode");
    }
}
