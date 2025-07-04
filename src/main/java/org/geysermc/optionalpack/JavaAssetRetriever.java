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
            InputStream is = JavaAssetRetriever.class.getClassLoader().getResourceAsStream("required_files.txt");
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
