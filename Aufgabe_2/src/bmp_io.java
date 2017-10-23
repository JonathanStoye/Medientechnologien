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

        in =new FileInputStream(inFilename);
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
        in =new FileInputStream(inFilename);
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

        in =new FileInputStream(inFilename);
        bmp = BmpReader.read_bmp(in);

        // bitreduzierung
        int reduced_bits = 7;
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
                PixelColor pNew = new PixelColor(b,g,r);
                bmp.image.setRgbPixel(x,y, pNew);

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
        in =new FileInputStream(inFilename);
        BmpImage bmpOriginal = BmpReader.read_bmp(in);
        BmpImage bmpDiff = (BmpImage)bmpOriginal;

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

        try {
            BmpWriter.write_bmp(out, bmp);
        } finally

        {
            out.close();
        }
    }
}
