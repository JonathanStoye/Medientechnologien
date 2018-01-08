import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public final class bmp_io_ue6 {

    public static void main(String[] args) throws IOException {
        String inFilename = null;
        String inFilename2 = null;

        PixelColor pc = null;
        PixelColor pc_f = null;

        BmpImage original = null;
        BmpImage tiefpassbmp = null;

        String outFilename = null;
        OutputStream out = null;


        inFilename = args[0];
        InputStream in = new FileInputStream(inFilename);
        original = BmpReader.read_bmp(in);

        in = new FileInputStream(inFilename);
        tiefpassbmp = BmpReader.read_bmp(in);

        outFilename = "tiefpass.bmp";
        out = new FileOutputStream(outFilename);

        int[][] tiefpass = new int[][]{
                {1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        };

        apply3x3Kernel(original, tiefpassbmp, tiefpass);
        // save tiefpass
        try {
            BmpWriter.write_bmp(out, tiefpassbmp);
        } finally {
            out.close();
        }

        // calc difference
        in = new FileInputStream(inFilename);
        BmpImage difference = BmpReader.read_bmp(in);
        difference(original, tiefpassbmp, difference);


        // save difference
        outFilename = "tiefpassDiff.bmp";
        out = new FileOutputStream(outFilename);
        try {
            BmpWriter.write_bmp(out, difference);
        } finally {
            out.close();
        }

        int[][] gradientFilter = new int[][]{
                {0, -2, 0},
                {-2, 12, -2},
                {0, -2, 0}
        };
        in = new FileInputStream(inFilename);
        BmpImage gradient = BmpReader.read_bmp(in);
        in = new FileInputStream(inFilename);
        BmpImage gradientDiff = BmpReader.read_bmp(in);
        apply3x3Kernel(original, gradient, gradientFilter);
        difference(original, gradient, gradientDiff);

        outFilename = "gradientfilter.bmp";
        out = new FileOutputStream(outFilename);
        try {
            BmpWriter.write_bmp(out, gradient);
        } finally {
            out.close();
        }

        outFilename = "gradientDifference.bmp";
        out = new FileOutputStream(outFilename);
        try {
            BmpWriter.write_bmp(out, gradientDiff);
        } finally {
            out.close();
        }


        in = new FileInputStream("y_scratched.bmp");
        BmpImage median = BmpReader.read_bmp(in);
        applyMedianFilter(original, median);

        outFilename = "median.bmp";
        out = new FileOutputStream(outFilename);
        try {
            BmpWriter.write_bmp(out, median);
        } finally {
            out.close();
        }

        int[][] sobelY = new int[][]{
                {1, 2, 1},
                {0, 0, 0},
                {-1, -2, -1}
        };

        int[][] sobelX = new int[][]{
                {1, 0, -1},
                {2, 0, -2},
                {1, 0, -1}
        };

        in = new FileInputStream(inFilename);
        BmpImage bmp_sobelX = BmpReader.read_bmp(in);
        apply3x3Kernel(original, bmp_sobelX, sobelX);

        in = new FileInputStream(inFilename);
        BmpImage bmp_sobelY = BmpReader.read_bmp(in);
        apply3x3Kernel(original, bmp_sobelY, sobelY);

        outFilename = "SobelX.bmp";
        out = new FileOutputStream(outFilename);
        try {
            BmpWriter.write_bmp(out, bmp_sobelX);
        } finally {
            out.close();
        }

        outFilename = "SobelY.bmp";
        out = new FileOutputStream(outFilename);
        try {
            BmpWriter.write_bmp(out, bmp_sobelY);
        } finally {
            out.close();
        }
    }


    public static int arraySum(int[][] arr) {
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length; j++) {
                sum += arr[i][j];
            }
        }
        return sum;
    }

    public static void apply3x3Kernel(BmpImage original, BmpImage target, int[][] kernel) {
        if (!(kernel.length == 3 && kernel[0].length == 3)) {
            throw new IllegalArgumentException("illegal kernel-size");
        }
        int sum = arraySum(kernel);

        for (int x = 0; x < original.image.width; x++) {
            for (int y = 0; y < original.image.height; y++) {
                int val = 0;
                for (int off_x = -1; off_x <= 1; off_x++) {
                    for (int off_y = -1; off_y <= 1; off_y++) {
                        // edge cases
                        if (x + off_x < 0) {
                            val += original.image.getRgbPixel(x, y).r * kernel[off_x + 1][off_y + 1];
                            continue;
                        }
                        if (x + off_x >= original.image.width) {
                            val += original.image.getRgbPixel(x, y).r * kernel[off_x + 1][off_y + 1];
                            continue;
                        }
                        if (y + off_y < 0) {
                            val += original.image.getRgbPixel(x, y).r * kernel[off_x + 1][off_y + 1];
                            continue;
                        }
                        if (y + off_y >= original.image.height) {
                            val += original.image.getRgbPixel(x, y).r * kernel[off_x + 1][off_y + 1];
                            continue;
                        }
                        val += original.image.getRgbPixel(x + off_x, y + off_y).r * kernel[off_x + 1][off_y + 1];
                    }
                }
                if (sum != 0) {
                    val /= sum;
                }

                if (val > 255) {
                    //System.out.println("Pixel X:" + x + " Y:" + y + "größer 255: " + val);
                    val = 255;

                }
                if (val < 0) {
                    val = 0;
                    //System.out.println("Pixel X:" + x + " Y:" + y + "kleiner 0");
                }
                PixelColor pc = new PixelColor(val, val, val);
                target.image.setRgbPixel(x, y, pc);
            }
        }
    }


    public static void applyMedianFilter(BmpImage original, BmpImage target) {
        for (int x = 0; x < original.image.width; x++) {
            for (int y = 0; y < original.image.height; y++) {
                int val = 0;
                ArrayList<Integer> values = new ArrayList<>();
                for (int off_x = -1; off_x <= 1; off_x++) {
                    for (int off_y = -1; off_y <= 1; off_y++) {
                        // edge cases
                        if (x + off_x < 0) {
                            continue;
                        }
                        if (x + off_x >= original.image.width) {
                            continue;
                        }
                        if (y + off_y < 0) {
                            continue;
                        }
                        if (y + off_y >= original.image.height) {
                            continue;
                        }

                        values.add(original.image.getRgbPixel(x + off_x, y + off_y).r);

                    }
                }
                Collections.sort(values);
                val = values.get(values.size() / 2);

                PixelColor pc = new PixelColor(val, val, val);
                target.image.setRgbPixel(x, y, pc);
            }
        }
    }

    public static void difference(BmpImage original_1, BmpImage original_2, BmpImage target) {
        for (int i = 0; i < original_1.image.width; i++) {
            for (int j = 0; j < original_1.image.height; j++) {
                int val = Math.abs(original_1.image.getRgbPixel(i, j).r - original_2.image.getRgbPixel(i, j).r);
                target.image.setRgbPixel(i, j, new PixelColor(val, val, val));
            }
        }

    }


}