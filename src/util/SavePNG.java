package util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import orthographicembedding.OrthographicEmbeddingResult;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author santi
 */
public class SavePNG {
    public static void savePNG(String fileName, OrthographicEmbeddingResult oe, int cell_width, int cell_height, boolean label_vertices) throws Exception {       
       double vertex_scale = 0.666;
       double auxiliary_vertex_scale = 0.333;
       double edge_scale = 0.1666;
       int fontSize = (int)(cell_height * vertex_scale * 0.5);
       
       double minx = -1;
       double maxx = -1;
       double miny = -1;
       double maxy = -1;
       for(int i = 0;i<oe.nodeIndexes.length;i++) {    
           if (i==0) {
               minx = maxx = oe.x[i];
               miny = maxy = oe.y[i];
           } else {
               if (oe.x[i]<minx) minx = oe.x[i];
               if (oe.x[i]>maxx) maxx = oe.x[i];
               if (oe.y[i]<miny) miny = oe.y[i];
               if (oe.y[i]>maxy) maxy = oe.y[i];
           }
       }
       double width_in_cells = (maxx-minx)+1;
       double height_in_cells = (maxy-miny)+1;
       
       int width = (int)(width_in_cells*cell_width);
       int height = (int)(height_in_cells*cell_height);
       
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        Graphics g = img.getGraphics();
        g.setFont(new Font("TimesRoman", Font.PLAIN, fontSize));
        for(int i = 0;i<oe.nodeIndexes.length;i++) {    
            for(int j = 0;j<oe.nodeIndexes.length;j++) {
                if (oe.edges[i][j]) {
                    double x0 = Math.min(oe.x[i], oe.x[j]) - minx;
                    double y0 = Math.min(oe.y[i], oe.y[j]) - miny;
                    double x1 = Math.max(oe.x[i], oe.x[j]) - minx;
                    double y1 = Math.max(oe.y[i], oe.y[j]) - miny;
//                    System.out.println("drawing edge between " + i + " and " + j + ": " + x0 +"," + y0 + " -> " + x1 + "," + y1);
                    g.setColor(Color.darkGray);
                    g.fillRect((int)(x0*cell_width + cell_width*(0.5 - edge_scale/2)), 
                               (int)(y0*cell_height + cell_height*(0.5 - edge_scale/2)), 
                               (int)((x1-x0)*cell_width + cell_width*edge_scale), 
                               (int)((y1-y0)*cell_height + cell_height*edge_scale));
                    double pointx = x1*cell_width + 0.5*cell_width;
                    double pointy = y1*cell_width + 0.5*cell_width;
                    double vx = x1-x0;
                    double vy = y1-y0;
                    double vn = Math.sqrt(vx*vx+vy*vy);
                    double w = cell_width/2;
                    vx/=vn;
                    vy/=vn;
                    double wx = vy;
                    double wy = -vx;
                    g.fillPolygon(new int[]{(int)pointx,
                                            (int)(pointx-vx*w+wx*w/2),
                                            (int)(pointx-vx*w-wx*w/2)}, 
                                  new int[]{(int)pointy,
                                            (int)(pointy-vy*w+wy*w/2),
                                            (int)(pointy-vy*w-wy*w/2)}, 3);
                }
            }
        }
        for(int i = 0;i<oe.nodeIndexes.length;i++) {    
            if (oe.nodeIndexes[i]>=0) {
                double x0 = oe.x[i] - minx;
                double y0 = oe.y[i] - miny;
                g.setColor(Color.black);
                g.fillRect((int)(x0*cell_width + cell_width*(0.5 - vertex_scale/2)), 
                           (int)(y0*cell_height + cell_height*(0.5 - vertex_scale/2)), 
                           (int)(cell_width*vertex_scale), 
                           (int)(cell_height*vertex_scale));
                
                if (label_vertices) {
                    g.setColor(Color.white);
                    String text = "" + oe.nodeIndexes[i];
                    int w = g.getFontMetrics().stringWidth(text);
                    g.drawString(text, 
                                 (int)(x0*cell_width + cell_width*(0.5) - w/2),
                                 (int)(y0*cell_height + cell_height*0.5 + fontSize/2));
                }
            } else {
                double x0 = oe.x[i] - minx;
                double y0 = oe.y[i] - miny;
                g.setColor(Color.black);
                g.fillRect((int)(x0*cell_width + cell_width*(0.5 - auxiliary_vertex_scale/2)), 
                           (int)(y0*cell_height + cell_height*(0.5 - auxiliary_vertex_scale/2)), 
                           (int)(cell_width*auxiliary_vertex_scale), 
                           (int)(cell_height*auxiliary_vertex_scale));
            }
        }
        
        ImageIO.write(img, "png", new File(fileName));
    }    
}
