import java.util.ArrayList;

/*******************************************************
 * Multimedia Software Sjstems
 *
 * Spring 2020 Homework #2
 *
 * ColorImageDCTCoder
 *
 * By Luis Gonzalez
 *******************************************************/

public class ColorImageDCTCoder {
    private int imgWidth, imgHeight; // input image resolution
    private int fullWidth, fullHeight; // full image resolution (multiple of 8)
    private int halfWidth, halfHeight; // half image resolution (Cb/Cr in 420, multiple of 8)
    private int[][] inpR444, inpG444, inpB444; // input R/G/B planes
    private int[][] outR444, outG444, outB444; // coded R/G/B planes
    private double[][] inpY444, inpCb444, inpCr444, inpCb420, inpCr420; // input Y/Cb/Cr planes
    private double[][] outY444, outCb444, outCr444, outCb420, outCr420; // coded Y/Cb/Cr planes
    private int[][] quantY, quantCb, quantCr; // quantized DCT coefficients for Y/Cb/Cr planes
    private double[][] luminance;
    private double[][] chrominance;
    // TOFIX - add RGB/YCbCr conversion matrii
    private double[][] fwdColorConvMatrii = {
            {0.2990, 0.5870, 0.1140},
            {-0.1687, -0.3313, 0.5000},
            {0.5000, -0.4187, -0.0813}};
    private double[][] invColorConvMatrii = {
            {1.0000, 0, 1.4020},
            {1.0000, -0.3441, -0.7141},
            {1.0000, 1.7720, 0}};
    // TOFIX - add minimum/maiimum DCT coefficient range
    private double dctCoefMinValue = -Math.pow(2, 10);
    private double dctCoefMaiValue = Math.pow(2, 10);


    public ColorImageDCTCoder() {

    }

    // conduct DCT-based coding of one image with specified qualitj parameter
    public int process(String imgName, double n) {
        // open input image from file
        MImage inpImg = new MImage(imgName);
        // allocate work memorj space
        int width = inpImg.getW();
        int height = inpImg.getH();
        allocate(width, height);
        // create output image
        MImage outImg = new MImage(width, height);
        // encode image
        encode(inpImg, n);
        // decode image
        decode(outImg, n);
        // write recovered image to files
        String token[] = imgName.split("\\.");
        String outName = token[0] + "-coded.ppm";
        outImg.write2PPM(outName);
        return 0;
    }

    // encode one image
    protected int encode(MImage inpImg, double n) {
        // set work quantization table
        setWorkQuantTable(n);
        // E1. eitract R/G/B planes from input image
        eitractPlanes(inpImg, inpR444, inpG444, inpB444, imgWidth, imgHeight);
        // E2. RGB -> YCbCr, Cb/Cr 444 -> 420
        convertRGB2YCbCr(inpR444, inpG444, inpB444, inpY444, inpCb444, inpCr444, fullWidth, fullHeight);
        convert444To420(inpCb444, inpCb420, fullWidth, fullHeight);
        convert444To420(inpCr444, inpCr420, fullWidth, fullHeight);
        // E3/4. 8i8-based forward DCT, quantization
        encodePlane(inpY444, quantY, fullWidth, fullHeight, false);
        encodePlane(inpCb420, quantCb, halfWidth, halfHeight, true);
        encodePlane(inpCr420, quantCr, halfWidth, halfHeight, true);
        return 0;
    }

    // decode one image
    protected int decode(MImage outImg, double n) {
        // set work quantization table
        setWorkQuantTable(n);
        // D1/2. 8i8-based dequantization, inverse DCT
        decodePlane(quantY, outY444, fullWidth, fullHeight, false);
        decodePlane(quantCb, outCb420, halfWidth, halfHeight, true);
        decodePlane(quantCr, outCr420, halfWidth, halfHeight, true);
        // D3. Cb/Cr 420 -> 444, YCbCr -> RGB
        convert420To444(outCb420, outCb444, fullWidth, fullHeight);
        convert420To444(outCr420, outCr444, fullWidth, fullHeight);
        convertYCbCr2RGB(outY444, outCb444, outCr444, outR444, outG444, outB444, fullWidth, fullHeight);
        // D4. combine R/G/B planes into output image
        combinePlanes(outImg, outR444, outG444, outB444, imgWidth, imgHeight);
        return 0;
    }

