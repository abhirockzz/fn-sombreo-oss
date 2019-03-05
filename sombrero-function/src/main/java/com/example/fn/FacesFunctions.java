package com.example.fn;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.Comparator;
import java.util.List;

public class FacesFunctions {

    static {
        OpenCVInit.loadLibrary();
        // TODO : move this to the FDK startup
        System.setProperty("java.awt.headless", "true");
    }

    private final CascadeClassifier faceClassifier;
    private final BufferedImage sombrero;

    public FacesFunctions() throws IOException {
        faceClassifier = new CascadeClassifier();
        if (!faceClassifier.load("data/haarcascade_frontalface_alt.xml")) {
            throw new RuntimeException("Failed to load face classifier ");
        }
        sombrero = ImageIO.read(new File("data/sombrero.png"));
    }

    public byte[] handleRequest(byte[] image) {

        BufferedImage img = null;
        try {
            img = ImageIO.read(new ByteArrayInputStream(image));
        } catch (IOException ex) {
            System.out.println("Unable to read image bytes " + ex.getMessage());
            return null;
        }
        List<Rect> faceRectangles = detectFaces(img);
        // Sort the face boxes by size smallest to largest (for Z-order)
        faceRectangles.sort(Comparator.comparingInt(a -> a.width));

        Graphics2D g = img.createGraphics();
        for (Rect r : faceRectangles) {
            System.err.println("Found rect " + r);
            double sombreroRatio = (double) sombrero.getWidth() / (double) sombrero.getHeight();
            double hatWidth = r.width * 2.3;
            double hatHeight = hatWidth / sombreroRatio;

            g.drawImage(sombrero, (int) ((double) r.x - ((hatWidth - r.width) / 2.0)), r.y - (int) hatHeight + (int) (r.height * .3), (int) hatWidth, (int) hatHeight, null);
        }
        System.err.println("found " + faceRectangles.size() + " faces on object ");

        ByteArrayOutputStream opStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "jpg", opStream);
        } catch (IOException ex) {
            System.out.println("Unable to write tranformed image bytes " + ex.getMessage());
            return null;

        }
        byte[] result = opStream.toByteArray();
        System.err.println("Image was successfully transformed");
        System.err.println("result bytes " + result.length);
        g.dispose();

        return result;
    }

    private List<Rect> detectFaces(BufferedImage bi) {

        Mat frame = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        try {
            byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
            frame.put(0, 0, data);

            MatOfRect faces = new MatOfRect();
            try {

                Mat grayFrame = new Mat();
                try {

                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.equalizeHist(grayFrame, grayFrame);

                    int height = grayFrame.rows();
                    double absoluteFaceSize = Math.round(height * 0.2f);
                    this.faceClassifier.detectMultiScale(grayFrame, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                            new Size(absoluteFaceSize, absoluteFaceSize), new Size());
                } finally {
                    grayFrame.release();

                }
                return faces.toList();
            } finally {
                faces.release();
            }

        } finally {
            frame.release();
        }

    }
}
