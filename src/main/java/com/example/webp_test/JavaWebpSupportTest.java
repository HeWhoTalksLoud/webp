package com.example.webp_test;

import com.luciad.imageio.webp.WebPWriteParam;
import org.apache.commons.io.FileUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class JavaWebpSupportTest {

    public static void main(String[] args) throws Exception {
        JavaWebpSupportTest webPSupportTest = new JavaWebpSupportTest();
        webPSupportTest.readWebpImage("1.webp");
        webPSupportTest.convertFileToWebpWithLossyCompression("dora.png", 0.6f);
        webPSupportTest
                .resizeFile("tiger.jpeg", "tiger_res.jpeg", 800, 400,
                        false);
    }

    public void readWebpImage(String fileName) throws IOException {
        BufferedImage image = ImageIO.read(new File(fileName));
        System.out.printf("\nDimension of the image:%dx%d", image.getWidth(), image.getHeight());
    }

    public void convertFileToWebpWithLossyCompression(String fileName, float compressionQuality)
            throws Exception {
        File fileToConvert = new File(fileName);
        byte[] fileContent = Files.readAllBytes(fileToConvert.toPath());
        byte[] convertedFileContent = convertToWebpWithLossyCompression(fileContent, compressionQuality);
        String webpFileName = fileName.substring(0, fileName.lastIndexOf(".")).concat(".webp");
        File webpFile = new File(webpFileName);
        // writes output data to file
        FileUtils.writeByteArrayToFile(webpFile, convertedFileContent);
    }

    public byte[] convertToWebpWithLossyCompression(byte[] data, float compressionQuality) throws Exception {

        // reads input image
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        BufferedImage image = ImageIO.read(bis);

        ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();

        WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
        //Notify encoder to consider WebPWriteParams
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        //Set lossy compression
        writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
        //Set compression quality. Allowed values are between 0 and 1
        writeParam.setCompressionQuality(compressionQuality);

        // Writes to output byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios =  ImageIO.createImageOutputStream(bos);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(image, null, null), writeParam);
        ios.flush();

        return bos.toByteArray();
    }

    public void resizeFile(String inputFileName, String outputFileName, int scaledWidth, int scaledHeight,
                       boolean maintainAspectRatio)
            throws IOException {
        // reads input image
        File inputFile = new File(inputFileName);
        String format = inputFileName.substring(inputFileName.lastIndexOf(".") + 1);
        byte[] fileContent = Files.readAllBytes(inputFile.toPath());
        byte[] resizedFileContent = resize(fileContent, format,
                scaledWidth, scaledHeight, maintainAspectRatio);
        File outputFile = new File(outputFileName);
        // writes output data to file
        FileUtils.writeByteArrayToFile(outputFile, resizedFileContent);
    }

    public byte[] resize(byte[] data, String format, int scaledWidth, int scaledHeight,
                       boolean maintainAspectRatio)
            throws IOException {
        // reads input image
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        BufferedImage inputImage = ImageIO.read(bis);

        if (maintainAspectRatio) {
            Dimension scaledDimension =
                    getScaledDimension(new Dimension(inputImage.getWidth(), inputImage.getHeight()),
                            new Dimension(scaledWidth, scaledHeight));
            scaledWidth = (int)scaledDimension.getWidth();
            scaledHeight = (int)scaledDimension.getHeight();
        }

        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        // writes to output byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios =  ImageIO.createImageOutputStream(bos);
        ImageIO.write(outputImage, format, ios);
        ios.flush();

        return bos.toByteArray();
    }

    private static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }

        return new Dimension(new_width, new_height);
    }

}