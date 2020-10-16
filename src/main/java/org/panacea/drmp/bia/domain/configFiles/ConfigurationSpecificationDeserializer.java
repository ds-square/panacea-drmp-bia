package org.panacea.drmp.bia.domain.configFiles;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.panacea.drmp.bia.domain.impact.ImpactLevel;
import org.panacea.drmp.bia.domain.query.output.QueryLikelihoodDeserializer;

import java.io.IOException;
import java.util.Iterator;

@Slf4j
public class ConfigurationSpecificationDeserializer extends StdDeserializer<ConfigurationSpecification> {

    private static final long serialVersionUID = 1L;

    public ConfigurationSpecificationDeserializer() {
        super(QueryLikelihoodDeserializer.class);
    }

    protected ConfigurationSpecificationDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ConfigurationSpecification deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectNode tn = jsonParser.readValueAsTree();
        String environment = tn.get("environment").asText();
        String fileType = tn.get("fileType").asText();
        String snapshotTime = tn.get("snapshotTime").asText();
        String snapshotId = tn.get("snapshotId").asText();
        ConfigurationSpecification configurationSpecification = new ConfigurationSpecification(environment, fileType, snapshotId, snapshotTime);
        for (JsonNode attackerNode : tn.get("attackers")) {
            Attacker a = new Attacker(
                    AttackerType.valueOf(attackerNode.get("attackerType").asText()),
                    attackerNode.get("attackComplexity").asDouble(),
                    attackerNode.get("attackVector").asDouble(),
                    attackerNode.get("privRequired").asDouble(),
                    attackerNode.get("codeMaturity").asDouble(),
                    attackerNode.get("reportConfidence").asDouble()
            );
            configurationSpecification.addAttacker(a);
        }
        JsonNode impactNode = tn.get("impactThresholds");
        for (Iterator<String> impactIterator = impactNode.fieldNames(); impactIterator.hasNext(); ) {
            String impactType = impactIterator.next();
            Double thresholdValue = impactNode.get(impactType).asDouble();
            configurationSpecification.addImpactLevel(ImpactLevel.valueOf(impactType), thresholdValue);
        }

        return configurationSpecification;
    }
}
