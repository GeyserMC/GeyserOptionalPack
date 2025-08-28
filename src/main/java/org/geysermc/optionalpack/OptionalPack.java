/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/GeyserOptionalPack
 */

package org.geysermc.optionalpack;

import org.geysermc.optionalpack.renderers.Renderer;
import org.geysermc.optionalpack.renderers.SweepAttackRenderer;
import org.reflections.Reflections;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class OptionalPack {
    public static final Path TEMP_PATH = Path.of("temp");
    public static final Path WORKING_PATH = TEMP_PATH.resolve("optionalpack");

    private static final Renderer[] RENDERERS;

    static {
        Reflections reflections = new Reflections("org.geysermc.optionalpack.renderers");
        Set<Class<? extends Renderer>> renderers = reflections.getSubTypesOf(Renderer.class);

        RENDERERS = renderers.stream().map(rendererClass -> {
            try {
                return rendererClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).toArray(Renderer[]::new);
    }

    public static void main(String[] args) {
        Instant start = Instant.now();
        try {
            log("===GeyserOptionalPack Compiler===");

            // Step 1: Extract the GeyserOptionalPack data to a working folder

            log("Extracting pre-made optional pack data to folder...");
            // there are probably better ways to do this, but this is the way im doing it
            Resources.extractFolder("optionalpack", WORKING_PATH);

            // Step 2: Download the 1.21.8 client.jar and copy all files needed to working folder
            File jarFile = LauncherMetaWrapper.getLatest().toFile();

            ZipFile clientJar = new ZipFile(jarFile);
            JavaResources.extract(clientJar);

            /* Step 3: Rendering sprites in a format that we use in the resource pack */
            for (Renderer renderer : RENDERERS) {
                log("Rendering " + renderer.getName() + "...");
                File destinationFolder = renderer.getDestinationPath().toFile().getParentFile();
                if (!destinationFolder.exists()) {
                    if (!destinationFolder.mkdirs()) {
                        throw new IOException("Failed to create directory: " + destinationFolder);
                    }
                }
                renderer.render();
            }

            // Step 4: Compile pack folder into a mcpack.
            log("Zipping as GeyserOptionalPack.mcpack...");
            FileUtils.zipFolder(WORKING_PATH, Path.of("GeyserOptionalPack.mcpack"));

            // Step 5: Cleanup temporary folders and files
            log("Clearing temporary files...");
            clientJar.close();
            FileUtils.deleteDirectory(WORKING_PATH);

            // Step 6: Finish!!
            DecimalFormat r3 = new DecimalFormat("0.000");
            Instant finish = Instant.now();

            log("===Done! (" + r3.format(Duration.between(start, finish).toMillis() / 1000.0d) + "s)===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints a message to the console.
     *
     * @param message The message to log.
     */
    public static void log(String message) {
        System.out.println(message);
    }
}