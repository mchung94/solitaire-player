package com.secondthorn.solitaireplayer.players.pyramid;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.secondthorn.solitaireplayer.players.PlayException;
import org.sikuli.script.Region;

import java.io.IOException;

/**
 * The regions (x, y, width, height) of all the 28 cards on the pyramid as well as the stock and
 * waste piles.  We "hardcode" these into resources/.../regions.json files instead of just searching
 * for images throughout the window, to make it more accurate.
 */

public class Regions {
    public Region[] pyramid;
    public Region stock;
    public Region waste;

    /**
     * A static factory method to load and return the contents of a regions.json file as a new Regions object.
     *
     * @param regionsJsonFilename A resources/.../regions.json filename for Pyramid Solitaire
     * @return a new Regions object containing the regions in the file
     * @throws PlayException if there's a problem reading the file
     */
    static Regions newInstance(String regionsJsonFilename) throws PlayException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule().addDeserializer(Region.class, new RegionDeserializer(null));
        mapper.registerModule(module);
        try {
            return mapper.readValue(ClassLoader.getSystemResource(regionsJsonFilename), Regions.class);
        } catch (IOException ex) {
            throw new PlayException("Unable to load " + regionsJsonFilename, ex);
        }
    }
}

/**
 * A Jackson JSON deserializer for Sikuli Region objects.
 * Jackson's default JSON deserialization behavior has problems with Region objects, and using a mixin to
 * have Jackson ignore properties we don't care about is also hard to do because there's a lot of other
 * classes involved too.  So far I think this is the simplest way to create Region objects based on JSON data.
 */
class RegionDeserializer extends StdDeserializer<Region> {
    RegionDeserializer(Class<?> vc) {
        super(vc);
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