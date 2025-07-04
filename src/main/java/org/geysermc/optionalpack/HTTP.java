package org.geysermc.optionalpack;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;



public class HTTP {
    public static InputStream request(URL url) {
        try {
            URLConnection cn = url.openConnection();
            cn.setConnectTimeout(5000);
          //  cn.setRequestProperty("User-Agent", "GeyserOptionalPackCompiler/GeyserOptionalPackCompiler/1.0.0");
            cn.connect();
            return cn.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream request(String url) {
        try {
            return request(new URL(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String asText(URL url) {
        try {
            return new String(request(url).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static String asText(String url) {
        try {
            return asText(new URL(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}