package org.geysermc.optionalpack;

import java.io.InputStream;

public class BedrockResourcesWrapper {
    private static final String BEDROCK_RESOURCES_URL = "https://raw.githubusercontent.com/Mojang/bedrock-samples/refs/tags/v" + Constants.BEDROCK_TARGET_VERSION + "/resource_pack/%s";

    public static String getResourceAsString(String path) {
        return WebUtils.getAsString(BEDROCK_RESOURCES_URL.formatted(path));
    }

    public static InputStream getResource(String path) {
        return WebUtils.request(BEDROCK_RESOURCES_URL.formatted(path));
    }
}
