package org.geysermc.optionalpack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Constants {
    public static final String JAVA_TARGET_VERSION = "1.21.8";
    public static final String BEDROCK_TARGET_VERSION = "1.21.100.6";

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
}
