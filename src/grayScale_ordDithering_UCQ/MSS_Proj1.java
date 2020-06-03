
/*******************************************************
 Multimedia Software Systems
 @ Author: Luis Gonzalez
 *******************************************************/

import java.util.Scanner;

public class MSS_Proj1 {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        if (args.length != 1) {
            usage();
            System.exit(1);
        }

        System.out.println("--Welcome to Multimedia Software Systems--");


        // Create an Image object with the input PPM file name.
        MImage img = new MImage(args[0]);
        System.out.println(img);
        String fileName = args[0];
        fileName = fileName.substring(0, fileName.length() - 4);
        while(true) {
            System.out.println(
                    "Main Menu-------------------------------------------\r\n" +
                            "1. Conversion to Gray-scale Image (24bits->8bits)\r\n" +
                            "2. Conversion to Binary Image using Ordered Dithering (k=4)\r\n" +
                            "3. Conversion to 8bit Indexed Color Image using Uniform Color Quantization (24bits->8bits)\r\n" +
                            "4. Quit\r\n" +
                            "Please enter the task number [1-4]: ");
            int option = in.nextInt();

            switch (option) {
                case 1:
                    grayScale(img, fileName);
                    break;
                case 2:
                    orderedDithering(img, fileName);
                    break;
                case 3:
                    uniformColorQuantization(img, fileName);
                    break;
                case 4:
                    System.exit(0);
                    break;
                default:
                    System.exit(0);
            }
        }

    }

    public static void uniformColorQuantization(MImage img, String fileName) {
        int[][] LUT = buildLutTable();
        printLutTable(LUT);

        for (int i = 0; i < img.getH(); i++) {
            for (int j = 0; j < img.getW(); j++) {
                int[] rgb = new int[3];
                img.getPixel(j, i, rgb);

                int index = (rgb[0] / 32 * 32) + (rgb[1] / 32 * 4) + (rgb[2] / 64);

                rgb[0] = index;
                rgb[1] = index;
                rgb[2] = index;

                img.setPixel(j, i, rgb);
            }
        }
        img.write2PPM(fileName + "-index.ppm");

        for (int i = 0; i < img.getH(); i++) {
            for (int j = 0; j < img.getW(); j++) {
                int[] rgb = new int[3];
                img.getPixel(j, i, rgb);
                int[] outRGB = LUT[rgb[0]];
                img.setPixel(j, i, outRGB);
            }
        }

        img.write2PPM(fileName + "-QT8.ppm");
    }

    public static void orderedDithering(MImage img, String fileName) {
        int[][] ditherMatrix = {{0, 8, 2, 10},
                {12, 4, 14, 6},
                {3, 11, 1, 9},
                {15, 7, 13, 5}};

        for (int i = 0; i < img.getH(); i++) {
            for (int j = 0; j < img.getW(); j++) {
                int[] rgb = new int[3];
                img.getPixel(j, i, rgb);
                int dx = j % 4;
                int dy = i % 4;
                int val = (int) (rgb[0] * 17.0 / 255.0);
                if (val > ditherMatrix[dx][dy]) {
                    rgb[0] = 255;
                    rgb[1] = 255;
                    rgb[2] = 255;
                } else {
                    rgb[0] = 0;
                    rgb[1] = 0;
                    rgb[2] = 0;
                }
                img.setPixel(j, i, rgb);
            }
        }
        img.write2PPM(fileName + "-OD4.ppm");
    }

    public static void grayScale(MImage img, String fileName) {

        for (int i = 0; i < img.getH(); i++) {
            for (int j = 0; j < img.getW(); j++) {
                int[] rgb = new int[3];
                img.getPixel(j, i, rgb);
                int gray = (int) Math.round(0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
                rgb[0] = gray;
                rgb[1] = gray;
                rgb[2] = gray;
                img.setPixel(j, i, rgb);
            }
        }

        img.write2PPM(fileName + "-gray.ppm");
    }

    public static void usage() {
        System.out.println("\nUsage: java MSS_Proj1 [input_ppm_file]\n");
    }

    public static int[][] buildLutTable() {
        int[][] LUT = new int[256][3];
        for (int i = 0; i < LUT.length; i++) {
            String num = Integer.toBinaryString(i);
            num = formatString(num);

            int first = Integer.parseInt(num.substring(0, 3), 2);
            int second = Integer.parseInt(num.substring(3, 6), 2);
            int last = Integer.parseInt(num.substring(6), 2);

            LUT[i][0] = 16 + (first * 32);
            LUT[i][1] = 16 + (second * 32);
            LUT[i][2] = 32 + (last * 64);

        }

        return LUT;
    }

    public static void printLutTable(int[][] LUT) {
        System.out.println("LUT by UCQ");
        System.out.println("Index  R  G  B ");
        System.out.println("-----------------------------");
        for (int i = 0; i < LUT.length; i++) {
            System.out.print(i);
            for (int j = 0; j < LUT[i].length; j++) {
                System.out.print(" " + LUT[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static String formatString(String num) {
        String padToAdd = "";
        int padding = 8 - num.length();
        if (padding != 0) {
            for (int i = 0; i < padding; i++) {
                padToAdd += "0";
            }
        }
        num = padToAdd + num;
        return num;
    }
}
