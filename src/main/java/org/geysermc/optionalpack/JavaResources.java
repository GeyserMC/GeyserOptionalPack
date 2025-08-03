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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipFile;

public class JavaResources {
    private static ZipFile clientJar;
    public static void extract(ZipFile clientJar) {
        JavaResources.clientJar = clientJar;
        try {
            String str = Resources.getAsText("required_files.txt");
            for (String line : str.lines().toList()) {
                String[] paths = line.split(" ");
                String jarAsset = paths[0];
                String destinationPath = paths[1];
                InputStream location = clientJar.getInputStream(clientJar.getEntry(jarAsset));

                OptionalPack.log("Extracting " + jarAsset + " to " + destinationPath + "...");
                // it works
                Path destination = OptionalPack.WORKING_PATH.resolve(destinationPath).resolve(Path.of(jarAsset).toFile().getName());
                if (destination.toFile().mkdirs()) {
                    Files.copy(location, destination, StandardCopyOption.REPLACE_EXISTING);
                }
                else {
                    OptionalPack.log("Could not make directories for copying " + jarAsset + " to " + destinationPath + "!");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getAsStream(String path) {
        try {
            return clientJar.getInputStream(clientJar.getEntry(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAsText(String resourcePath) throws IOException {
        return getAsText(resourcePath, Charset.defaultCharset());
    }

    public static String getAsText(String resourcePath, Charset charset) throws IOException {
        InputStream is = getAsStream(resourcePath);
        String text = new String(is.readAllBytes(), charset);
        is.close();
        return text;
    }
    public static BufferedImage getAsImage(String resourcePath) throws IOException {
        InputStream is = getAsStream(resourcePath);
        BufferedImage image = ImageIO.read(is);
        is.close();
        return image;
    }
}
