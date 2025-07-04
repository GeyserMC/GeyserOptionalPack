package org.geysermc.optionalpack;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipFile;

public class JavaAssetRetriever {


    public static void extract(ZipFile clientJar) {
        try {
            InputStream is = new FileInputStream(OptionalPack.WORKING_PATH.resolve("required_files.txt").toFile());
            String str = new String(is.readAllBytes());
            for (String line : str.lines().toList()) {
                String[] paths = line.split(" ");
                InputStream location = clientJar.getInputStream(clientJar.getEntry(paths[0]));

                OptionalPack.log("Extracting " + paths[0] + " to " + paths[1] + "...");
                // it works
                Path destination = OptionalPack.WORKING_PATH.resolve(paths[1]).resolve(Path.of(paths[0]).toFile().getName());
                destination.toFile().mkdirs();

                Files.copy(location, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            is.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream get(ZipFile clientJar, String path) {
        try {
            return clientJar.getInputStream(clientJar.getEntry(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
