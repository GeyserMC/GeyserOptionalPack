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

package org.geysermc.optionalpack.renderers;

import org.geysermc.optionalpack.JavaResources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Renderer for creating a vertical sprite sheet from a list of Java sprite paths.
 * The sprites are stacked vertically in the output image.
 */
public class VerticalSpriteSheetRenderer implements Renderer {
    private final String name;
    private final String destination;
    private final List<String> spritePaths;

    public VerticalSpriteSheetRenderer(String name, String destination, List<String> spritePaths) {
        this.name = name;
        this.destination = destination;
        this.spritePaths = spritePaths;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDestination() {
        return destination;
    }

    @Override
    public BufferedImage render() throws IOException {
        List<BufferedImage> sprites = new ArrayList<>();
        for (String path : spritePaths) {
            // Retrieve the image from the client jar
            BufferedImage is = JavaResources.getAsImage(path);
            sprites.add(is);
        }

        BufferedImage canvas = new BufferedImage(sprites.getFirst().getWidth(), sprites.getFirst().getHeight() * sprites.size(), BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < sprites.size(); i++) {
            BufferedImage sprite = sprites.get(i);
            canvas.getGraphics().drawImage(sprite, 0, i * sprite.getHeight(), null);
        }
        return canvas;
    }
}
