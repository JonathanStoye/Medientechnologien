import java.io.*;
import java.util.HashMap;

public final class bmp_io {

    // Run with zero command-line arguments. This program reads Demo.bmp and writes Demo2.bmp in the current directory.
    public static void main(String[] args) throws IOException {
        String inFilename = null;
        String outFilename = null;
        PixelColor pc = null;
        BmpImage bmp = null;

        if (args.length < 1)
            System.out.println("At least one filename specified  (" + args.length + ")");

        inFilename = args[0];
        InputStream in = new FileInputStream(inFilename);
        bmp = BmpReader.read_bmp(in);
        PrintWriter writer = new PrintWriter("horizontal.txt", "UTF-8");

        // BGR schreiben horizontal 2.1.
        for (int x = 0; x < bmp.image.getWidth(); x++) {
            writer.println(bmp.image.getRgbPixel(x, 0).b + " " + bmp.image.getRgbPixel(x, 0).g + " " + bmp.image.getRgbPixel(x, 0).r);
        }
        writer.close();

        writer = new PrintWriter("vertikal.txt", "UTF-8");
        // BGR schreiben vertikal 2.1.
        for (int y = 0; y < bmp.image.getHeight(); y++) {
            writer.println(bmp.image.getRgbPixel(0, y).b + " " + bmp.image.getRgbPixel(0, y).g + " " + bmp.image.getRgbPixel(0, y).r);
        }

        writer.close();

        OutputStream out = new FileOutputStream("original.bmp");
        OutputStream outGrey = new FileOutputStream("graustufenbild.bmp");
        OutputStream outDownsampling = new FileOutputStream("downsampled.bmp");
        OutputStream outDownsamplingVertical = new FileOutputStream("downsampling_vertical.bmp");
        OutputStream outBitreduced = new FileOutputStream("bitreduced.bmp");
        OutputStream outDiff = new FileOutputStream("difference.bmp");

        try {
            BmpWriter.write_bmp(out, bmp);
        } finally

        {
            out.close();
        }


        // erzeuge graustufenbild
        for (int y = 0; y < bmp.image.getHeight(); y++) {
            for (int x = 0; x < bmp.image.getWidth(); x++) {
                int r = bmp.image.getRgbPixel(x, y).r;
                int g = bmp.image.getRgbPixel(x, y).g;
                int b = bmp.image.getRgbPixel(x, y).b;
                int mw = (r + g + b) / 3;
                bmp.image.setRgbPixel(x, y, new PixelColor(mw, mw, mw));

            }
        }
        try {
            BmpWriter.write_bmp(outGrey, bmp);
        } finally

        {
            outGrey.close();
        }

        in = new FileInputStream(inFilename);
        bmp = BmpReader.read_bmp(in);
        // downsampling horizontal
        for (int y = 0; y < bmp.image.getHeight(); y++) {
            for (int x = 0; x < bmp.image.getWidth(); x++) {
                if (x % 2 == 1) {
                    bmp.image.setRgbPixel(x, y, bmp.image.getRgbPixel(x - 1, y));
                }
            }
        }

        try {
            BmpWriter.write_bmp(outDownsampling, bmp);
        } finally

        {
            outDownsampling.close();
        }


        // read original bitmap again
        in = new FileInputStream(inFilename);
        bmp = BmpReader.read_bmp(in);

        // downsampling vertical
        for (int y = 0; y < bmp.image.getHeight(); y++) {
            for (int x = 0; x < bmp.image.getWidth(); x++) {
                if (y % 2 == 1) {
                    bmp.image.setRgbPixel(x, y, bmp.image.getRgbPixel(x, y - 1));
                }
            }
        }

        try {
            BmpWriter.write_bmp(outDownsamplingVertical, bmp);
        } finally

        {
            outDownsamplingVertical.close();
        }

        in = new FileInputStream(inFilename);
        bmp = BmpReader.read_bmp(in);

        // bitreduzierung
        int reduced_bits = 6;
        for (int y = 0; y < bmp.image.getHeight(); y++) {
            for (int x = 0; x < bmp.image.getWidth(); x++) {
                PixelColor p = bmp.image.getRgbPixel(x, y);
                int r = p.r;
                int g = p.g;
                int b = p.b;

                int pow = (int) Math.pow(2, reduced_bits);
                r /= pow;
                g /= pow;
                b /= pow;
                r *= pow;
                g *= pow;
                b *= pow;
                PixelColor pNew = new PixelColor(b, g, r);
                bmp.image.setRgbPixel(x, y, pNew);

            }
        }
        try {
            BmpWriter.write_bmp(outBitreduced, bmp);
        } finally

        {
            outBitreduced.close();
        }


        // bitreduzierung differenz
        reduced_bits = 1;
        int bitsPerColor = 8;
        in = new FileInputStream(inFilename);
        BmpImage bmpOriginal = BmpReader.read_bmp(in);
        BmpImage bmpDiff = (BmpImage) bmpOriginal;

        for (int y = 0; y < bmpOriginal.image.getHeight(); y++) {
            for (int x = 0; x < bmpOriginal.image.getWidth(); x++) {
                PixelColor pOriginal = bmpOriginal.image.getRgbPixel(x, y);
                PixelColor bitreduced = bmp.image.getRgbPixel(x, y);
                int r = pOriginal.r - bitreduced.r;
                int g = pOriginal.g - bitreduced.g;
                int b = pOriginal.b - bitreduced.b;
                r = Math.abs(r);
                g = Math.abs(g);
                b = Math.abs(b);
                PixelColor diff = new PixelColor(b, g, r);
                bmpDiff.image.setRgbPixel(x, y, diff);
            }
        }
        try {
            BmpWriter.write_bmp(outDiff, bmpDiff);
        } finally

        {
            outDiff.close();
        }


        // Übung 4
        in = new FileInputStream(inFilename);
        BmpImage bmpRed = BmpReader.read_bmp(in);

        in = new FileInputStream(inFilename);
        BmpImage bmpBlue = BmpReader.read_bmp(in);

        in = new FileInputStream(inFilename);
        BmpImage bmpGreen = BmpReader.read_bmp(in);

        in = new FileInputStream(inFilename);
        BmpImage bmpY = BmpReader.read_bmp(in);

        in = new FileInputStream(inFilename);
        BmpImage bmpCb = BmpReader.read_bmp(in);

        in = new FileInputStream(inFilename);
        BmpImage bmpCr = BmpReader.read_bmp(in);

        OutputStream outRed = new FileOutputStream("red.bmp");
        for (int y = 0; y < bmpRed.image.getHeight(); y++) {
            for (int x = 0; x < bmpRed.image.getWidth(); x++) {
                PixelColor pOriginal = bmpRed.image.getRgbPixel(x, y);
                pOriginal.b = 0;
                pOriginal.g = 0;
                bmpRed.image.setRgbPixel(x, y, pOriginal);
            }
        }
        try {
            BmpWriter.write_bmp(outRed, bmpRed);
        } finally

        {
            outRed.close();
        }


        OutputStream outBlue = new FileOutputStream("blue.bmp");
        for (int y = 0; y < bmpBlue.image.getHeight(); y++) {
            for (int x = 0; x < bmpBlue.image.getWidth(); x++) {
                PixelColor pOriginal = bmpBlue.image.getRgbPixel(x, y);
                pOriginal.r = 0;
                pOriginal.g = 0;
                bmpBlue.image.setRgbPixel(x, y, pOriginal);
            }
        }
        try {
            BmpWriter.write_bmp(outBlue, bmpBlue);
        } finally

        {
            outBlue.close();
        }


        OutputStream outGreen = new FileOutputStream("green.bmp");
        for (int y = 0; y < bmpGreen.image.getHeight(); y++) {
            for (int x = 0; x < bmpGreen.image.getWidth(); x++) {
                PixelColor pOriginal = bmpGreen.image.getRgbPixel(x, y);
                pOriginal.r = 0;
                pOriginal.b = 0;
                bmpGreen.image.setRgbPixel(x, y, pOriginal);
            }
        }
        try {
            BmpWriter.write_bmp(outGreen, bmpGreen);
        } finally

        {
            outGreen.close();
        }


        OutputStream outY = new FileOutputStream("Y.bmp");
        OutputStream outCb = new FileOutputStream("Cb.bmp");
        OutputStream outCr = new FileOutputStream("Cr.bmp");

        in = new FileInputStream(inFilename);
        bmpOriginal = BmpReader.read_bmp(in);

        // YCbCr - Images
        for (int y = 0; y < bmpOriginal.image.getHeight(); y++) {
            for (int x = 0; x < bmpOriginal.image.getWidth(); x++) {
                PixelColor pOriginal = bmpOriginal.image.getRgbPixel(x, y);
                PixelColor yCbCr = rgbToYCbCr(pOriginal);
                PixelColor pY = new PixelColor(128, 128, yCbCr.r);
                PixelColor pCb = new PixelColor(0, yCbCr.g, 0);
                PixelColor pCr = new PixelColor(yCbCr.b, 0, 0);

                pY = ycbcrTorgb(pY);
                pCb = ycbcrTorgb(pCb);
                pCr = ycbcrTorgb(pCr);

                bmpY.image.setRgbPixel(x, y, pY);
                bmpCb.image.setRgbPixel(x, y, pCb);
                bmpCr.image.setRgbPixel(x, y, pCr);
            }
        }
        try {
            BmpWriter.write_bmp(outY, bmpY);
            BmpWriter.write_bmp(outCb, bmpCb);
            BmpWriter.write_bmp(outCr, bmpCr);
        } finally

        {
            outY.close();
            outCb.close();
            outCr.close();
        }

        // Aufgabe 2 Histogramm

        double avgHelligkeit = calcAndSaveHistogram(bmpY, "Y-Histogramm.txt");

        avgHelligkeit = avgHelligkeit / (bmpY.image.getHeight() * bmpY.image.getWidth());

        double stdabw = 0;
        for (int x = 0; x < bmpY.image.getWidth(); x++) {
            for (int y = 0; y < bmpY.image.getHeight(); y++) {
                int key = bmpY.image.getRgbPixel(x, y).r;
                stdabw += Math.pow(key - avgHelligkeit, 2);
            }
        }
        stdabw = Math.sqrt(stdabw / (bmpY.image.getHeight() * bmpY.image.getWidth()));


        System.out.println("Mittlere Helligkeit: " + avgHelligkeit);
        System.out.println("Standardabweichung: " + stdabw);



        // Aufgabe 3 b Helligkeit
        for (int i = -250; i < 256; i += 20) {
            in = new FileInputStream(inFilename);
            bmpOriginal = BmpReader.read_bmp(in);
            applyBrightnessToImage(i, bmpOriginal);
            OutputStream outBright = new FileOutputStream("brightness_" + i + "_.bmp");
            BmpWriter.write_bmp(outBright, bmpOriginal);
            outBright.close();
        }

        // histogram low brightness
        in = new FileInputStream(inFilename);
        bmpOriginal = BmpReader.read_bmp(in);
        applyBrightnessToImage(-170, bmpOriginal);
        calcAndSaveHistogram(bmpOriginal, "low_brightness.txt");

        // histogram high brightness
        in = new FileInputStream(inFilename);
        bmpOriginal = BmpReader.read_bmp(in);
        applyBrightnessToImage(70, bmpOriginal);
        calcAndSaveHistogram(bmpOriginal, "high_brightness.txt");



        // Aufgabe 3 c Kontrast
        writeAndCalcContrastImage(0.2, inFilename);

        // calc histogram for low contrast image
        in = new FileInputStream(inFilename);
        bmpOriginal = BmpReader.read_bmp(in);
        applyContrastToImage(0.2, bmpOriginal);
        calcAndSaveHistogram(bmpOriginal,"low_contrast.txt");

        writeAndCalcContrastImage(0.4, inFilename);
        writeAndCalcContrastImage(0.8, inFilename);
        writeAndCalcContrastImage(1.0, inFilename);
        writeAndCalcContrastImage(1.5, inFilename);
        writeAndCalcContrastImage(2.5, inFilename);
        writeAndCalcContrastImage(5.0, inFilename);
        writeAndCalcContrastImage(10.0, inFilename);

        // calc histogram for high contrast image
        in = new FileInputStream(inFilename);
        bmpOriginal = BmpReader.read_bmp(in);
        applyContrastToImage(10.0, bmpOriginal);
        calcAndSaveHistogram(bmpOriginal,"high_contrast.txt");

        writeAndCalcContrastImage(-1.0, inFilename);
    }

