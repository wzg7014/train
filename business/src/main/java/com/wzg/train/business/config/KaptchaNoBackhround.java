package com.wzg.train.business.config;

import com.google.code.kaptcha.BackgroundProducer;
import com.google.code.kaptcha.util.Configurable;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class KaptchaNoBackhround extends Configurable implements BackgroundProducer {
    @Override
    public BufferedImage addBackground(BufferedImage baseImage) {
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, 1);
        Graphics2D graph = (Graphics2D) bufferedImage.getGraphics();
        graph.fill(new Rectangle2D.Double(0.0D,0.0D,(double) width,(double) height));
        graph.drawImage(baseImage,0,0,null);

        return bufferedImage;
    }
}

