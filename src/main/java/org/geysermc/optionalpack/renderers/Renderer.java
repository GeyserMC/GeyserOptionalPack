package org.geysermc.optionalpack.renderers;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface Renderer {
    String getName();
    String getDestination();
    BufferedImage render() throws IOException;
}
