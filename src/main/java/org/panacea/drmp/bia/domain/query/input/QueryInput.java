
package org.panacea.drmp.bia.domain.query.input;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@SuppressWarnings("unused")
public class QueryInput {

    private List<Source> sources;
    private List<Target> targets;
    private String type;

    public List<String> getTargetIds() {
        ArrayList<String> result = new ArrayList<>();
        for (Target t : this.targets) {
            result.add(t.getTargetId());
        }
        return result;
    }

    public List<String> getSourceIds() {
        ArrayList<String> result = new ArrayList<>();
        for (Source s : this.sources) {
            result.add(s.getSourceId());
        }
        return result;
    }

}
