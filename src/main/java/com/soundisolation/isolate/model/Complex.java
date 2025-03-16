package com.soundisolation.isolate.model;

public class Complex {
    private final double real;
    private final double img;

    public Complex(double real, double img) {
        this.real = real;
        this.img = img;
    }

    public double getReal() {
        return real;
    }

    public double getImg() {
        return img;
    }

    //Addition
    public Complex plus(Complex other) {
        return new Complex(real + other.real, img + other.img);
    }

    //Subtraction
    public Complex minus(Complex other) {
        return new Complex(real - other.real, img - other.img);
    }

    //Multiplication
    public Complex times(Complex other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot perform complex operations with null argument");
        }
        double realPart = this.real * other.real - this.img * other.img;
        double imagPart = this.real * other.img + this.img * other.real;
        return new Complex(realPart, imagPart);
    }

    //Conjugate
    public Complex conjugate() {
        return new Complex(real, -img);
    }

    //Scaling
    public Complex scale(double scale) {
        return new Complex(real * scale, img * scale);
    }

    // toString for printing
    public String toString() {
        if (img == 0) return real + "";
        if (img > 0) return real + " + " + img + "i";
        return real + " - " + (-img) + "i";
    }
}
