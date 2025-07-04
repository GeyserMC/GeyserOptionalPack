package org.geysermc.optionalpack.renderers;

import org.geysermc.optionalpack.JavaAssetRetriever;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.geysermc.optionalpack.OptionalPack.CLIENT_JAR;

public class SweepAttackRenderer implements Renderer {
    @Override
    public String getName() {
        return "Sweep Attack";
    }

    @Override
    public String getDestination() {
        return "textures/geyser/particle/sweep_attack.png";
    }

    public final List<String> spritePaths = List.of(
            "assets/minecraft/textures/particle/sweep_0.png",
            "assets/minecraft/textures/particle/sweep_1.png",
            "assets/minecraft/textures/particle/sweep_2.png",
            "assets/minecraft/textures/particle/sweep_3.png",
            "assets/minecraft/textures/particle/sweep_4.png",
            "assets/minecraft/textures/particle/sweep_5.png",
            "assets/minecraft/textures/particle/sweep_6.png",
            "assets/minecraft/textures/particle/sweep_7.png"
    );

    public final BufferedImage render() throws IOException {
        List<BufferedImage> sprites = new ArrayList<>();
        for (String path : spritePaths) {
            // Retrieve the image from the client jar
            InputStream is = JavaAssetRetriever.get(CLIENT_JAR, path);
            if (is != null) {
                sprites.add(ImageIO.read(is));
            }
        }

        BufferedImage canvas = new BufferedImage(sprites.getFirst().getWidth(), sprites.getFirst().getHeight() * sprites.size(), BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < sprites.size(); i++) {
            BufferedImage sprite = sprites.get(i);
            canvas.getGraphics().drawImage(sprite, 0, i * sprite.getHeight(), null);
        }
        return canvas;
    }
}
