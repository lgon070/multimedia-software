import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.*;

/*******************************************************
 * CS4551 Multimedia Software Systems
 *
 * Spring 2020 Homework #2
 *
 * ColorImageVectorQuantizer
 *
 * By Luis Gonzalez
 *******************************************************/

public class ColorImageVectorQuantizer {
    private HashMap<Integer, ArrayList<int[]>> hashCodeMap = new HashMap<>();
    private int imgWidth, imgHeight; // img resolution /
    private int blkWidth, blkHeight; // block resolution /
    private int numBlock; // number of blocks in img
    private int numDimension; // number of vector dimension in VQ /
    private int numCluster;// number of clusters in VQ /
    private int maxIteration; // maximum number of iteration in VQ training /
    private int[][] codeBook; // codebook in VQ/
    private int inputVectors[][]; // vectors from input img
    private int quantVectors[][]; // vectors for quantized img
    private int quantIndices[]; // quantized indices for blocks
    private int halfWidth;
    private int halfHeight;

    public ColorImageVectorQuantizer() {
        this.blkWidth = 2;
        this.blkHeight = 2;
        this.numDimension = blkWidth * blkHeight * 3;
        this.numCluster = 256;
        this.maxIteration = 100;
    }

    public int process(String inputName) {
        // read 24-bit color img from PPM file
        MImage inputImage = new MImage(inputName);
        System.out.println(inputImage);
        String token[] = inputName.split("\\.");
        // set up workspace
        int width = inputImage.getW();
        int height = inputImage.getH();
        allocate(width, height);
        // form vector from input img
        img2Vectors(inputImage, inputVectors, imgWidth, imgHeight);
        // train vector quantizer
        train(inputVectors, numBlock);
        // display trained codebook
        display();
        // quantize input img vectors to indices
        quantize(inputVectors, numBlock, quantIndices);
        // TOFIX - add code to save indices as PPM file
        MImage indexImage = new MImage(halfWidth, halfHeight);
        indices2PPM(quantIndices, imgWidth, imgHeight, halfWidth, halfHeight, indexImage);
        String indecy = token[0] + "-index.ppm";
        indexImage.write2PPM(indecy);
        // dequantize indices back to vectors
        dequantize(quantIndices, numBlock, quantVectors);
        // write quantized img to file
        MImage quantImage = new MImage(imgWidth, imgHeight);
        vectors2Image(quantVectors, quantImage, width, height);
        String quantName = token[0] + "-quant.ppm";
        quantImage.write2PPM(quantName);
        return 0;
    }

    // TOFIX - add code to set up work space
    protected int allocate(int width, int height) {

        this.imgWidth = width;
        this.imgHeight = height;
        this.halfHeight = height / 2;
        this.halfWidth = width / 2;
        this.codeBook = new int[numCluster][numDimension];
        if (width % 2 != 0) {
            numBlock = (int) Math.ceil((double) (height * (width + 1)) / 4);
        } else if (height % 2 != 0) {
            numBlock = (int) Math.ceil((double) ((height + 1) * width) / 4);
        } else {
            numBlock = (int) Math.ceil((double) (height * width) / 4);
        }
        this.inputVectors = new int[numBlock][numDimension];
        this.quantVectors = new int[numBlock][numCluster];
        this.quantIndices = new int[numBlock];

        return 0;
    }

