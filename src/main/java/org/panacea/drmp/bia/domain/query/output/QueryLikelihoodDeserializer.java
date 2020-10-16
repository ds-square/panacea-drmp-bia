package org.panacea.drmp.bia.domain.query.output;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.panacea.drmp.bia.domain.configFiles.AttackerType;

import java.io.IOException;
import java.util.Iterator;

@Slf4j
public class QueryLikelihoodDeserializer extends StdDeserializer<QueryLikelihood> {

    private static final long serialVersionUID = 1L;

    public QueryLikelihoodDeserializer() {
        super(QueryLikelihoodDeserializer.class);
    }

    protected QueryLikelihoodDeserializer(Class<?> vc) {
        super(vc);
    }


    @Override
    public QueryLikelihood deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        QueryLikelihood queryLikelihood = new QueryLikelihood();
        ObjectNode tn = jsonParser.readValueAsTree();
        for (Iterator<String> targetIterator = tn.fieldNames(); targetIterator.hasNext(); ) {
            String target = targetIterator.next();
            JsonNode vector = tn.get(target);
            for (Iterator<String> attackerIterator = vector.fieldNames(); attackerIterator.hasNext(); ) {
                String attackerTypeString = attackerIterator.next();
                AttackerType a = AttackerType.valueOf(attackerTypeString);
                Double likelihood = vector.get(attackerTypeString).asDouble();
                queryLikelihood.addLilelihood(target, a, likelihood);
            }
        }
        return queryLikelihood;
    }
}
