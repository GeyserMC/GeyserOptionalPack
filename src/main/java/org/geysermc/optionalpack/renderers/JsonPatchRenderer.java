package org.geysermc.optionalpack.renderers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.geysermc.optionalpack.BedrockResourcesWrapper;
import org.geysermc.optionalpack.Constants;
import org.geysermc.optionalpack.FileUtils;
import org.geysermc.optionalpack.OptionalPack;
import org.geysermc.optionalpack.Resources;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonPatchRenderer implements Renderer {
    private static final Path PATCHES_PATH = OptionalPack.TEMP_PATH.resolve("patches");

    @Override
    public String getName() {
        return "Json Patcher";
    }

    @Override
    public String getDestination() {
        return "";
    }

    @Override
    public void render() throws IOException {
        log("Extracting JSON patches...");
        Resources.extractFolder("patches", PATCHES_PATH);

        log("Patching JSON files...");
        try (var stream = Files.walk(PATCHES_PATH)) {
            for (var path : stream.filter(Files::isRegularFile).toList()) {
                String patchFile = PATCHES_PATH.relativize(path).toString().replace("\\", "/");

                log("Applying patch: " + patchFile);
                patchJsonFile(patchFile);
            }
        }

        // Clean up patches folder
        FileUtils.deleteDirectory(PATCHES_PATH);
    }

    private void patchJsonFile(String patchFile) throws IOException {
        String realPath = patchFile.replace(".patch.json", ".json");

        JsonObject sourceJson = JsonParser.parseString(BedrockResourcesWrapper.getResourceAsString(realPath)).getAsJsonObject();
        JsonObject patchJson = JsonParser.parseString(Files.readString(PATCHES_PATH.resolve(patchFile), StandardCharsets.UTF_8)).getAsJsonObject();

        JsonObject merged = mergeJsonObjects(sourceJson, patchJson);

        try (FileWriter writer = new FileWriter(OptionalPack.WORKING_PATH.resolve(realPath).toFile())) {
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
