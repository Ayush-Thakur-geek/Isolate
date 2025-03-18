package com.soundisolation.isolate.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

@Service
public class FileServiceImplement implements FileService {
    @Override
    public void uploadFile(MultipartFile file) {
        System.out.println("File uploaded");
        try {
            byte[] data = file.getBytes();
            Double[] audioSamples = extractAudioSamples(data);
            int len = audioSamples.length;
            double[] fftData = new double[2 * len];

            for (int i = 0; i < len; i++) {
                fftData[2 * i] = audioSamples[i];   // Real part
                fftData[2 * i + 1] = 0.0;          // Imaginary part (set to 0)
            }

            // Create FFT instance
            DoubleFFT_1D fft = new DoubleFFT_1D(len);

            // Perform FFT in-place
            fft.complexForward(fftData);

            // Print FFT results
            System.out.println("FFT Results:");
            for (int i = 0; i < len; i++) {
                double real = fftData[2 * i];
                double imag = fftData[2 * i + 1];
                System.out.printf("Frequency %d: Real = %.5f, Imag = %.5f%n", i, real, imag);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Double[] extractAudioSamples(byte[] data) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        // Extract audio samples from the data
        AudioInputStream audioSystem = AudioSystem.getAudioInputStream(new ByteArrayInputStream(data));

//        Clip clip = AudioSystem.getClip();
//        clip.open(audioSystem);
//        clip.start();

        AudioFormat audioFormat = audioSystem.getFormat();
        System.out.println(audioFormat);
        int sampleSize = audioFormat.getSampleSizeInBits();
        System.out.println(sampleSize);
        boolean isBigEndian = audioFormat.isBigEndian();
        System.out.println(isBigEndian);

        // Extract audio samples from the data
        byte[] rawBytes = new byte[data.length];
        int len = audioSystem.read(rawBytes);
        System.out.println(len);

        int numSamples = rawBytes.length / (sampleSize / 8);
        Double[] samples = new Double[numSamples];

        for (int i = 0, sampleIndex = 0; i < rawBytes.length - 1; i += sampleSize/8, sampleIndex++) {
            int sample;
            if (isBigEndian) {
                sample = (rawBytes[i] << 8) | (rawBytes[i + 1] & 0xFF);
            } else {
                sample = (rawBytes[i + 1] << 8) | (rawBytes[i] & 0xFF);
            }
            // Normalize based on sample size
            double maxAmplitude = Math.pow(2, sampleSize - 1);
            samples[sampleIndex] = sample/maxAmplitude;
        }

        return samples;
    }
}
