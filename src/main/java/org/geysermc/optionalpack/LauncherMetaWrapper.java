package org.geysermc.optionalpack;

import com.google.gson.Gson;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

public class LauncherMetaWrapper {
    private static final Path CLIENT_JAR = OptionalPack.TEMP_PATH.resolve("client.jar");
    private static final String LAUNCHER_META_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    public static Path getLatest() {
        OptionalPack.log("Downloading " + Constants.JAVA_TARGET_VERSION + " client.jar from Mojang...");

        VersionManifest versionManifest = Constants.GSON.fromJson(HTTP.getAsString(LAUNCHER_META_URL), VersionManifest.class);

        for (Version version : versionManifest.versions()) {
            if (version.id().equals(Constants.JAVA_TARGET_VERSION)) {
                VersionInfo versionInfo = Constants.GSON.fromJson(HTTP.getAsString(version.url()), VersionInfo.class);
                VersionDownload client = versionInfo.downloads().get("client");
                if (!Files.exists(CLIENT_JAR) || !client.sha1.equals(getSha1(CLIENT_JAR))) {
                    // Download the client jar
                    try (InputStream in = HTTP.request(client.url())) {
                        Files.copy(in, CLIENT_JAR);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not download client jar", e);
                    }
                } else {
                    OptionalPack.log("Client jar already exists and is up to date.");
                }
            }
        }

        return CLIENT_JAR;
    }

    private static String getSha1(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream fis = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = fis.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
            }
            byte[] sha1sum = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : sha1sum) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not compute SHA-1 hash", e);
        }
    }

    public record VersionManifest(
        LatestVersion latest,
        List<Version> versions
    ) {}

    public record LatestVersion(
        String release,
        String snapshot
    ) {}

    public record Version(
        String id,
        String type,
        String url,
        String time,
        String releaseTime
    ) {}

    public record VersionInfo(
        String id,
        String type,
        String time,
        String releaseTime,
        Map<String, VersionDownload> downloads
    ) {}

    public record VersionDownload(
        String sha1,
        int size,
        String url
    ) {}
}
