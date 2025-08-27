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
import java.net.URL;
import java.nio.charset.Charset;

public class Resources {
    /**
     * Returns a resource as an InputStream.
     *
     * @param resourcePath The path to the resource in the JAR file.
     * @return The resource as a BufferedImage.
     */
    public static InputStream getAsStream(String resourcePath) {
        return Resources.class.getClassLoader().getResourceAsStream(resourcePath);
    }

    /**
     * Returns a resource as a URL.
     *
     * @param resourcePath The path to the resource in the JAR file.
     * @return The resource as a URL.
     */
    public static URL get(String resourcePath) {
        return Resources.class.getClassLoader().getResource(resourcePath);
    }

    /**
     * Returns a resource as a String using the default charset (UTF-8).
     *
     * @param resourcePath The path to the resource in the JAR file.
     * @return The resource as a String.
     */
    public static String getAsText(String resourcePath) throws IOException {
        return getAsText(resourcePath, Charset.defaultCharset());
    }

    /**
     * Returns a resource as a String.
     *
     * @param resourcePath The path to the resource in the JAR file.
     * @param charset The charset to use for decoding the resource.
     * @return The resource as a String.
     */
    public static String getAsText(String resourcePath, Charset charset) throws IOException {
        InputStream is = getAsStream(resourcePath);
        String text = new String(is.readAllBytes(), charset);
        is.close();
        return text;
    }

    /**
     * Returns a resource as a BufferedImage.
     *
     * @param resourcePath The path to the resource in the JAR file.
     * @return The resource as a BufferedImage.
     */
    public static BufferedImage getAsImage(String resourcePath) throws IOException {
        InputStream is = getAsStream(resourcePath);
        BufferedImage image = ImageIO.read(is);
        is.close();
        return image;
    }
}