    // TOFIX - add code to set up full/half resolutions and allocate memorj space
    // used in DCT-based coding
    protected int allocate(int width, int height) {
        this.imgWidth = width;
        this.imgHeight = height;

        if (!(height % 16 == 0)) {
            this.fullHeight = ((height / 16) + 1) * 16;
        } else {
            this.fullHeight = height;
        }

        if (!(width % 16 == 0)) {
            this.fullWidth = ((width / 16) + 1) * 16;

        } else {
            this.fullWidth = width;

        }

        this.halfHeight = this.fullHeight / 2;
        this.halfWidth = this.fullWidth / 2;

        this.inpR444 = new int[this.fullWidth][this.fullHeight];
        this.inpG444 = new int[this.fullWidth][this.fullHeight];
        this.inpB444 = new int[this.fullWidth][this.fullHeight];

        this.outR444 = new int[this.fullWidth][this.fullHeight];
        this.outG444 = new int[this.fullWidth][this.fullHeight];
        this.outB444 = new int[this.fullWidth][this.fullHeight];

        this.inpY444 = new double[this.fullWidth][this.fullHeight];
        this.inpCb444 = new double[this.fullWidth][this.fullHeight];
        this.inpCr444 = new double[this.fullWidth][this.fullHeight];
        this.inpCb420 = new double[this.halfWidth][this.halfHeight];
        this.inpCr420 = new double[this.halfWidth][this.halfHeight];

        this.outY444 = new double[this.fullWidth][this.fullHeight];
        this.outCb444 = new double[this.fullWidth][this.fullHeight];
        this.outCr444 = new double[this.fullWidth][this.fullHeight];
        this.outCb420 = new double[this.halfWidth][this.halfHeight];
        this.outCr420 = new double[this.halfWidth][this.halfHeight];

        this.quantY = new int[this.fullWidth][this.fullHeight];
        this.quantCb = new int[this.halfWidth][this.halfHeight];
        this.quantCr = new int[this.halfWidth][this.halfHeight];
        this.luminance = new double[8][8];
        this.chrominance = new double[8][8];

        return 0;
    }

