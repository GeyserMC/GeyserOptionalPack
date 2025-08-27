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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class OptionalPack {
    public static final Path TEMP_PATH = Path.of("temp");
    public static final Path WORKING_PATH = TEMP_PATH.resolve("optionalpack");

    /*
    List of renderers that will be used to convert sprites for the resource pack.
    They are executed in order from start to end.
     */
    private static List<Renderer> renderers = List.of(
            new SweepAttackRenderer()
    );

    public static void main(String[] args) {
        Instant start = Instant.now();
        try {
            log("===GeyserOptionalPack Compiler===");

            // Step 1: Extract the GeyserOptionalPack data to a working folder

            log("Extracting pre-made optional pack data to folder...");
            // there are probably better ways to do this, but this is the way im doing it
            unzipPack(Resources.get("optionalpack"), WORKING_PATH);

            // Step 2: Download the 1.21.8 client.jar and copy all files needed to working folder
            File jarFile = LauncherMetaWrapper.getLatest().toFile();

            ZipFile clientJar = new ZipFile(jarFile);
            JavaResources.extract(clientJar);

            /* Step 3: Rendering sprites in a format that we use in the resource pack */
            for (Renderer renderer : renderers) {
                log("Rendering " + renderer.getName() + "...");
                File imageFile = WORKING_PATH.resolve(renderer.getDestination()).toFile();
                if (imageFile.mkdirs()) {
                    ImageIO.write(renderer.render(), "PNG", imageFile);
                }
            }

            // Step 4: Compile pack folder into a mcpack.
            log("Zipping as GeyserOptionalPack.mcpack...");
            zipFolder(WORKING_PATH, Path.of("GeyserOptionalPack.mcpack"));

            // Step 5: Cleanup temporary folders and files
            log("Clearing temporary files...");
            clientJar.close();
            deleteDirectory(WORKING_PATH.toFile());

            // Step 6: Finish!!
            DecimalFormat r3 = new DecimalFormat("0.000");
            Instant finish = Instant.now();

            log("===Done! (" + r3.format(Duration.between(start, finish).toMillis() / 1000.0d) + "s)===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a directory and all files within it
     * From: https://www.geeksforgeeks.org/java/java-program-to-delete-a-directory/
     *
     * @param directory The directory to remove
     */
    public static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File subfile : directory.listFiles()) {
                if (subfile.isDirectory()) {
                    deleteDirectory(subfile);
                }
                subfile.delete();
            }
        }

        directory.delete();
    }

    /**
     * Zip a folder
     * From: https://stackoverflow.com/a/57997601
     *
     * @param sourceFolderPath Folder to zip
     * @param zipPath Output path for the zip
     */
    private static void zipFolder(Path sourceFolderPath, Path zipPath) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
        Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
                Files.copy(file, zos);
                zos.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
        zos.close();
    }

    /**
     * Extract a zip to a given directory
     *
     * @param file The zip to extract
     * @param destDir THe destination to put all the files
     */
    private static void unzipPack(URL file, Path destDir) {
        File dir = destDir.toFile();
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();

        try {
            if (file.getProtocol().equals("file")) {
                Path resourceDir = Paths.get(file.toURI());
                Files.walk(resourceDir)
                    .filter(Files::isRegularFile)
                    .forEach(source -> {
                        try {
                            Path relative = resourceDir.relativize(source);
                            Path target = destDir.resolve(relative);
                            Files.createDirectories(target.getParent());
                            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            } else {
                byte[] buffer = new byte[1024];
                FileInputStream fileStream = new FileInputStream(new File(file.toURI()));
                ZipInputStream zipStream = new ZipInputStream(fileStream);
                ZipEntry entry = zipStream.getNextEntry();
                while (entry != null) {
                    if (!entry.isDirectory()) {
                        String fileName = entry.getName();
                        File newFile = new File(destDir + File.separator + fileName);
                        // create directories for subdirectories in zip
                        new File(newFile.getParent()).mkdirs();
                        FileOutputStream extractedFile = new FileOutputStream(newFile);
                        int len;
                        while ((len = zipStream.read(buffer)) > 0) {
                            extractedFile.write(buffer, 0, len);
                        }
                        extractedFile.close();
                    }
                    // close this ZipEntry

                    zipStream.closeEntry();
                    entry = zipStream.getNextEntry();
                }
                // close the last ZipEntry
                zipStream.closeEntry();
                zipStream.close();
                fileStream.close();
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Unable to unzip pack!", e);
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