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

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class WebUtils {

    /**
     * Requests a URL and returns the InputStream.
     *
     * @param url The URL to request.
     * @return The InputStream of the requested URL.
     */
    public static InputStream request(URL url) {
        try {
            URLConnection cn = url.openConnection();
            cn.setConnectTimeout(5000);
            // TODO: proper versioning here
            cn.setRequestProperty("User-Agent", "GeyserMC/GeyserOptionalPackCompiler/1.0.0");
            cn.connect();
            return cn.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Requests a URL and returns the InputStream.
     *
     * @param url The URL to request.
     * @return The InputStream of the requested URL.
     */
    public static InputStream request(String url) {
        try {
            return request(URI.create(url).toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Requests a URL and returns the data as a String.
     *
     * @param url The URL to request.
     * @return The data of the requested URL as a String.
     */
    public static String getAsString(URL url) {
        try (InputStream request = request(url)) {
            return new String(request.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Requests a URL and returns the data as a String.
     *
     * @param url The URL to request.
     * @return The data of the requested URL as a String.
     */
    public static String getAsString(String url) {
        try {
            return getAsString(URI.create(url).toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}