    public static void writeAndCalcContrastImage(double c, String filename) throws IOException {
        InputStream in = new FileInputStream(filename);
        BmpImage bmpOriginal = BmpReader.read_bmp(in);
        applyContrastToImage(c, bmpOriginal);
        OutputStream outBright = new FileOutputStream("contrast_" + c + "_.bmp");
        BmpWriter.write_bmp(outBright, bmpOriginal);
        outBright.close();
    }

    public static void applyContrastToImage(double c, BmpImage img) {
        for (int i = 0; i < img.image.width; i++) {
            for (int j = 0; j < img.image.getHeight(); j++)
                adjustContrast(img.image.getRgbPixel(i, j), c);
        }
    }

    public static void applyBrightnessToImage(int bOffset, BmpImage img) {
        for (int i = 0; i < img.image.width; i++) {
            for (int j = 0; j < img.image.getHeight(); j++)
                adjustBrightness(img.image.getRgbPixel(i, j), bOffset);
        }
    }

    public static void setInRange(PixelColor p) {
        if (p.r < 0)
            p.r = 0;
        else if (p.r > 255)
            p.r = 255;

        if (p.g < 0)
            p.g = 0;
        else if (p.g > 255)
            p.g = 255;

        if (p.b < 0)
            p.b = 0;
        else if (p.b > 255)
            p.b = 255;
    }

