package org.geysermc.optionalpack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
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
     * @see #deleteDirectory(File)
     */
    public static void deleteDirectory(Path directory) {
        deleteDirectory(directory.toFile());
    }

    /**
     * Zip a folder
     * From: https://stackoverflow.com/a/57997601
     *
     * @param sourceFolderPath Folder to zip
     * @param zipPath Output path for the zip
     */
    public static void zipFolder(Path sourceFolderPath, Path zipPath) throws Exception {
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
}
