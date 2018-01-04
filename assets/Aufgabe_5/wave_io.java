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

        String filenameMusic = null;
        String filenameVoice = null;
        String filenameSine = null;
        String filenameWhiteNoise = null;
        String outFilename = null;

        if (args.length < 1) {
            try {
                throw new WavFileException("At least one filename specified  (" + args.length + ")");
            } catch (WavFileException e1) {
                e1.printStackTrace();
            }
        }

        filenameMusic = args[0];
        filenameVoice = args[1];
        filenameSine = args[2];
        filenameWhiteNoise = args[3];
        //read wave data, sample contained in array readWavFile.sound

        try {

            //*******************
            //*** Amplification *
            //*******************

            // amplify by 3 dB
            WavFile amplified = WavFile.read_wav(filenameMusic);
            amplify(amplified, 3);
            WavFile.write_wav("amplify_3db.wav", amplified.getNumChannels(), amplified.getNumFrames(), amplified.getValidBits(), amplified.getSampleRate(), amplified.sound);

            // amplify by 6 dB
            amplified = WavFile.read_wav(filenameMusic);
            amplify(amplified, 6);
            WavFile.write_wav("amplify_6db.wav", amplified.getNumChannels(), amplified.getNumFrames(), amplified.getValidBits(), amplified.getSampleRate(), amplified.sound);

            // amplify by 9 dB
            amplified = WavFile.read_wav(filenameMusic);
            amplify(amplified, 9);
            WavFile.write_wav("amplify_9db.wav", amplified.getNumChannels(), amplified.getNumFrames(), amplified.getValidBits(), amplified.getSampleRate(), amplified.sound);


            // sine amplification
            amplified = WavFile.read_wav(filenameSine);
            amplify(amplified, 6);
            WavFile.write_wav("amplify_Sine_6db.wav", amplified.getNumChannels(), amplified.getNumFrames(), amplified.getValidBits(), amplified.getSampleRate(), amplified.sound);

            amplified = WavFile.read_wav(filenameSine);
            amplify(amplified, 9);
            WavFile.write_wav("amplify_Sine_9db.wav", amplified.getNumChannels(), amplified.getNumFrames(), amplified.getValidBits(), amplified.getSampleRate(), amplified.sound);

            amplified = WavFile.read_wav(filenameSine);
            amplify(amplified, 12);
            WavFile.write_wav("amplify_Sine_12db.wav", amplified.getNumChannels(), amplified.getNumFrames(), amplified.getValidBits(), amplified.getSampleRate(), amplified.sound);

            //*******************
            //*** Echo **********
            //*******************

            WavFile echoMusic = WavFile.read_wav(filenameMusic);
            echo(echoMusic, 10);
            WavFile.write_wav("echo_Music_10ms.wav", echoMusic.getNumChannels(), echoMusic.getNumFrames(), echoMusic.getValidBits(), echoMusic.getSampleRate(), echoMusic.sound);

            WavFile echoVoice = WavFile.read_wav(filenameVoice);
            echo(echoVoice, 10);
            WavFile.write_wav("echo_Voice_10ms.wav", echoVoice.getNumChannels(), echoVoice.getNumFrames(), echoVoice.getValidBits(), echoVoice.getSampleRate(), echoVoice.sound);

            echoMusic = WavFile.read_wav(filenameMusic);
            echo(echoMusic, 100);
            WavFile.write_wav("echo_Music_100ms.wav", echoMusic.getNumChannels(), echoMusic.getNumFrames(), echoMusic.getValidBits(), echoMusic.getSampleRate(), echoMusic.sound);

            echoVoice = WavFile.read_wav(filenameVoice);
            echo(echoVoice, 100);
            WavFile.write_wav("echo_Voice_100ms.wav", echoVoice.getNumChannels(), echoVoice.getNumFrames(), echoVoice.getValidBits(), echoVoice.getSampleRate(), echoVoice.sound);

            echoMusic = WavFile.read_wav(filenameMusic);
            echo(echoMusic, 200);
            WavFile.write_wav("echo_Music_200ms.wav", echoMusic.getNumChannels(), echoMusic.getNumFrames(), echoMusic.getValidBits(), echoMusic.getSampleRate(), echoMusic.sound);

            echoVoice = WavFile.read_wav(filenameVoice);
            echo(echoVoice, 200);
            WavFile.write_wav("echo_Voice_200ms.wav", echoVoice.getNumChannels(), echoVoice.getNumFrames(), echoVoice.getValidBits(), echoVoice.getSampleRate(), echoVoice.sound);


            //*******************
            //*** Filter ********
            //*******************


            WavFile filter = WavFile.read_wav(filenameMusic);
            filterOne(filter);
            WavFile.write_wav("filter_Music_1.wav", filter.getNumChannels(), filter.getNumFrames(), filter.getValidBits(), filter.getSampleRate(), filter.sound);

            filter = WavFile.read_wav(filenameMusic);
            filterTwo(filter);
            WavFile.write_wav("filter_Music_2.wav", filter.getNumChannels(), filter.getNumFrames(), filter.getValidBits(), filter.getSampleRate(), filter.sound);

            filter = WavFile.read_wav(filenameWhiteNoise);
            filterOne(filter);
            WavFile.write_wav("filter_WhiteNoise_1.wav", filter.getNumChannels(), filter.getNumFrames(), filter.getValidBits(), filter.getSampleRate(), filter.sound);

            filter = WavFile.read_wav(filenameWhiteNoise);
            filterTwo(filter);
            WavFile.write_wav("filter_WhiteNoise_2.wav", filter.getNumChannels(), filter.getNumFrames(), filter.getValidBits(), filter.getSampleRate(), filter.sound);


        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static short range(int val) {
        if (val > Short.MAX_VALUE) {
            val = Short.MAX_VALUE;
        } else if (val < Short.MIN_VALUE) {
            val = Short.MIN_VALUE;
        }
        return (short) val;
    }

    public static void amplify(WavFile wav, double db) {
        double ampFac = Math.pow(10, db / 20d);
        for (int i = 0; i < wav.sound.length; i++) {
            int val = (int) Math.round(wav.sound[i] * ampFac);

            wav.sound[i] = range(val);
        }
    }

    public static void echo(WavFile wav, int tDelta) {
        long sampleOffset = Math.round(wav.getSampleRate() * wav.getNumChannels() * (tDelta / 1000d));
        for (int i = 0; i < wav.sound.length; i += wav.getNumChannels()) {
            if (i < tDelta) {
                wav.sound[i] = wav.sound[i];
            } else {
                // iterate over channels
                for (int j = 0; j < wav.getNumChannels(); j++) {
                    int echoIndex = i + j - (int) sampleOffset;
                    if (echoIndex < 0 || i+j >= wav.sound.length) {
                        wav.sound[i] = wav.sound[i];
                    } else {
                        wav.sound[i + j] = range((int) (0.6 * wav.sound[echoIndex] + wav.sound[i + j]));
                    }
                }
            }
        }
    }

    public static void filterOne(WavFile wav) {
        for (int i = 0; i < wav.sound.length; i++) {
            if (i == 0)
                wav.sound[i] = wav.sound[i];
            else
                wav.sound[i] = range((int)(0.5 * wav.sound[i] + 0.45 * wav.sound[i - 1]));
        }
    }

    public static void filterTwo(WavFile wav) {
        for (int i = 0; i < wav.sound.length; i++) {
            if (i == 0)
                wav.sound[i] = wav.sound[i];
            else
                wav.sound[i] = range((int)(0.5 * wav.sound[i] - 0.45 * wav.sound[i - 1]));
        }
    }

}