    public static double calcAndSaveHistogram(BmpImage img, String filename) throws FileNotFoundException, UnsupportedEncodingException {
        HashMap<Integer, Integer> verteilung = new HashMap<>();
        double avgHelligkeit = 0;
        // initialize hasmap
        for (int i = 0; i < 256; i++) {
            verteilung.put(i, 0);
        }
        for (int x = 0; x < img.image.getWidth(); x++) {
            for (int y = 0; y < img.image.getHeight(); y++) {
                int key = img.image.getRgbPixel(x, y).r;
                avgHelligkeit += key;
                if (!verteilung.containsKey(key)) {
                    verteilung.put(key, 1);
                } else {
                    int count = verteilung.get(key);
                    count++;
                    verteilung.replace(key, count);
                }
            }
        }
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        // BGR schreiben vertikal 2.1.
        for (HashMap.Entry p : verteilung.entrySet()) {
            writer.println(p.getKey() + "," + p.getValue());
        }
        writer.close();
        return avgHelligkeit;
    }

    public static void adjustContrast(PixelColor p, double c) {
        p.r = (int) (Math.round(c * (p.r - 128) + 128));
        p.g = (int) (Math.round(c * (p.g - 128) + 128));
        p.b = (int) (Math.round(c * (p.b - 128) + 128));
        setInRange(p);
    }


