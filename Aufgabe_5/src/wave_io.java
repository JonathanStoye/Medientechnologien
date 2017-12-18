import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class wave_io {
    public static void main(String[] args) {
        int samples = 0;
        int validBits = 0;
        long sampleRate = 0;
        long numFrames = 0;
        int numChannels = 0;

        String inFilename = null;
        String outFilename = null;

        if (args.length < 1) {
            try {
                throw new WavFileException("At least one filename specified  (" + args.length + ")");
            } catch (WavFileException e1) {
                e1.printStackTrace();
            }
        }

        inFilename = args[0];

        //read wave data, sample contained in array readWavFile.sound
        WavFile readWavFile = null;
        try {
            readWavFile = WavFile.read_wav(inFilename);

            //local copy of header data
            numFrames = readWavFile.getNumFrames();
            numChannels = readWavFile.getNumChannels();
            samples = (int) numFrames * numChannels;
            validBits = readWavFile.getValidBits();
            sampleRate = readWavFile.getSampleRate();


            // Write wav as readable ASCII-File
            List<String> lines;
            lines = new ArrayList<String>();
            // samples schreiben 2.1.
            for (int i = 0; i < samples - 1; i++) {
                lines.add(String.valueOf(readWavFile.sound[i]));
            }


            Path file = Paths.get("Wav_as_ASCII.txt");
            Files.write(file, lines, Charset.forName("UTF-8"));

            if (args.length == 1)
                System.exit(0);

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (WavFileException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        outFilename = args[1];
        try {


            int downsampledSampleCount = samples / 2;
            short downsampled[] = new short[downsampledSampleCount];

            // 2.4 Downsampling
            for (int i = 0; i < (samples / 2); i++) {
                // ********* ToDo ***************
                downsampled[i] = readWavFile.sound[i * 2];
            }

            // Header angepasst?
            WavFile.write_wav("downsampled.wav", numChannels, numFrames / 2, validBits, sampleRate / 2, downsampled);

            short reducedSamples[] = new short[samples];
            // 3.2 Bitreduzierung
            int reduced_bits = 14;
            for (int i = 0; i < samples; i++) {
                short reducedSample = (short) (readWavFile.sound[i] / (int) (Math.pow(2, reduced_bits)));
                reducedSamples[i] = (short) (reducedSample * (int) (Math.pow(2, reduced_bits)));
            }


            WavFile.write_wav("bitreduced.wav", numChannels, numFrames, validBits, sampleRate, reducedSamples);

            short diff[] = new short[samples];
            // 3.4 Bitreduzierung
            reduced_bits = 1;
            for (int i = 0; i < samples; i++) {
                diff[i] = (short) (Math.abs(reducedSamples[i]) - Math.abs(readWavFile.sound[i]));
            }

            WavFile.write_wav("diff.wav", numChannels, numFrames, validBits, sampleRate, diff);
            WavFile.write_wav(outFilename, numChannels, numFrames, validBits, sampleRate, readWavFile.sound);

        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    public static void amplify(WavFile wav, double db) {
        double ampFac = Math.pow(10, db / 20d);
        for (int i = 0; i < wav.sound.length; i++) {
            wav.sound[i] = (short) Math.round(wav.sound[i] * ampFac);
        }
    }

    public static void echo(WavFile wav, int tDelta) {
        long sampleOffset = wav.getSampleRate() * wav.getNumChannels() * (tDelta / 1000);
        for (int i = 0; i < wav.sound.length; i += wav.getNumChannels()) {
            if (i < tDelta) {
                wav.sound[i] = wav.sound[i];
            } else {
                // iterate over channels
                for (int j = 0; j < wav.getNumChannels(); j++) {
                    int echoIndex = i + j - (int) sampleOffset;
                    wav.sound[i + j] = (short) (0.6 * wav.sound[echoIndex] + wav.sound[i + j]);
                }
            }
        }
    }

    public static void filterOne(WavFile wav) {
        for (int i = 0; i < wav.sound.length; i++) {
            if (i == 0)
                wav.sound[i] = wav.sound[i];
            else
                wav.sound[i] = (short) (0.5 * wav.sound[i] + 0.45 * wav.sound[i - 1]);
        }
    }

    public static void filterTwo(WavFile wav) {
        for (int i = 0; i < wav.sound.length; i++) {
            if (i == 0)
                wav.sound[i] = wav.sound[i];
            else
                wav.sound[i] = (short) (0.5 * wav.sound[i] - 0.45 * wav.sound[i - 1]);
        }
    }

}
