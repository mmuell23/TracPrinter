package de.tracprinter;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

public class QRCodeGenerator {
    public static Image getCodeForURL(String url) {
        BufferedImage i = null;
        
        try {
            i = ImageIO.read(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }
}