    public static void adjustBrightness(PixelColor p, int bOffset) {
        p.r += bOffset;
        p.g += bOffset;
        p.b += bOffset;
        setInRange(p);
    }

    public static PixelColor rgbToYCbCr(PixelColor p) {
        return new PixelColor(rgbToCr(p), rgbToCb(p), rgbToY(p));
    }

    public static int rgbToY(PixelColor p) {
        return (int) (0.299 * p.r + 0.587 * p.g + 0.114 * p.b);
    }

    public static int rgbToCb(PixelColor p) {
        return (int) (-0.169 * p.r - 0.331 * p.g + 0.5 * p.b) + 128;
    }

    public static int rgbToCr(PixelColor p) {
        return (int) (0.5 * p.r - 0.419 * p.g - 0.081 * p.b) + 128;
    }

    public static PixelColor ycbcrTorgb(PixelColor p) {
        PixelColor rgb = new PixelColor(0, 0, 0);
        int r = (int) (p.r + 1.403 * (p.b - 128));
        int g = (int) (p.r - 0.433 * (p.g - 128) - 0.714 * (p.b - 128));
        int b = (int) (p.r + 1.733 * (p.g - 128));
        rgb.r = r;
        rgb.b = b;
        rgb.g = g;
        return rgb;
    }
}
