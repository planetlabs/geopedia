/*
 *
 */
package com.sinergise.java.gis.ogc.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.imageio.ImageIO;

public class ErrorImageCreator {
    public static byte[] createErrorPNG(String error, int w, int h) {
        BufferedImage bi=new BufferedImage(w,h,BufferedImage.TYPE_INT_BGR);
        Graphics2D g2d=bi.createGraphics();
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, w, h);
        g2d.setColor(Color.RED);
        Point2D.Float pen = new Point2D.Float(10, 10);
        
        error = error.replaceAll("\\n", " /-/ ");
        
        AttributedString as=new AttributedString(error);
        as.addAttribute(TextAttribute.FONT, g2d.getFont());
        AttributedCharacterIterator aci = as.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(aci, g2d.getFontRenderContext());
        float wrappingWidth = w<40?w:w - 20;

        while (measurer.getPosition() < error.length()-1) {

            TextLayout layout = measurer.nextLayout(wrappingWidth);

            pen.y += (layout.getAscent());
            float dx = layout.isLeftToRight() ?
                0 : (wrappingWidth - layout.getAdvance());

            layout.draw(g2d, pen.x + dx, pen.y);
            pen.y += layout.getDescent() + layout.getLeading();
        }
        
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "PNG", bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }
}
