package com.amc.utils;

import com.sun.imageio.plugins.gif.GIFImageWriter;
import com.sun.imageio.plugins.gif.GIFImageWriterSpi;
import com.sun.imageio.plugins.gif.GIFStreamMetadata;
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class GenerateGene {
    private static String imageCodeGroup = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static int DEFAULT_WIDTH = 300;
    private static int DEFAULT_HEIGHT = 100;
    private static int DEFAULT_REPIT = 10;  //十张

    private static ExecutorService executorService = null;
    private static HashMap<String, Runnable> fontGroup = new HashMap<>();
    private static HashMap<String, Runnable> geneGroup = new HashMap<>();

    static {

    }

    static {
        for(int i=0; i<imageCodeGroup.length(); i++){
            String c = String.valueOf(imageCodeGroup.charAt(i));
        }
    }
    static {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public static String createImage(String code, Integer level){
        try {
            return outputImage(DEFAULT_WIDTH,DEFAULT_HEIGHT,code,level);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getImageCode(int size){
        return  generateVerifyCode(size,imageCodeGroup);
    }
    //使用到Algerian字体，系统里没有的话需要安装字体，字体只显示大写，去掉了1,0,i,o几个容易混淆的字符
    private static Random random = new Random();
    /**
     * 使用系统默认字符源生成验证码
     * @param verifySize    验证码长度
     * @return
     */
    public static String generateVerifyCode(int verifySize){
        return generateVerifyCode(verifySize, imageCodeGroup);
    }
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
    private static String outputImage(int w, int h, String code, int level) throws IOException{
        int verifySize = code.length();


        char[] chars = code.toCharArray();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //FileCacheImageOutputStream imageOutputStream = new FileCacheImageOutputStream(outputStream,null);
        ImageOutputStream imageOutputStream = new FileImageOutputStream(new File("/Users/iamcap/Desktop/x.gif"));
        GifSequenceWriter gifSequenceWriter = new GifSequenceWriter(imageOutputStream, BufferedImage.TYPE_INT_RGB,20,true);
        for(int j=0; j<100; j++){
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Random rand = new Random();
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);// 设置背景色
            g2.fillRect(0, 0, w, h);
            g2.setColor(Color.BLACK);
            int fontSize = h-4;
            Font font = new Font("Algerian", Font.PLAIN, fontSize);
            g2.setFont(font);

            for(int i = 0; i < verifySize; i++){
                AffineTransform affine = new AffineTransform();
                affine.setToRotation(Math.PI / 4 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1), (w / verifySize) * i + fontSize/2, h/2);
                g2.setTransform(affine);
                GlyphVector v = font.createGlyphVector(g2.getFontMetrics(font).getFontRenderContext(), String.valueOf(chars[i]));
                Shape shape = v.getOutline();
                Rectangle bounds = shape.getBounds();
                g2.translate(
                        Math.abs((w/4 - bounds.width) - bounds.x + (i*w)/4 - rand.nextInt(w/4-bounds.width)),
                        (h - bounds.height) / 2 - bounds.y
                );
                System.out.println(w + " " + bounds.width + " " + bounds.x + " " + ((w - bounds.width) / 2 - bounds.x));
                g2.setColor(Color.WHITE);
                g2.fill(shape);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(shape);
            }
            gifSequenceWriter.writeToSequence(image);
        }

        gifSequenceWriter.close();
        outputStream.close();
//        g2.dispose();
        /**
         * 转BASE64
         */
        
//        ImageIO.write(image, "gif", outputStream);
        System.out.printf("size:"+ outputStream.size());
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(outputStream.toByteArray());
    }

    //线程随时刷新
    static class FontTask implements Runnable{
        volatile Shape shape = null;
        String x;
        FontTask(String x){
            this.x = x;
        }

        @Override
        public void run() {
            int w = DEFAULT_WIDTH/4;
            int h = DEFAULT_HEIGHT/4;
            BufferedImage image = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            while(true){
                AffineTransform affine = new AffineTransform();
                affine.setToRotation(Math.PI / 4 * random.nextDouble() * (random.nextBoolean() ? 1 : -1), 0, h/2);
                g2.setTransform(affine);
                Font font = new Font("Algerian", Font.PLAIN, h);
                GlyphVector v = font.createGlyphVector(g2.getFontMetrics(font).getFontRenderContext(), x);
                Shape shape = v.getOutline();
                Rectangle bounds = shape.getBounds();
                g2.translate(
                        Math.abs((w/4 - bounds.width) - bounds.x  - random.nextInt(w/4-bounds.width)),
                        (h - bounds.height) / 2 - bounds.y
                );
                g2.setColor(Color.WHITE);
                g2.fill(shape);
                g2.setColor(Color.BLACK);

                g2.setStroke(new BasicStroke(3f));
                this.shape = g2.getClip();
                try {
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

    static class AdditionTask implements Runnable{
        volatile Shape shape = null;
        volatile Graphics2D graphics2D = null;
        String code;
        AdditionTask(Graphics2D graphics2D , String code){
            this.graphics2D = graphics2D;
            this.code = code;
        }

        @Override
        public void run() {
            BufferedImage image = new BufferedImage(DEFAULT_WIDTH,DEFAULT_HEIGHT,BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            while(true){
                AffineTransform affineTransform = new AffineTransform();
            }
        }
    }

}
