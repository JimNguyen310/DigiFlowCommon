package fec.digiflow.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import fec.digiflow.common.dto.LazyLoadEvent;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deserializes the 'filters' property of a LazyLoadEventDTO. The value for each
 * filter key can be either a single FilterMetadata object or an array of FilterMetadata objects.
 * This deserializer handles both cases, always producing a List<FilterMetadata> as the value.
 */
public class FiltersDeserializer extends JsonDeserializer<Map<String, List<LazyLoadEvent.FilterMetadata>>> {

    @Override
    public Map<String, List<LazyLoadEvent.FilterMetadata>> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (!p.isExpectedStartObjectToken()) {
            ctxt.reportWrongTokenException(this.getClass(), JsonToken.START_OBJECT, "Expected a JSON object for filters");
            return Collections.emptyMap();
        }

        Map<String, List<LazyLoadEvent.FilterMetadata>> result = new HashMap<>();
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        CollectionType filterListType = mapper.getTypeFactory().constructCollectionType(List.class, LazyLoadEvent.FilterMetadata.class);

        while (p.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = p.getCurrentName();
            p.nextToken();

            List<LazyLoadEvent.FilterMetadata> listValue;
            JsonToken currentToken = p.currentToken();

            if (currentToken == JsonToken.START_ARRAY) {
                listValue = mapper.readValue(p, filterListType);
            } else if (currentToken == JsonToken.START_OBJECT) {
                LazyLoadEvent.FilterMetadata singleMeta = mapper.readValue(p, LazyLoadEvent.FilterMetadata.class);
                listValue = Collections.singletonList(singleMeta);
            } else {
                // Skip unknown/unhandled tokens (like null, string, etc.) for this field
                p.skipChildren();
                continue;
            }
            result.put(fieldName, listValue);
        }
        return result;
    }
}
