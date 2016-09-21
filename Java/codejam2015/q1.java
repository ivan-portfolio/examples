package gcjam15;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class q1 {

	public static void main(String[] args) throws IOException {
		BufferedReader inputStream = null;
		
		try {
			inputStream = new BufferedReader(new FileReader("a2.in"));
			q1m(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static void q1m(BufferedReader input) throws IOException {
		Scanner scanner = new Scanner(input);
		int cases = 0;
		BufferedWriter bw = new BufferedWriter(new FileWriter("a2.out"));
		
		cases = Integer.parseInt(scanner.nextLine());
		
		for (int i = 1; i <= cases; i++) {
			
			int max = scanner.nextInt();
			int s = 0; //standing
			int f = 0; //friends
			
			String crowd = scanner.next();
			
			
			for (int level = 0; level <= max; level++) {
				int shypop = Character.getNumericValue(crowd.toCharArray()[level]);
				
				//debug output
				//System.out.println("lvl:" + level + " shy:" + shypop + " stand:" + s + " fr:" + f);
				
				if (shypop != 0) {
					if (s + f >= level) {
						
						s += shypop;
					}
					else { //add friends to make stand
						int af = level - s - f;
						f += af;
						s += shypop;
						//debug output
						//System.out.println("fr added: " + af);
					}
				}
			}
			String strout = "Case #" + i + ": " + f;
			System.out.println(strout);
			bw.write(strout);
			bw.newLine();
			
		}
		scanner.close();
		bw.close();
		
	}
	
}