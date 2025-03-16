package com.soundisolation.isolate.services;

import com.soundisolation.isolate.model.Complex;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

@Service
public class FileServiceImplement implements FileService {
    @Override
    public void uploadFile(MultipartFile file) {
        System.out.println("File uploaded");
        try {
            byte[] data = file.getBytes();
            Complex[] audioSamples = extractAudioSamples(data);
//            System.out.println(Arrays.toString(audioSamples));
            Complex[] result = fft(audioSamples);

//            for (Complex complex : result) {
//                complex.toString();
//            }

            Complex[] iResult = ifft(result);

            for (Complex complex : iResult) {
                complex.toString();
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Complex[] extractAudioSamples(byte[] data) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
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
        Complex[] samples = new Complex[numSamples];

        for (int i = 0, sampleIndex = 0; i < rawBytes.length - 1; i += sampleSize/8, sampleIndex++) {
            int sample;
            if (isBigEndian) {
                sample = (rawBytes[i] << 8) | (rawBytes[i + 1] & 0xFF);
            } else {
                sample = (rawBytes[i + 1] << 8) | (rawBytes[i] & 0xFF);
            }
            // Normalize based on sample size
            double maxAmplitude = Math.pow(2, sampleSize - 1);
            samples[sampleIndex] = new Complex(sample / maxAmplitude, 0);
        }

        return samples;
    }

    public static Complex[] fft(Complex[] x) {
        int n = x.length;

        // Base case
        if (n == 1) return new Complex[] { x[0] };

        // Ensure n is a power of 2 (zero-pad instead of truncating)
        if (n % 2 != 0) {
            Complex[] padded = new Complex[n + 1];
            System.arraycopy(x, 0, padded, 0, n);
            padded[n] = new Complex(0, 0);
            x = padded;
            n = x.length;
        }

        // Compute FFT of even terms
        Complex[] even = new Complex[n/2];
        Complex[] odd = new Complex[n/2]; // ✅ Fix: New array instead of reusing 'even'
        for (int k = 0; k < n/2; k++) {
            even[k] = x[2*k] != null ? x[2*k] : new Complex(0, 0);
            odd[k] = x[2*k + 1] != null ? x[2*k + 1] : new Complex(0, 0);
        }

        Complex[] evenFFT = fft(even);
        Complex[] oddFFT = fft(odd);

        // 🔥 **Debug print before using evenFFT and oddFFT**
        System.out.println("FFT Computed for n = " + n);
        System.out.println("evenFFT: " + Arrays.toString(evenFFT));
        System.out.println("oddFFT: " + Arrays.toString(oddFFT));

        // Combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n/2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));

//            System.out.println("index: " + k);
//            if (evenFFT[k] == null || oddFFT[k] == null) {
//                System.out.println("evenFFT[" + k + "] or oddFFT[" + k + "] is NULL!");
//            } else {
//                System.out.println("evenFFT[" + k + "]: " + evenFFT[k].toString());
//                System.out.println("oddFFT[" + k + "]: " + oddFFT[k].toString());
//            }

            y[k] = evenFFT[k].plus(wk.times(oddFFT[k]));
            y[k + n/2] = evenFFT[k].minus(wk.times(oddFFT[k]));
        }
        return y;
    }



    // compute the inverse FFT of x[], assuming its length n is a power of 2
    public static Complex[] ifft(Complex[] x) {
        int n = x.length;
        Complex[] y = new Complex[n];

        // take conjugate
        for (int i = 0; i < n; i++) {
            y[i] = x[i].conjugate();
        }

        // compute forward FFT
        y = fft(y);

        // take conjugate again
        for (int i = 0; i < n; i++) {
            y[i] = y[i].conjugate();
        }

        // divide by n
        for (int i = 0; i < n; i++) {
            y[i] = y[i].scale(1.0 / n);
        }

        return y;

    }

    // compute the circular convolution of x and y
    public static Complex[] cconvolve(Complex[] x, Complex[] y) {

        // should probably pad x and y with 0s so that they have same length
        // and are powers of 2
        if (x.length != y.length) {
            throw new IllegalArgumentException("Dimensions don't agree");
        }

        int n = x.length;

        // compute FFT of each sequence
        Complex[] a = fft(x);
        Complex[] b = fft(y);

        // point-wise multiply
        Complex[] c = new Complex[n];
        for (int i = 0; i < n; i++) {
            c[i] = a[i].times(b[i]);
        }

        // compute inverse FFT
        return ifft(c);
    }


    // compute the linear convolution of x and y
    public static Complex[] convolve(Complex[] x, Complex[] y) {
        Complex ZERO = new Complex(0, 0);

        Complex[] a = new Complex[2*x.length];
        for (int i = 0;        i <   x.length; i++) a[i] = x[i];
        for (int i = x.length; i < 2*x.length; i++) a[i] = ZERO;

        Complex[] b = new Complex[2*y.length];
        for (int i = 0;        i <   y.length; i++) b[i] = y[i];
        for (int i = y.length; i < 2*y.length; i++) b[i] = ZERO;

        return cconvolve(a, b);
    }

    // compute the DFT of x[] via brute force (n^2 time)
    public static Complex[] dft(Complex[] x) {
        int n = x.length;
        Complex ZERO = new Complex(0, 0);
        Complex[] y = new Complex[n];
        for (int k = 0; k < n; k++) {
            y[k] = ZERO;
            for (int j = 0; j < n; j++) {
                int power = (k * j) % n;
                double kth = -2 * power *  Math.PI / n;
                Complex wkj = new Complex(Math.cos(kth), Math.sin(kth));
                y[k] = y[k].plus(x[j].times(wkj));
            }
        }
        return y;
    }

    // display an array of Complex numbers to standard output
    public static void show(Complex[] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");
        for (int i = 0; i < x.length; i++) {
            System.out.println(x[i]);
        }
        System.out.println();
    }
}
