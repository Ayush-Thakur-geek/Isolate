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
        return null;
    }
}
