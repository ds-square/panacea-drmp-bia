package org.panacea.drmp.bia.domain.query.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.HashBasedTable;
import lombok.Data;
import org.panacea.drmp.bia.domain.configFiles.AttackerType;

@Data
@SuppressWarnings("unused")
@JsonDeserialize(using = QueryLikelihoodDeserializer.class)
public class QueryLikelihood {
    private HashBasedTable<String, AttackerType, Double> target2likelihood;

    public QueryLikelihood() {
        this.target2likelihood = HashBasedTable.create();
    }

    public void addLilelihood(String s, AttackerType a, Double likelihood) {
        this.target2likelihood.put(s, a, likelihood);
    }

//    public Double getLikelihood(String s, AttackerType a){
//        Double result = this.target2likelihood.get(s, a);
//        return result;
//    }

}
