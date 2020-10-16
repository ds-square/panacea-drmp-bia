
package org.panacea.drmp.bia.domain.serviceLevel;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class Dependency {

    protected List<Dependency> dependencies;
    protected String dependencyType;
    protected String serviceLevelId;

    public void addDependency(Dependency d) {
        this.dependencies.add(d);
    }

}
