
/*******************************************************
 * CS4551 Multimedia Software Systems @ Author: Elaine Kang
 * 
 * Spring 2020 Homework #2 Framework by Yi Zhao
 *******************************************************/

import java.util.Scanner;

public class CS4551_Main {
	public static void main(String[] args) {

		// the program expects one command line argument
		// if there is no command line argument, exit the program
		if (args.length != 1) {
			usage();
			System.exit(1);
		}

		// define main variables
		boolean done = false;
		String imgName = args[0];
		Scanner in = new Scanner(System.in);

		// main menu
		while (!done) {
			int choice = menu(in);
			switch (choice) {
			case 1:
				conductVectorQuantization(imgName);
				break;
			case 2:
				conductDCTCoding(imgName, in);
				break;
			case 3:
				done = true;
				break;
			default:
				System.out.println("Invalid command!");

			}
		}
	}

	public static void usage() {
		System.out.println("\nUsage: java CS4551_Main [input_ppm_file]\n");
	}

	// main menu
	public static int menu(Scanner in) {
		int choice = 0;
		String menu = "\nMain Menu------------------------------------------\n" +
				"1. Vector Quantization\n" +
				"2. DCT-based Coding\n" +
				"3. Quit\n\n"+
				"Please Enter the task number [1-3]: ";
		System.out.print(menu);
		try {
			choice = in.nextInt();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return choice;
	}
	
	public static int conductVectorQuantization(String imgName) {
		ColorImageVectorQuantizer quantizer = new ColorImageVectorQuantizer();
		quantizer.process(imgName);
		return 0;	
	}
	
	public static int conductDCTCoding(String imgName, Scanner in) {
		System.out.println("Enter quality parameter n [0..5] ");
		double n = in.nextDouble();
		ColorImageDCTCoder coder = new ColorImageDCTCoder();
		coder.process(imgName, n);
		return 0;
	}
}
