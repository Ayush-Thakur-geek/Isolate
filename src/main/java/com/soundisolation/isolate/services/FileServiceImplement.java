package com.soundisolation.isolate.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class FileServiceImplement implements FileService {
    @Override
    public void uploadFile(MultipartFile file) {
        System.out.println("File uploaded");
        try {
            byte[] data = file.getBytes();
            double[] audioSamples = extractAudioSamples(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private double[] extractAudioSamples(byte[] data) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        // Extract audio samples from the data
        AudioInputStream audioSystem = AudioSystem.getAudioInputStream(new ByteArrayInputStream(data));

        Clip clip = AudioSystem.getClip();
        clip.open(audioSystem);
        clip.start();

        AudioFormat audioFormat = audioSystem.getFormat();
        int sampleSize = audioFormat.getSampleSizeInBits();
        boolean isBigEndian = audioFormat.isBigEndian();

        // Extract audio samples from the data
        byte[] rawBytes = audioSystem.readAllBytes();
        int numSamples = rawBytes.length / (sampleSize / 8);
        double[] samples = new double[numSamples];

        for (int i = 0, sampleIndex = 0; i < rawBytes.length - 1; i += sampleSize/8, sampleIndex++) {
            int sample;
            if (isBigEndian) {
                sample = (rawBytes[i] << 8) | (rawBytes[i + 1] & 0xFF);
            } else {
                sample = (rawBytes[i + 1] << 8) | (rawBytes[i] & 0xFF);
            }
            // Normalize based on sample size
            double maxAmplitude = Math.pow(2, sampleSize - 1);
            samples[sampleIndex] = sample / maxAmplitude;
        }

        return samples;
    }
}
