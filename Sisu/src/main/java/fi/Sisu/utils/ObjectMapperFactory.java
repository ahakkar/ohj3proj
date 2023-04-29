package fi.Sisu.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import fi.Sisu.model.SisuNode;

import java.io.IOException;
import java.util.Optional;

/**
 * Factory class for creating Jackson ObjectMappers which use a custom
 * serializer to tackle the problem of serializing Optional<SisuNode> objects.
 * 
 * @author Antti Hakkarainen
 */
public class ObjectMapperFactory {

    /**
     * Custom serializer for Optional<SisuNode> objects. If the Optional<SisuNode>
     * is present, the SisuNode object is written to the json file. If the
     * Optional<SisuNode> is empty, null is written to the json file.
     */
    public static class OptionalSisuNodeSerializer extends StdSerializer<Optional<SisuNode>> {

        public OptionalSisuNodeSerializer() {
            super(Optional.class, false);
        }
    
        @Override
        public void serialize(
            Optional<SisuNode> optionalSisuNode,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
        ) throws IOException {
            // If the Optional<SisuNode> is present, write the SisuNode object
            if (optionalSisuNode.isPresent()) {
                jsonGenerator.writeObject(optionalSisuNode.get());
            }
            else {
                jsonGenerator.writeNull();
            }
        }
    }
    

    /**
     * Creates a new ObjectMapper which uses a custom serializer for
     * Optional<SisuNode> objects.
     * 
     * @return ObjectMapper with custom serializer for Optional<SisuNode> objects
     */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new OptionalSisuNodeSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
