import java.io.*;

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
        PrintWriter writer = new PrintWriter(outFilename, "UTF-8");


        // BGR schreiben horizontal 2.1.
        for (int x = 0; x < bmp.image.getWidth(); x++) {

            writer.println(bmp.image.getRgbPixel(x, 0).b + " " + bmp.image.getRgbPixel(x, 0).g + " " + bmp.image.getRgbPixel(x, 0).r);
            // ********* ToDo ***************
        }

        // BGR schreiben vertikal 2.1.
        for (int y = 0; y < bmp.image.getHeight(); y++) {

            // ********* ToDo ***************
            writer.println(bmp.image.getRgbPixel(0, y).b + " " + bmp.image.getRgbPixel(0, y).g + " " + bmp.image.getRgbPixel(0, y).r);

        }

        writer.close();

        if (args.length == 1)
            System.exit(0);

        outFilename = args[1];
        OutputStream out = new FileOutputStream(outFilename);
        OutputStream outGrey = new FileOutputStream("graustufenbild.bmp");
        OutputStream outDownsampling = new FileOutputStream("downsampled.bmp");
        OutputStream outBitreduced = new FileOutputStream("bitreduced.bmp");
        OutputStream outDiff = new FileOutputStream("difference.bmp");

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
            out.close();
        }


        // downsampling
        for (int y = 0; y < bmp.image.getHeight(); y++) {
            for (int x = 0; x < bmp.image.getWidth(); x++) {
                // ********* ToDo ***************
                if (x % 2 == 1) {
                    bmp.image.setRgbPixel(x, y, bmp.image.getRgbPixel(x - 1, y));
                }
            }
        }

        try {
            BmpWriter.write_bmp(outDownsampling, bmp);
        } finally

        {
            out.close();
        }

        // bitreduzierung
        int reduced_bits = 1;
        for (int y = 0; y < bmp.image.getHeight(); y++) {
            for (int x = 0; x < bmp.image.getWidth(); x++) {

                // ********* ToDo ***************
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
            }
        }
        try {
            BmpWriter.write_bmp(outBitreduced, bmp);
        } finally

        {
            out.close();
        }


        // bitreduzierung differenz
        reduced_bits = 1;
        int bitsPerColor = 8;
        BmpImage bmpOriginal = BmpReader.read_bmp(in);
        BmpImage bmpDiff = new BmpImage();

        for (int y = 0; y < bmp.image.getHeight(); y++) {
            for (int x = 0; x < bmp.image.getWidth(); x++) {
                // ********* ToDo ***************
                PixelColor pOriginal = bmpOriginal.image.getRgbPixel(x, y);
                PixelColor bitreduced = bmp.image.getRgbPixel(x, y);
                int r = pOriginal.r - bitreduced.r;
                int g = pOriginal.g - bitreduced.g;
                int b = pOriginal.b - bitreduced.b;
                if (r < 0) {
                    r += 127;
                }
                if (g < 0) {
                    g += 127;
                }
                if (b < 0) {
                    b += 127;
                }
                PixelColor diff = new PixelColor(b, g, r);
                bmpDiff.image.setRgbPixel(x, y, diff);
            }
        }
        try {
            BmpWriter.write_bmp(outDiff, bmpDiff);
        } finally

        {
            out.close();
        }

        try {
            BmpWriter.write_bmp(out, bmp);
        } finally

        {
            out.close();
        }
    }
}