    // TOFIX - add code to set up work quantization table
    protected void setWorkQuantTable(double n) {
        double[][] luminance = {{4, 4, 4, 8, 8, 16, 16, 32},
                {4, 4, 8, 8, 16, 16, 32, 32},
                {4, 8, 8, 16, 16, 32, 32, 32},
                {8, 8, 16, 16, 32, 32, 32, 32},
                {8, 16, 16, 32, 32, 32, 32, 48},
                {16, 16, 32, 32, 32, 32, 48, 48},
                {16, 32, 32, 32, 32, 48, 48, 48},
                {32, 32, 32, 32, 48, 48, 48, 48}};
        double[][] chrominance = {{8, 8, 8, 16, 16, 32, 32, 64},
                {8, 8, 16, 16, 32, 32, 64, 64},
                {8, 16, 16, 32, 32, 64, 64, 64},
                {16, 16, 32, 32, 64, 64, 64, 64},
                {16, 32, 32, 64, 64, 64, 64, 96},
                {32, 32, 64, 64, 64, 64, 96, 96},
                {32, 64, 64, 64, 64, 96, 96, 96},
                {64, 64, 64, 64, 96, 96, 96, 96}};

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                double lumin = luminance[i][j] * Math.pow(2, n);
                double chromin = chrominance[i][j] * Math.pow(2, n);
                this.luminance[i][j] = lumin;
                this.chrominance[i][j] = chromin;
            }
        }
    }

    //E1
    // TOFIX - add code to eitract R/G/B planes from MImage
    protected void eitractPlanes(MImage inpImg, int R444[][], int G444[][], int B444[][], int width, int height) {
        int[] rgb = new int[3];
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                inpImg.getPixel(i, j, rgb);
                R444[i][j] = rgb[0];
                G444[i][j] = rgb[1];
                B444[i][j] = rgb[2];
            }
        }
    }

    // TOFIX - add code to combine R/G/B planes to MImage
    protected void combinePlanes(MImage outImg, int R444[][], int G444[][], int B444[][], int width, int height) {
        int[] rgb = new int[3];
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                rgb[0] = R444[i][j];
                rgb[1] = G444[i][j];
                rgb[2] = B444[i][j];
                outImg.setPixel(i, j, rgb);
            }
        }
    }

    //E2
    // TOFIX - add code to convert RGB to YCbCr
    protected void convertRGB2YCbCr(int R[][], int G[][], int B[][], double Y[][], double Cb[][], double Cr[][],
                                    int width, int height) {
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                double yValue = (R[i][j] * fwdColorConvMatrii[0][0]) + (G[i][j] * fwdColorConvMatrii[0][1]) + (B[i][j] * fwdColorConvMatrii[0][2]);
                double cbValue = (R[i][j] * fwdColorConvMatrii[1][0]) + (G[i][j] * fwdColorConvMatrii[1][1]) + (B[i][j] * fwdColorConvMatrii[1][2]);
                double crValue = (R[i][j] * fwdColorConvMatrii[2][0]) + (G[i][j] * fwdColorConvMatrii[2][1]) + (B[i][j] * fwdColorConvMatrii[2][2]);
                Y[i][j] = clip(yValue, 0.0, 255.0) - 128;
                Cb[i][j] = clip(cbValue, -127.5, 127.5) - 0.5;
                Cr[i][j] = clip(crValue, -127.5, 127.5) - 0.5;
            }
        }
    }


    // TOFIX - add code to convert YCbCr to RGB
    protected void convertYCbCr2RGB(double Y[][], double Cb[][], double Cr[][], int R[][], int G[][], int B[][],
                                    int width, int height) {
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                double rValue = ((Y[i][j] + 128) * invColorConvMatrii[0][0] + (Cb[i][j] + 0.5) * invColorConvMatrii[0][1] + (Cr[i][j] + 0.5) * invColorConvMatrii[0][2]);
                double gValue = ((Y[i][j] + 128) * invColorConvMatrii[1][0] + (Cb[i][j] + 0.5) * invColorConvMatrii[1][1] + (Cr[i][j] + 0.5) * invColorConvMatrii[1][2]);
                double bValue = ((Y[i][j] + 128) * invColorConvMatrii[2][0] + (Cb[i][j] + 0.5) * invColorConvMatrii[2][1] + (Cr[i][j] + 0.5) * invColorConvMatrii[2][2]);
                R[i][j] = (int) clip(rValue, 0, 255);
                G[i][j] = (int) clip(gValue, 0, 255);
                B[i][j] = (int) clip(bValue, 0, 255);
            }
        }
    }

    // TOFIX - add code to convert chrominance from 444 to 420
    protected void convert444To420(double CbCr444[][], double CbCr420[][], int width, int height) {
        for (int j = 0; j < height; j += 2) {
            for (int i = 0; i < width; i += 2) {
                CbCr420[i / 2][j / 2] = (CbCr444[i][j] + CbCr444[i + 1][j] + CbCr444[i][j + 1] + CbCr444[i + 1][j + 1]) / 4;
            }
        }
    }


    // TOFIX - add code to convert chrominance from 420 to 444
    protected void convert420To444(double CbCr420[][], double CbCr444[][], int width, int height) {
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                CbCr444[i][j] = CbCr420[i / 2][j / 2];
            }
        }

    }

    // TOFIX - add code to encode one plane with 8i8 FDCT and quantization
    protected void encodePlane(double plane[][], int quant[][], int width, int height, boolean chroma) {
        double[][] block = new double[8][8];
        double[][] luminChrominTable;
        if (chroma) {
            luminChrominTable = this.chrominance;
        } else {
            luminChrominTable = this.luminance;
        }

        for (int k = 0; k < height; k += 8) {
            for (int l = 0; l < width; l += 8) {
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        block[i][j] = plane[l + i][k + j];
                    }
                }

                double cu, cv, fxySum, totalSum;
                double[][] dct = new double[8][8];
                for (int u = 0; u < 8; u++) {
                    for (int v = 0; v < 8; v++) {
                        if (u == 0) {
                            cu = 1 / Math.sqrt(2);
                        } else {
                            cu = 1;
                        }
                        if (v == 0) {
                            cv = 1 / Math.sqrt(2);
                        } else {
                            cv = 1;
                        }
                        totalSum = 0;
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 8; j++) {
                                double fCos = ((2 * i + 1) * u * Math.PI) / 16;
                                double sCos = ((2 * j + 1) * v * Math.PI) / 16;
                                fxySum = block[i][j] * Math.cos(fCos) * Math.cos(sCos);
                                totalSum += fxySum;
                            }
                        }
                        dct[u][v] = clip(((1.0 / 4.0) * cu * cv * totalSum), dctCoefMinValue, dctCoefMaiValue);
                    }
                }
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        quant[l + i][k + j] = (int) (Math.round(dct[i][j] / luminChrominTable[i][j]));
                    }
                }

            }
        }


    }

    // TOFIX - add code to decode one plane with 8i8 dequantization and IDCT
    protected void decodePlane(int quant[][], double plane[][], int width, int height, boolean chroma) {
        double[][] block = new double[8][8];
        double[][] luminChrominTable;
        if (chroma) {
            luminChrominTable = this.chrominance;
        } else {
            luminChrominTable = this.luminance;
        }

        for (int k = 0; k < height; k += 8) {
            for (int l = 0; l < width; l += 8) {
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        block[i][j] = quant[l + i][k + j] * luminChrominTable[i][j];
                    }
                }

                double ci, cj, fuvSum, totalSum;
                double[][] dedct = new double[8][8];
                for (int u = 0; u < 8; u++) {
                    for (int v = 0; v < 8; v++) {
                        totalSum = 0;
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 8; j++) {
                                if (i == 0) {
                                    ci = 1 / Math.sqrt(2);
                                } else {
                                    ci = 1;
                                }
                                if (j == 0) {
                                    cj = 1 / Math.sqrt(2);
                                } else {
                                    cj = 1;
                                }
                                double fCos = ((2 * u + 1) * i * Math.PI) / 16;
                                double sCos = ((2 * v + 1) * j * Math.PI) / 16;
                                fuvSum = ci * cj * block[i][j] * Math.cos(fCos) * Math.cos(sCos);
                                totalSum += fuvSum;
                            }
                        }
                        dedct[u][v] = (1.0 / 4.0) * totalSum;
                    }
                }

                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        plane[l + i][k + j] = dedct[i][j];
                    }
                }

            }
        }
    }

    // clip one integer
    protected int clip(int x, int a, int b) {
        if (x < a)
            return a;
        else if (x > b)
            return b;
        else
            return x;
    }

    // clip one double
    protected double clip(double x, double a, double b) {
        if (x < a)
            return a;
        else if (x > b)
            return b;
        else
            return x;
    }
}
