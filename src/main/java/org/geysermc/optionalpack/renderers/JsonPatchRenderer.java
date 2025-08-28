package org.geysermc.optionalpack.renderers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.geysermc.optionalpack.BedrockResourcesWrapper;
import org.geysermc.optionalpack.Constants;

import java.io.FileWriter;
import java.io.IOException;

public class JsonPatchRenderer implements Renderer {
    private final String name;
    private final String file;
    private final String patch;

    public JsonPatchRenderer(String name, String file, String patch) {
        this.name = name;
        this.file = file;
        this.patch = patch;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDestination() {
        return file;
    }

    @Override
    public void render() throws IOException {
        JsonObject sourceJson = JsonParser.parseString(BedrockResourcesWrapper.getResourceAsString(getDestination())).getAsJsonObject();
        JsonObject patchJson = JsonParser.parseString(patch).getAsJsonObject();

        JsonObject merged = mergeJsonObjects(sourceJson, patchJson);

        try (FileWriter writer = new FileWriter(getDestinationPath().toFile())) {
            writer.write(Constants.GSON.toJson(merged));
        }
    }

    /**
     * Merges two JsonObjects. In case of conflicts, values from obj2 take precedence.
     * If both values are JsonObjects, they are merged recursively.
     *
     * @param obj1 The first JsonObject
     * @param obj2 The second JsonObject
     * @return The merged JsonObject
     */
    private static JsonObject mergeJsonObjects(JsonObject obj1, JsonObject obj2) {
        JsonObject merged = obj1.deepCopy(); // Start with a copy of the first

        for (String key : obj2.keySet()) {
            JsonElement value2 = obj2.get(key);
            if (merged.has(key)) {
                JsonElement value1 = merged.get(key);

                // If both are JsonObjects, recursively merge
                if (value1.isJsonObject() && value2.isJsonObject()) {
                    merged.add(key, mergeJsonObjects(value1.getAsJsonObject(), value2.getAsJsonObject()));
                } else {
                    // Override with value from obj2
                    merged.add(key, value2);
                }
            } else {
                merged.add(key, value2);
            }
        }

        return merged;
    }
}