    // TOFIX - add code to convert one img to vectors in VQ
    protected void img2Vectors(MImage img, int vectors[][], int width, int height) {
        int vIndex = 0;
        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {

                int[] rgb = new int[3];
                img.getPixel(x, y, rgb);
                vectors[vIndex][0] = rgb[0];
                vectors[vIndex][1] = rgb[1];
                vectors[vIndex][2] = rgb[2];

                if (x + 1 >= width) {
                    img.getPixel(x, y, rgb);
                    vectors[vIndex][3] = rgb[0];
                    vectors[vIndex][4] = rgb[1];
                    vectors[vIndex][5] = rgb[2];

                } else {
                    img.getPixel(x + 1, y, rgb);
                    vectors[vIndex][3] = rgb[0];
                    vectors[vIndex][4] = rgb[1];
                    vectors[vIndex][5] = rgb[2];
                }
                if (y + 1 >= height) {
                    img.getPixel(x, y, rgb);
                    vectors[vIndex][6] = rgb[0];
                    vectors[vIndex][7] = rgb[1];
                    vectors[vIndex][8] = rgb[2];
                } else {
                    img.getPixel(x, y + 1, rgb);
                    vectors[vIndex][6] = rgb[0];
                    vectors[vIndex][7] = rgb[1];
                    vectors[vIndex][8] = rgb[2];
                }
                if (x + 1 >= width && y + 1 >= height) {
                    img.getPixel(x, y, rgb);
                    vectors[vIndex][9] = rgb[0];
                    vectors[vIndex][10] = rgb[1];
                    vectors[vIndex][11] = rgb[2];
                } else {
                    img.getPixel(x + 1, y + 1, rgb);
                    vectors[vIndex][9] = rgb[0];
                    vectors[vIndex][10] = rgb[1];
                    vectors[vIndex][11] = rgb[2];
                }

                vIndex++;
            }
        }

    }

    // TOFIX - add code to convert vectors to one img in VQ
    protected void vectors2Image(int vectors[][], MImage img, int width, int height) {

        int vIndex = 0;
        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                int[] vector = vectors[vIndex];
                int[] rgb = new int[3];
                rgb[0] = vector[0];
                rgb[1] = vector[1];
                rgb[2] = vector[2];
                img.setPixel(x, y, rgb);

                if (!(x + 1 >= width)) {
                    rgb[0] = vector[3];
                    rgb[1] = vector[4];
                    rgb[2] = vector[5];
                    img.setPixel(x + 1, y, rgb);
                }
                if (!(y + 1 >= height)) {
                    rgb[0] = vector[6];
                    rgb[1] = vector[7];
                    rgb[2] = vector[8];
                    img.setPixel(x, y + 1, rgb);
                }
                if (!(x + 1 >= width && y + 1 >= height)) {
                    rgb[0] = vector[9];
                    rgb[1] = vector[10];
                    rgb[2] = vector[11];
                    img.setPixel(x + 1, y + 1, rgb);
                }

                vIndex++;

            }
        }

    }


    // TOFIX - add code to train codebook with K-means clustering algorithm
    protected void train(int vectors[][], int count) {

        Random r = new Random();
        for (int i = 0; i < numCluster; i++) {
            boolean equals = false;
            int index = r.nextInt(numBlock - 0) + 0;

            if (i == 0) {
                codeBook[i] = vectors[index];
            } else {
                for (int j = 0; j < i; j++) {
                    if (Arrays.equals(codeBook[j], vectors[index])) {
                        equals = true;
                        continue;
                    }
                }
                if (!equals) {
                    codeBook[i] = vectors[index];
                    continue;
                }
                i--;
            }

        }

        for (int i = 0; i < count; i++) {

            int[] vector = vectors[i];
            double minDist = Double.MAX_VALUE;
            int index = 0;

            for (int j = 0; j < numCluster; j++) {

                int[] cluster = codeBook[j];
                int sum = 0;

                for (int k = 0; k < 12; k++) {

                    sum += (vector[k] - cluster[k]) * (vector[k] - cluster[k]);

                }

                double curDist = Math.sqrt(sum);
                if (curDist < minDist) {
                    minDist = curDist;
                    index = j;
                }

            }

            if (hashCodeMap.containsKey(index)) {
                hashCodeMap.get(index).add(vector);
            } else {
                ArrayList<int[]> arrayList = new ArrayList<>();
                arrayList.add(vector);
                hashCodeMap.put(index, arrayList);
            }

        }

    }

    // TOFIX - add code to display codebook
    protected void display() {

        for (int i = 0; i < numCluster; i++) {
            for (int j = 0; j < codeBook[i].length; j++) {

                System.out.print(codeBook[i][j] + "|");
            }
            System.out.println();
        }

    }

    // TOFIX - add code to quantize vectors to indices
    protected void quantize(int vectors[][], int count, int indices[]) {

        for (int i = 0; i < count; i++) {

            int[] vector = vectors[i];

            indices[i] = locateVector(vector);

        }

    }


    // TOFIX - add code to dequantize indices to vectors
    protected void dequantize(int indices[], int count, int vectors[][]) {

        for (int i = 0; i < count; i++) {
            vectors[i] = codeBook[indices[i]];
        }

    }

    protected void indices2PPM(int[] indices, int fullWidth, int fullHeight, int width, int height, MImage img) {

        int vIndex = 0;
        for (int y = 0; y < fullHeight; y += 2) {
            for (int x = 0; x < fullWidth; x += 2) {

                if (y < height && x < width) {
                    int index = indices[vIndex];
                    int[] rgb = new int[3];
                    rgb[0] = index;
                    rgb[1] = index;
                    rgb[2] = index;
                    img.setPixel(x, y, rgb);

                    if (!(x + 1 >= width)) {
                        rgb[0] = index;
                        rgb[1] = index;
                        rgb[2] = index;
                        img.setPixel(x + 1, y, rgb);
                    }
                    if (!(y + 1 >= height)) {
                        rgb[0] = index;
                        rgb[1] = index;
                        rgb[2] = index;
                        img.setPixel(x, y + 1, rgb);
                    }
                    if (!(x + 1 >= width && y + 1 >= height)) {
                        rgb[0] = index;
                        rgb[1] = index;
                        rgb[2] = index;
                        img.setPixel(x + 1, y + 1, rgb);
                    }
                }

                vIndex++;

            }
        }
    }


    protected int locateVector(int[] vector) {

        for (int i = 0; i < hashCodeMap.size(); i++) {

            if (hashCodeMap.containsKey(i)) {

                for (int j = 0; j < hashCodeMap.get(i).size(); j++) {

                    if (Arrays.equals(vector, hashCodeMap.get(i).get(j))) {

                        return i;
                    }

                }

            }

        }

        return 0;

    }
}
