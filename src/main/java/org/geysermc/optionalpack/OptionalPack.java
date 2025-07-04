package org.geysermc.optionalpack;

import org.geysermc.optionalpack.renderers.Renderer;
import org.geysermc.optionalpack.renderers.SweepAttackRenderer;

import javax.imageio.ImageIO;
import java.io.*;
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
    public static final Path TEMP_PATH = Path.of("temp-pack/");
    public static final Path WORKING_PATH = Path.of("temp-pack/optionalpack/");
    public static ZipFile CLIENT_JAR;

    private static List<Renderer> renderers = List.of(
            new SweepAttackRenderer()
    );

    // Files that don't need to be in the pack
    private static List<String> blacklistFiles = List.of(
            "required_files.txt",
            "README.md",
            "prepare_pack.sh",
            "developer_documentation.md"
    );
    private static Instant start;

    public static void main(String[] args) {
        start = Instant.now();
        try {
            log("===GeyserOptionalPack Compiler===");

            /* Step 1: Extract the GeyserOptionalPack data to a working folder */

            log("Extracting pre-made optional pack data to folder...");
            extractOptionalPackDataToFolder();

            // todo: maybe update this to something more recent e.g 1.21.7
            /* Step 2: Download the 1.16 client.jar and copy all files needed to working folder */

            log("Downloading client.jar from Mojang...");
            InputStream in = HTTP.request("https://launcher.mojang.com/v1/objects/37fd3c903861eeff3bc24b71eed48f828b5269c8/client.jar");
            Path jarPath = Path.of("client.jar");
            Files.copy(in, jarPath, StandardCopyOption.REPLACE_EXISTING);
            File jarFile = jarPath.toFile();

            CLIENT_JAR = new ZipFile(jarFile);
            JavaAssetRetriever.extract(CLIENT_JAR);

            /* Step 3: Rendering sprites in a format that we use in the resource pack */
            for (Renderer renderer : renderers) {
                log("Rendering " + renderer.getName() + "...");
                File imageFile = WORKING_PATH.resolve(renderer.getDestination()).toFile();
                if (imageFile.mkdirs()) {
                    ImageIO.write(renderer.render(), "PNG", imageFile);
                }
            }

            /* Step 4: Compile pack folder into a mcpack. */
            // Remove unnecessary files that don't need to be in the pack.
            for (String path : blacklistFiles) {
                Path filePath = WORKING_PATH.resolve(path);

                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            }

            log("Zipping as GeyserOptionalPack.mcpack...");
            zipFolder(WORKING_PATH, Path.of("GeyserOptionalPack.mcpack"));

            /* Step 5: Cleanup temporary folders and files */
            log("Clearing temporary files...");
            CLIENT_JAR.close();
            jarFile.delete();

            deleteDirectory(TEMP_PATH.toFile());
            TEMP_PATH.toFile().delete();

            /* Step 6: Finish!! */
            DecimalFormat r3 = new DecimalFormat("0.000");
            Instant finish = Instant.now();

            log("===Done! (" + r3.format(Duration.between(start, finish).toMillis() / 1000.0d) + "s)===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // thank you https://www.geeksforgeeks.org/java/java-program-to-delete-a-directory/
    public static void deleteDirectory(File file) {
        for (File subfile : file.listFiles()) {
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }
            subfile.delete();
        }
    }

    // there are probably better ways to do this, but this is the way im doing it
    private static void extractOptionalPackDataToFolder() throws Exception {
        File f = new File(OptionalPack.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI());

        unzipPack(f, TEMP_PATH);

    }

    // thank you https://stackoverflow.com/a/57997601
    private static void zipFolder(Path sourceFolderPath, Path zipPath) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
        Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
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

    private static void unzipPack(File file, Path destDir) {
        File dir = destDir.toFile();
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;

        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                if (!ze.isDirectory()) {
                    String fileName = ze.getName();
                    File newFile = new File(destDir + File.separator + fileName);
                    // create directories for subdirectories in zip
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                // close this ZipEntry

                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            // close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void log(String message) {
        System.out.println(message);
    }
}