package com.amc.utils;

import com.sun.imageio.plugins.gif.GIFImageWriter;
import com.sun.imageio.plugins.gif.GIFImageWriterSpi;
import com.sun.imageio.plugins.gif.GIFStreamMetadata;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class GenerateGene {
    private static String imageCodeGroup = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static int DEFAULT_WIDTH = 300;
    private static int DEFAULT_HEIGHT = 100;

    private static ExecutorService executorService = null;
    private static HashMap<String, DisturbCode> FullBuffer =  new HashMap<>();

    static {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        for(int i = 0 ; i< imageCodeGroup.length(); i++){
            DisturbCode temp = new DisturbCode(DEFAULT_WIDTH,DEFAULT_HEIGHT,String.valueOf(imageCodeGroup.charAt(i)));
            temp.start();
            //executorService.submit(temp);
            FullBuffer.put(String.valueOf(imageCodeGroup.charAt(i)),temp);
        }
    }



    public static String createImage(String code){
        try {
            if(Strings.isNullOrEmpty(code)){
                code = getImageCode(random.nextInt(2)+4);
            }
            return outputImage(DEFAULT_WIDTH,DEFAULT_HEIGHT,code);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getImageCode(int size){
        return  generateVerifyCode(size,imageCodeGroup);
    }

    private static Random random = new Random();

    /**
     * 使用指定源生成验证码
     * @param verifySize    验证码长度
     * @param sources   验证码字符源
     * @return
     */
    private static String generateVerifyCode(int verifySize, String sources){
        if(sources == null || sources.length() == 0){
            sources = imageCodeGroup;
        }
        int codesLen = sources.length();
        Random rand = new Random(System.currentTimeMillis());
        StringBuilder verifyCode = new StringBuilder(verifySize);
        for(int i = 0; i < verifySize; i++){
            verifyCode.append(sources.charAt(rand.nextInt(codesLen-1)));
        }
        return verifyCode.toString();
    }
    /**
     * 输出指定验证码图片流
     * @param w
     * @param h
     * @param code
     * @throws IOException
     */
    private static String outputImage(int w, int h, String code) throws IOException{
        int verifySize = code.length();


        char[] chars = code.toCharArray();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = new FileCacheImageOutputStream(outputStream,null);
        GifSequenceWriter gifSequenceWriter = new GifSequenceWriter(imageOutputStream, BufferedImage.TYPE_INT_RGB,20,true);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0,0,w,h);
        g2.setColor(Color.BLACK);
        Shape num = FullBuffer.get(code).getShape();
        Rectangle bounds = num.getBounds();
        g2.translate(
                Math.abs((w/4 - bounds.width) - bounds.x),
                (h - bounds.height) / 2 - bounds.y
        );
        g2.setColor(Color.WHITE);
        g2.fill(num);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(Color.BLACK);
        g2.draw(num);
        gifSequenceWriter.writeToSequence(image);
        gifSequenceWriter.writeToSequence(image);
        gifSequenceWriter.writeToSequence(image);
//        for(int j=0; j<100; j++){
//            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
//            Random rand = new Random();
//            Graphics2D g2 = image.createGraphics();
//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
//            g2.setColor(Color.WHITE);// 设置背景色
//            g2.fillRect(0, 0, w, h);
//            g2.setColor(Color.BLACK);
//            int fontSize = h-4;
//            Font font = new Font("Algerian", Font.PLAIN, fontSize);
//            g2.setFont(font);
//
//            for(int i = 0; i < verifySize; i++){
//                AffineTransform affine = new AffineTransform();
//                affine.setToRotation(Math.PI / 4 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1), (w / verifySize) * i + fontSize/2, h/2);
//                g2.setTransform(affine);
//                GlyphVector v = font.createGlyphVector(g2.getFontMetrics(font).getFontRenderContext(), String.valueOf(chars[i]));
//                Shape shape = v.getOutline();
//                Rectangle bounds = shape.getBounds();
//                g2.translate(
//                        Math.abs((w/4 - bounds.width) - bounds.x + (i*w)/4 - rand.nextInt(w/4-bounds.width)),
//                        (h - bounds.height) / 2 - bounds.y
//                );
//                System.out.println(w + " " + bounds.width + " " + bounds.x + " " + ((w - bounds.width) / 2 - bounds.x));
//                g2.setColor(Color.WHITE);
//                g2.fill(shape);
//                g2.setColor(Color.BLACK);
//                g2.setStroke(new BasicStroke(3f));
//                g2.draw(shape);
//            }
//            gifSequenceWriter.writeToSequence(image);
//        }
        imageOutputStream.flush();
        gifSequenceWriter.close();
        outputStream.close();
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(outputStream.toByteArray());
    }

    static class DisturbCode extends Thread {

        private String dis;
        private int width;
        private int height;
        Graphics2D g2;
        Shape shape;

        public DisturbCode(int width, int height, String dis){
            this.dis = dis;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {
            BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            g2 = image.createGraphics();
            Font font = new Font("Algerian",Font.PLAIN,height-10);
            GlyphVector v = font.createGlyphVector(g2.getFontMetrics(font).getFontRenderContext(),dis);
            Shape shape = v.getOutline();
            while(true){
                try {
                    //进行胡乱切割形成相似元素
                    Arc2D slice = new Arc2D.Float();
                    double start = random.nextDouble()*150 + 30;
                    double end = random.nextDouble()*300 + 60;
                    slice.setArc(v.getVisualBounds(),start,end,Arc2D.PIE);
                    Area show = new Area(shape);
                    show.subtract(new Area(slice));
                    this.shape = show;
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public Shape getShape(){
            return shape;
        }
    }
}
