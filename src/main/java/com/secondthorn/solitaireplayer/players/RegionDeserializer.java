package com.secondthorn.solitaireplayer.players;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.sikuli.script.Region;

import java.io.IOException;

/**
 * A Jackson JSON deserializer for Sikuli Region objects.
 * Jackson's default JSON deserialization behavior has problems with Region objects, and using a mixin to
 * have Jackson ignore properties we don't care about is also hard to do because there's a lot of other
 * classes involved too.  So far I think this is the simplest way to create Region objects based on JSON data.
 */
public class RegionDeserializer extends StdDeserializer<Region> {
    private RegionDeserializer(Class<?> vc) {
        super(vc);
    }

    static Regions createRegions(String regionsJsonFilename) throws PlayException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule().addDeserializer(Region.class, new RegionDeserializer(null));
        mapper.registerModule(module);
        try {
            return mapper.readValue(ClassLoader.getSystemResource(regionsJsonFilename), Regions.class);
        } catch (IOException ex) {
            throw new PlayException("Unable to load " + regionsJsonFilename, ex);
        }
    }

    @Override
    public Region deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.readValueAsTree();
        int x = node.get("x").asInt();
        int y = node.get("y").asInt();
        int width = node.get("width").asInt();
        int height = node.get("height").asInt();
        return new Region(x, y, width, height);
    }
}