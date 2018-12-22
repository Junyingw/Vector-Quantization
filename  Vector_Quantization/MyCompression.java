import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.nio.channels.Channel;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.awt.geom.*;
import java.util.*
;

/*****************  Image Compression RAW & RGB: Only finish mode 1 and mode 2 *****************/
public class MyCompression{
    JFrame frame;
    JLabel lbIm1;
	JLabel lbIm2;
	static  BufferedImage Img;
	static  BufferedImage CompressedImg;
    static int width = 352;
    static int height = 288;
	static byte [] bytes_original; 
	static byte [] bytes_output;           
	static int N; 
	static int Channel=0;
	static int mode;
	static int choice;
	static int type;
	static double [][] odd_input;   
	static double [][] even_input;            
	static double [][] odd_output;  
	static double [][] even_output;  
	static double [][] input3;   
	static double [][] input4;            
	static double [][] output3;  
	static double [][] output4;  
    static int [] index1;   
	static int [] index2;   
	static int [] index3;  
	static int [] index4;                   
    static int [] newindex1;  
	static int [] newindex2;  
	static int [] newindex3;  
    static int [] newindex4;                 
    static double [][] centroid1;     
	static double [][] centroid2; 
	static double [][] centroid3;     
    static double [][] centroid4;     
    static double [][] codebook1;   
	static double [][] codebook2;  
	static double [][] codebook3;   
	static double [][] codebook4;  
	static int [] mapping;                   
	static int [] frequency;           
	static int [] freq_sorted;    
	static int [] mapping_output;	
	static byte[] bytes;

	//Choosing random value to initialize centroids
	//Calculation of the k means of pixels individually and combining them 1*2, 2*2 4*4. 
	public static void InitializeCentroids_rgb(int n, double [][] centroid)
	{
		// Initialize centroids randomly
		N=n;
		if (centroid !=null) {
			centroid1 = centroid;
			centroid2 = centroid;
			if(mode==2){
				centroid3 = centroid;
				centroid4 = centroid;
			}
		}
		if(centroid == null){
			centroid1 = new double[N][3];
			centroid2 = new double[N][3];
			if(mode==2){
				centroid3 = new double[N][3];
				centroid4 = new double[N][3];
			}
			ArrayList index = new ArrayList();
			for (int i = 0; i < N; i++){
				int key;
				do{
					key = (int) (Math.random()*(width*height/type));
				}while(index.contains(key)); 
				index.add(key);
				for (int j=0; j<3; j++) {
					centroid1[i][j] = odd_input[key][j];
					centroid2[i][j] = even_input[key][j];
					if(mode==2){
						centroid3[i][j] = input3[key][j];
						centroid4[i][j] = input4[key][j];
					}
				}
			}
		}
	}
	public static void InitializeCentroids_raw(int n, double [][] centroid)
	{
		// Initialize centroids randomly
		N=n;
		if (centroid !=null) {
			centroid1 = centroid;
			centroid2 = centroid;
			if(mode==2){
				centroid3 = centroid;
				centroid4 = centroid;
			}
		}
		if(centroid == null){
			centroid1 = new double[N][1];
			centroid2 = new double[N][1];
			if(mode==2){
				centroid3 = new double[N][1];
				centroid4 = new double[N][1];
			}
			ArrayList index = new ArrayList();
			for (int i = 0; i < N; i++){
				int key;
				do{
					key = (int) (Math.random()*(width*height/type));
				}while(index.contains(key)); 
				index.add(key);
				centroid1[i][0] = odd_input[key][0];
				centroid2[i][0] = even_input[key][0];
				if(mode==2){
					centroid3[i][0] = input3[key][0];
					centroid4[i][0] = input4[key][0];
				}
			}
		}
	}
	//Cluster even pixels and odd pixels seperately
	public static void ClusterData_rgb(int runs){
		// Cluster odd pixels 
		int iteration=0;
		double [][] centroid_1 = centroid1;
		while (true){
			centroid1 = centroid_1;
			index1 = new int[(width*height/type)];
			for (int i=0; i<(width*height/type); i++){
				index1[i] = FindNearestCentroid_rgb(odd_input[i],centroid1);    
			}
			centroid_1 = UpdateCentroids_rgb(index1,odd_input);
			iteration ++;
			if ((runs >0 && iteration >=runs) || CheckConvergence_rgb(centroid1, centroid_1))
				break;
		}
		System.out.println("Cluster iteration of odd pixels: " + iteration+"\n");
		// Cluster even pixels 
		double [][] centroid_2 = centroid2;
		iteration=0;
		while (true){
			centroid2 = centroid_2;
			index2 = new int[(width*height/type)];
			for (int i=0; i<(width*height/type); i++){
				index2[i] = FindNearestCentroid_rgb(even_input[i],centroid2);
			}
			centroid_2 = UpdateCentroids_rgb(index2,even_input);
			iteration ++;
			if (runs >0 && iteration >=runs)
				break;
			if(CheckConvergence_rgb(centroid2, centroid_2))
				break;
		}
		System.out.println("Cluster iteration of even pixels: " + iteration+"\n");
		if(mode==2){
		// Cluster the third pixel 
			double [][] centroid_3 = centroid3;
			iteration=0;
			while (true){
				centroid3 = centroid_3;
				index3 = new int[(width*height/4)];
				for (int i=0; i<(width*height/4); i++){
					index3[i] = FindNearestCentroid_rgb(input3[i],centroid3);
				}
				centroid_3 = UpdateCentroids_rgb(index3,input3);
				iteration ++;
				if (runs >0 && iteration >=runs)
					break;
				if(CheckConvergence_rgb(centroid3, centroid_3))
					break;
			}
			System.out.println("Cluster iteration of the third pixels: " + iteration+"\n");
			// Cluster the fourth pixel 
			double [][] centroid_4 = centroid4;
			iteration=0;
			while (true){
				centroid4 = centroid_4;
				index4 = new int[(width*height/4)];
				for (int i=0; i<(width*height/4); i++){
					index4[i] = FindNearestCentroid_rgb(input4[i],centroid4);
				}
				centroid_4 = UpdateCentroids_rgb(index4,input4);
				iteration ++;
				if (runs >0 && iteration >=runs)
					break;
				if(CheckConvergence_rgb(centroid4, centroid_4))
					break;
			}
			System.out.println("Cluster iteration of the third pixels: " + iteration+"\n");
		}
	}

	//Cluster even pixels and odd pixels seperately
	public static void ClusterData_raw(int runs){
		// Cluster odd pixels 
		int iteration=0;
		double [][] centroid_1 = centroid1;
		while (true){
			centroid1 = centroid_1;
			index1 = new int[(width*height/type)];
			for (int i=0; i<(width*height/type); i++){
				index1[i] = FindNearestCentroid_raw(odd_input[i],centroid1);    
			}
			centroid_1 = UpdateCentroids_raw(index1,odd_input);
			iteration ++;
			if ((runs >0 && iteration >=runs) || CheckConvergence_raw(centroid1, centroid_1))
				break;
		}
		System.out.println("Cluster iteration of odd pixels: " + iteration+"\n");
		// Cluster even pixels 
		double [][] centroid_2 = centroid2;
		iteration=0;
		while (true){
			centroid2 = centroid_2;
			index2 = new int[(width*height/type)];
			for (int i=0; i<(width*height/type); i++){
				index2[i] = FindNearestCentroid_raw(even_input[i],centroid2);
			}
			centroid_2 = UpdateCentroids_raw(index2,even_input);
			iteration ++;
			if (runs >0 && iteration >=runs)
				break;
			if(CheckConvergence_raw(centroid2, centroid_2))
				break;
		}
		System.out.println("Cluster iteration of even pixels: " + iteration+"\n");
		if(mode==2){
			// Cluster the third pixel 
			double [][] centroid_3 = centroid3;
			iteration=0;
			while (true){
				centroid3 = centroid_3;
				index3 = new int[(width*height/4)];
				for (int i=0; i<(width*height/4); i++){
					index3[i] = FindNearestCentroid_raw(input3[i],centroid3);
				}
				centroid_3 = UpdateCentroids_raw(index3,input3);
				iteration ++;
				if (runs >0 && iteration >=runs)
					break;
				if(CheckConvergence_raw(centroid3, centroid_3))
					break;
			}
			System.out.println("Cluster iteration of the third pixels: " + iteration+"\n");
			// Cluster the fourth pixel 
			double [][] centroid_4 = centroid4;
			iteration=0;
			while (true){
				centroid4 = centroid_4;
				index4 = new int[(width*height/4)];
				for (int i=0; i<(width*height/4); i++){
					index4[i] = FindNearestCentroid_raw(input4[i],centroid4);
				}
				centroid_4 = UpdateCentroids_raw(index4,input4);
				iteration ++;
				if (runs >0 && iteration >=runs)
					break;
				if(CheckConvergence_raw(centroid4, centroid_4))
					break;
			}
			System.out.println("Cluster iteration of the third pixels: " + iteration+"\n");
		}
	}

	// Checking Euclidean distance to find which is the closest pixel and assign the current pixel to the centroid.
	public static int FindNearestCentroid_rgb(double [] RGB , double[][] centroid){
		int index=0;
		double t=0;
		double min= Double.POSITIVE_INFINITY;
		for(int i=0; i<N; i++){
			double sum=0;
			for (int j=0; j<3; j++){
				double d = RGB[j]-centroid[i][j];
				sum += d*d;
			}
			t=Math.sqrt(sum);
			if (min>t){
				min = t;
				index = i;
			}
		}
		return index;
	}

	// Checking Euclidean distance to find which is the closest pixel and assign the current pixel to the centroid.
	public static int FindNearestCentroid_raw(double [] RGB , double[][] centroid){
		int index=0;
		double t=0;
		double min= Double.POSITIVE_INFINITY;
		for(int i=0; i<N; i++){
			double sum=0;
			double d = RGB[0]-centroid[i][0];
			sum += d*d;
			t=Math.sqrt(sum);
			if (min>t){
				min = t;
				index = i;
			}
		}
		return index;
	}

	//Update the centroids after finishing clustering pixels
	public static double [][] UpdateCentroids_rgb(int [] index , double[][] image){
		double [][] newcentorid = new double [N][3]; 
		int [] counts = new int[N]; 
		for (int i=0; i<N; i++){
			counts[i] =0;
			for (int j=0; j<3; j++){
				newcentorid[i][j] =0;
			}		
		}
		for (int i=0; i<(width*height/type); i++){
			int newcount = index[i]; 
			for (int j=0; j<3; j++){
				newcentorid[newcount][j] += image[i][j]; 
			}
			counts[newcount]++;
		}
		for (int i=0; i< N; i++){
			for (int j=0; j<3; j++){
				newcentorid[i][j]/= counts[i];
			}
		}
		return newcentorid;
	}

	//Update the centroids after finishing clustering pixels
	public static double [][] UpdateCentroids_raw(int [] index , double[][] image){
		double [][] newcentorid = new double [N][1]; 
		int [] counts = new int[N]; 
		for (int i=0; i<N; i++){
			counts[i] =0;
			newcentorid[i][0] =0;	
		}
		for (int i=0; i<(width*height/type); i++){
			int newcount = index[i]; 
			newcentorid[newcount][0] += image[i][0]; 
			counts[newcount]++;
		}
		for (int i=0; i< N; i++){
			newcentorid[i][0]/= counts[i];
		}
		return newcentorid;
	}

	// Evaluate the Euclidean distance for checking which is the closest RGB pixel
	public static double Euclidean_rgb(double [] RGB1, double [] RGB2){
		double sum=0;
		for (int i=0; i<3; i++){
			double d = RGB1[i]-RGB2[i];
			sum += d*d;
		}
		return Math.sqrt(sum);
	}

	// Evaluate the Euclidean distance for checking which is the closest RAW pixel
	public static double Euclidean_raw(double [] RGB1, double [] RGB2){
		double sum=0;
		double d = RGB1[0]-RGB2[0];
		sum += d*d;
		return Math.sqrt(sum);
	}

	// Checking convergence RGB,setting threshold to 0.00001
	public static boolean CheckConvergence_rgb(double [][] centroid_1, double [][] centroid_2){
		double min = 0;
		double threshold = 0.00001;
		double t=0;
		for(int i=0; i<N; i++){
			double sum=0;
			for (int j=0; j<3; j++){
				double d = centroid_1[i][j]-centroid_2[i][j];
				sum += d*d;
			}
			t=Math.sqrt(sum);
			if (min<t) min = t;
		}
		if (min < threshold)
			return true;
		else
			return false;
	}

	// Checking convergence RAW,setting threshold to 0.00001
	public static boolean CheckConvergence_raw(double [][] centroid_1, double [][] centroid_2){
		double min = 0;
		double threshold = 0.00001;
		double t=0;
		for(int i=0; i<N; i++){
			double sum=0;
			double d = centroid_1[i][0]-centroid_2[i][0];
			sum += d*d;
			t=Math.sqrt(sum);
			if (min<t) min = t;
		}
		if (min < threshold)
			return true;
		else
			return false;
	}

	// Building the codebook and mapping the vector to  all possible combinations 
	public static void BuildCodebook_rgb (int N) {
		codebook1 = new double[N][3];
		codebook2 = new double[N][3];
		newindex1 = new int [N];
		newindex2 = new int [N];
		if(mode==2){
			codebook3 = new double[N][3];
			codebook4 = new double[N][3];
			newindex3 = new int [N];
			newindex4 = new int [N];
		}
		int[] mapping_output =new int[N];
		mapping_output= CheckFrequency ();
		for (int i = 0 ; i < N ; i++) {
			int shift = (int) (Math.log(N) / Math.log(2));
			if(mode==1){
				newindex1[i] = (mapping_output[i] >> shift) & (N-1);
				newindex2[i] = mapping_output[i] & (N-1);
			}
			
			if(mode==2){
				newindex1[i] = (mapping_output[i] >> shift) & (N-1);
				newindex2[i] = (mapping_output[i] >> shift) & (N-1);
				newindex3[i] = (mapping_output[i] >> shift) & (N-1);
				newindex4[i] = (mapping_output[i] >> shift) & (N-1);
			}
		}
		for (int i = 0 ; i < N ; i++) {				
			for (int j = 0 ; j < 3 ; j++) {
				codebook1[i][j] = centroid1[newindex1[i]][j];
				codebook2[i][j] = centroid2[newindex2[i]][j];
				if(mode==2){
					codebook3[i][j] = centroid1[newindex3[i]][j];
					codebook4[i][j] = centroid2[newindex4[i]][j];
				}
			}
		}
	}
	
	// Building the codebook and mapping the vector to  all possible combinations 
	public static void BuildCodebook_raw (int N) {
		codebook1 = new double[N][1];
		codebook2 = new double[N][1];
		newindex1 = new int [N];
		newindex2 = new int [N];
		if(mode==2){
			codebook3 = new double[N][1];
			codebook4 = new double[N][1];
			newindex3 = new int [N];
			newindex4 = new int [N];
		}
		int[] mapping_output =new int[N];
		mapping_output= CheckFrequency ();
		for (int i = 0 ; i < N ; i++) {
			int shift = (int) (Math.log(N) / Math.log(2));
			if(mode==1){
				newindex1[i] = (mapping_output[i] >> shift) & (N-1);
				newindex2[i] = mapping_output[i] & (N-1);
			}
			if(mode==2){
				newindex1[i] = (mapping_output[i] >> shift) & (N-1);
				newindex2[i] = mapping_output[i] & (N-1);
				newindex3[i] = (mapping_output[i] >> shift) & (N-1);
				newindex4[i] = mapping_output[i] & (N-1);
			}
		}
		for (int i = 0 ; i < N ; i++) {				
			codebook1[i][0] = centroid1[newindex1[i]][0];
			codebook2[i][0] = centroid2[newindex2[i]][0];
			if(mode==2){
				codebook3[i][0] = centroid1[newindex3[i]][0];
				codebook4[i][0] = centroid2[newindex4[i]][0];
			}
		}
	}

	//Finding the most frequent N codewords to bulid the codebook
	public static int[] CheckFrequency (){
		mapping = new int [(width*height/type)];
		for (int i = 0 ; i < (width*height/type) ; i++) {
			mapping[i] = 0;
		}
		for (int i = 0 ; i < (width*height/type) ; i++) {
			int shift = (int) (Math.log(N) / Math.log(2));
			if(mode==1){
				mapping[i] = (index1[i] << shift) + index2[i];
			}
			if(mode ==2){
				mapping[i] = (index1[i] << shift) + index2[i]+(index3[i] << shift)+ index4[i];
			}
		}
		frequency = new int[N*N];
		for (int i = 0 ; i < N*N ; i++) {
			int count = 0;
			for (int j = 0 ; j < (width*height/type) ; j++) {
				if(mapping[j] == i) 
					count++;
			}
			frequency[i] = count;
		}
		int [] freq = new int[N*N];
		for (int i = 0 ; i < N*N ; i++) {
			freq[i] = frequency[i];
		}
		Arrays.sort(freq);
		for (int i = 0 ; i < freq.length/type ; i++) {
			int tempt = freq[i];
			freq[i] = freq[freq.length - (i+1)];
			freq[freq.length - (i+1)] = tempt;
		}
		freq_sorted = new int[N];
		for (int i = 0 ; i < N ; i++) {
			freq_sorted[i] = freq[i];
		}
		mapping_output = new int [N];
		for (int i = 0 ; i < N ; i++) {
			int res = -1;
			for (int j = 0 ; j < frequency.length ; j++) {
				if (frequency[j] == freq_sorted[i]) {
					res = j;
					break;
				}
			}
			mapping_output[i] = res;
		}
		return mapping_output;
	}

	// Vector quantization by choosing the most frequent K RGB tuples.
	public static void VectorQuantization() {
		if(Channel==1){
			odd_output = new double[(width*height/type)][1];
			even_output = new double[(width*height/type)][1];
		}
		if(Channel==3){
			odd_output = new double[(width*height/type)][3];
			even_output = new double[(width*height/type)][3];
		}
		if(mode==1){
			for (int k = 0 ; k < (width*height/2) ; k++) {	
				for (int i = 0 ; i < N ; i++) {
					int [] array1 = {index1[k] , index2[k]};
					int [] array2 = {newindex1[i] , newindex2[i]};
					if(Arrays.equals(array1,array2)) {
						if(Channel==3){
							for(int j=0;j<3;j++){
								odd_output[k][j] = centroid1[index1[k]][j];
								even_output[k][j] = centroid2[index2[k]][j];
							}
							break;
						}
						if(Channel==1){
							odd_output[k][0] = centroid1[index1[k]][0];
							even_output[k][0] = centroid2[index2[k]][0];
							break;
						}	
					} else {
						if(Channel==1){
							double [] rgb1 = new double[1];
							double [] rgb2 = new double[1];
							rgb1[0] = odd_input[k][0];
							rgb2[0] = even_input[k][0];
							double min = 2 * Math.pow(255,3); 
							int index = -1;
							for (int j = 0 ; j < N ; j++) {
								double error1= Math.pow(Euclidean_raw(rgb1,codebook1[j]), 2);
								double error2= Math.pow(Euclidean_raw(rgb2,codebook2[j]), 2);
								double error = error1 + error2;
								if (error < min ) {
									min = error;
									index = j;
								}
							}
							odd_output[k][0] = centroid1[newindex1[index]][0];
							even_output[k][0] = centroid2[newindex2[index]][0];
						}
						if(Channel==3){
							double [] rgb1 = new double[3];
							double [] rgb2 = new double[3];
							for (int j = 0 ; j < 3 ; j++) {
								rgb1[j] = odd_input[k][j];
								rgb2[j] = even_input[k][j];
							}
							double min = 2 * Math.pow(255,3); 
							int index = -1;
							for (int j = 0 ; j < N ; j++) {
								double error1= Math.pow(Euclidean_rgb(rgb1,codebook1[j]), 2);
								double error2= Math.pow(Euclidean_rgb(rgb2,codebook2[j]), 2);
								double error = error1 + error2;
								if (error < min ) {
									min = error;
									index = j;
								}
							}
							for(int t=0;t<3;t++){
								odd_output[k][t] = centroid1[newindex1[index]][t];
								even_output[k][t] = centroid2[newindex2[index]][t];
							}
						}
					}
				}
			}
		}
		if(mode==2){
			if(Channel==1){
				output3= new double[(width*height/type)][1];
				output4= new double[(width*height/type)][1];
			}
			if(Channel==3){
				output3= new double[(width*height/type)][3];
				output4= new double[(width*height/type)][3];
			}
			for (int k = 0 ; k < (width*height/type) ; k++) {	
				for (int i = 0 ; i < N ; i++) {
					int [] array1 = {index1[k] , index2[k], index3[k], index4[k]};
					int [] array2 = {newindex1[i] , newindex2[i], newindex3[i], newindex4[i]};
					if(Arrays.equals(array1,array2)) {
						if(Channel==1){
							odd_output[k][0] = centroid1[index1[k]][0];
							even_output[k][0] = centroid2[index2[k]][0];
							output3[k][0] = centroid3[index3[k]][0];
							output4[k][0] = centroid4[index4[k]][0];
							break;
						}
						if(Channel==3){
							for(int j=0;j<3;j++){
								odd_output[k][j] = centroid1[index1[k]][j];
								even_output[k][j] = centroid2[index2[k]][j];
								output3[k][j] = centroid3[index3[k]][j];
								output4[k][j] = centroid4[index4[k]][j];
							}
							break;
						}
						
					} else {
						if(Channel==1){
							double [] rgb1 = new double[1];
							double [] rgb2 = new double[1];
							double [] rgb3 = new double[1];
							double [] rgb4 = new double[1];
							rgb1[0] = odd_input[k][0];
							rgb2[0] = even_input[k][0];
							rgb3[0] = input3[k][0];
							rgb4[0] = input4[k][0];
							double min = 2 * Math.pow(255,3); 
							int index = -1;
							for (int j = 0 ; j < N ; j++) {
								double error1= Math.pow(Euclidean_raw(rgb1,codebook1[j]), 2);
								double error2= Math.pow(Euclidean_raw(rgb2,codebook2[j]), 2);
								double error3= Math.pow(Euclidean_raw(rgb1,codebook3[j]), 2);
								double error4= Math.pow(Euclidean_raw(rgb2,codebook4[j]), 2);
								double error = (error1 + error2 + error3 + error4)/2;
								if (error < min ) {
									min = error;
									index = j;
								}
							}
							odd_output[k][0] = centroid1[newindex1[index]][0];
							even_output[k][0] = centroid2[newindex2[index]][0];
							output3[k][0] = centroid3[newindex3[index]][0];
							output4[k][0] = centroid4[newindex4[index]][0];
						}
						if(Channel==3){
							double [] rgb1 = new double[3];
							double [] rgb2 = new double[3];
							double [] rgb3 = new double[3];
							double [] rgb4 = new double[3];
							for (int j = 0 ; j < 3 ; j++) {
								rgb1[j] = odd_input[k][j];
								rgb2[j] = even_input[k][j];
								rgb3[j] = input3[k][j];
								rgb4[j] = input4[k][j];
							}
							double min = 2 * Math.pow(255,3); 
							int index = -1;
							for (int j = 0 ; j < N ; j++) {
								double error1= Math.pow(Euclidean_rgb(rgb1,codebook1[j]), 2);
								double error2= Math.pow(Euclidean_rgb(rgb2,codebook2[j]), 2);
								double error3= Math.pow(Euclidean_rgb(rgb1,codebook3[j]), 2);
								double error4= Math.pow(Euclidean_rgb(rgb2,codebook4[j]), 2);
								double error = (error1 + error2 + error3 + error4)/2;
								if (error < min ) {
									min = error;
									index = j;
								}
							}
							for(int t=0;t<3;t++){
								odd_output[k][t] = centroid1[newindex1[index]][t];
								even_output[k][t] = centroid2[newindex2[index]][t];
								output3[k][t] = centroid3[newindex3[index]][t];
								output4[k][t] = centroid4[newindex4[index]][t];
							}
						}
					}
				}
			}
		}
	}

	// Converting back to Interlaced RGB format.
	public static void BytesConvert_rgb () {
		int ind = 0;
		int flag =0;
		int total=width*height;
		bytes_output = new byte [3*total];
		if(mode==1){
			for (int i = 0 ; i < (total/2) ;i++) {
				for(int j=0; j< 3; j++){
					bytes_output[ind+j*total] = (byte) (int) odd_output[i][j];
					bytes_output[ind+1+j*total] = (byte) (int) even_output[i][j];
				}
			ind = ind + 2;
			}
		}
		if(mode==2){
			for (int i = 0 ; i < total/4 ;i++) {
				for(int j =0; j<3; j++){
					if(flag==width){
						ind =ind + width;
						flag=0;
					} 
					bytes_output[ind+j*total] = (byte) (int) odd_output[i][j];
					bytes_output[ind+1+j*total] = (byte) (int) even_output[i][j];
					bytes_output[ind+ width +j*total] =(byte) (int) output3[i][j];	
					bytes_output[ind+ width+1 +j*total] =(byte) (int) output4[i][j];
				}
				ind = ind + 2;
				flag = flag +2;
			}
		}
	}

	// Converting back to Interlaced RGB format.
	public static void BytesConvert_raw () {
		int ind = 0;
		int flag =0;
		int total=width*height;
		bytes_output = new byte [3*total];
		if(mode==1){
			for (int i = 0 ; i < (total/2) ;i++) {
				bytes_output[ind] = (byte) (int) odd_output[i][0];
				bytes_output[ind+1] = (byte) (int) even_output[i][0];
				ind = ind + 2;
			}
		}
		if(mode==2){
			for (int i = 0 ; i < total/4 ;i++) {
				if(flag==width){
					ind =ind + width;
					flag=0;
				} 
				bytes_output[ind] = (byte) (int) odd_output[i][0];
				bytes_output[ind+1] = (byte) (int) even_output[i][0];
				bytes_output[ind+ width] =(byte) (int) output3[i][0];	
				bytes_output[ind+ width+1] =(byte) (int) output4[i][0];
				ind = ind + 2;
				flag = flag +2;
			}
		}
	}
	public static BufferedImage ImageDisplay_rgb(int width,int height, byte[] bytes) {
		Img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int ind = 0;
		int pix = 0;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				byte r = bytes[ind];
				byte g = bytes[ind+height*width];
				byte b = bytes[ind+height*width*2];
				pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				Img.setRGB(x,y,pix);
				ind++;
			}
		} 
		return Img;
	}
	public BufferedImage ImageDisplay_raw(int width,int height,byte [] bytes) {
		Img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int ind = 0;
		int[][] r = new int[width][height];
		for(int h = 0; h < height; h++){
			for(int w = 0; w < width; w++){

				byte a = 0;
				byte red = bytes[ind];
				r[w][h] = red&0xff;
				int pix = 0xff000000 | ((r[w][h] & 0xff) << 16) | ((r[w][h] & 0xff) << 8) | (r[w][h] & 0xff);
				Img.setRGB(w,h,pix);
				ind++;
			}
		}
		return Img;
	}

    public void showIms(String[] args){
		int N = Integer.parseInt(args[1]); //Number of clusters
		mode = Integer.parseInt(args[2]); //Choosing mode
        BufferedImage Img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage CompressedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		String param0 = args[0];
		String[] values=param0.split("\\.");
		String extension = values[1];

		//Input data evaluation as given in the starter code.
		try {
			//Checking for input data.
			File file = new File(param0);
			InputStream is = new FileInputStream(file);
			long len = file.length();
			bytes_original = new byte[(int)len];
			int offset = 0;
			int numRead = 0;
			while (offset < bytes_original.length && (numRead=is.read(bytes_original, offset, bytes_original.length-offset)) >= 0) 
			{
				offset += numRead;
			}
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(extension.equals("rgb")) Channel=3; 
		if(extension.equals("raw")) Channel=1; 
		if(mode == 1) {
			choice = 256;
			type=2;
		}
		if(mode == 2){
			choice = 256;
			type=4;
		}
		if(mode == 3){
			mode=1;
		}
		if(Channel==1){
			if(mode==1){
				int ind = 0;
				odd_input = new double[width*height/2][1];
				even_input = new double[width*height/2][1];
				int total = width*height;
				for (int i = 0 ; i < total/2 ; i++) {
					odd_input[i][0] = 0xFF & bytes_original[ind];
					even_input[i][0] = 0xFF & bytes_original[ind+1];	
					ind = ind + 2;
				}
			}
			if(mode==2){
				int ind=0;
				int flag =0;
				odd_input = new double[width*height/4][1];
				even_input = new double[width*height/4][1];
				input3 = new double[width*height/4][1];
				input4 = new double[width*height/4][1];
				for (int i = 0 ; i < width*height/4 ;i++) {
					if(flag==width){
						ind =ind + width;
						flag=0;
					} 
					odd_input[i][0] = 0xFF & bytes_original[ind];
					even_input[i][0] = 0xFF & bytes_original[ind+1];
					input3[i][0] = 0xFF & bytes_original[ind +width];	
					input4[i][0] = 0xFF & bytes_original[ind +width+1];
					ind = ind + 2;
					flag = flag + 2;
				}
			}
			InitializeCentroids_raw( N ,null);
			System.out.println("Number of codewords is "+N+"\n");
			System.out.println("InitializeCentroids_raw finish!\n");
			ClusterData_raw(500);
			System.out.println("ClusterData_raw finish!\n");
			BuildCodebook_raw(N);
			System.out.println("BuildCodebook_raw finish!\n");
			VectorQuantization();
			System.out.println("VectorQuantization finish!\n");
	    	BytesConvert_raw();
			System.out.println("BytesConvert_raw finish!\n");
			Img = ImageDisplay_raw(width,height,bytes_original);
			CompressedImg=ImageDisplay_raw(width,height,bytes_output);
			System.out.println("ImageDisplay_raw finish!\n");
		}
		if(Channel==3){
			if(mode==1){
				int ind = 0;
				odd_input = new double[width*height/2][3];
				even_input = new double[width*height/2][3];
				int total = width*height;
				for (int i = 0 ; i < total/2 ; i++) {
					for(int j =0; j<3; j++){
						odd_input[i][j] = 0xFF & bytes_original[ind+(j*total)];
						even_input[i][j] = 0xFF & bytes_original[(ind+1)+(j*total)];
						
					}
					ind = ind + 2;
				}
			}
			if(mode==2){
				int ind=0;
				int flag =0;
				odd_input = new double[width*height/4][3];
				even_input = new double[width*height/4][3];
				input3 = new double[width*height/4][3];
				input4 = new double[width*height/4][3];
				int total = width*height;
				for (int i = 0 ; i < width*height/4 ;i++) {	
					for(int j =0; j<3; j++){
						if(flag==width){
							ind =ind + width;
							flag=0;
						} 
						odd_input[i][j] = 0xFF & bytes_original[ind+(j*total)];
						even_input[i][j] = 0xFF & bytes_original[(ind+1)+(j*total)];
						input3[i][j] = 0xFF & bytes_original[ind +width+ j*total];	
						input4[i][j] = 0xFF & bytes_original[ind +width+1 + j*total];
					}
					ind = ind + 2;
					flag = flag + 2;
				}
			}
			InitializeCentroids_rgb( N ,null);
			System.out.println("Number of codewords is "+N+"\n");
			System.out.println("InitializeCentroids_rgb finish!\n");
			ClusterData_rgb(500);
			System.out.println("ClusterData_rgb finish!\n");
			BuildCodebook_rgb(N);
			System.out.println("BuildCodebook_rgb finish!\n");
			VectorQuantization();
			System.out.println("VectorQuantization_rgb finish!\n");
	    	BytesConvert_rgb();
			System.out.println("BytesConvert_rgb finish!\n");
			Img = ImageDisplay_rgb(width,height,bytes_original);
			CompressedImg=ImageDisplay_rgb(width,height,bytes_output);
			System.out.println("ImageDisplay_rgb finish!\n");
		}

        // Use labels to display the images
        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        JLabel lbText1 = new JLabel("Original image (Left)");
        lbText1.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lbText2 = new JLabel("Image after compression (Right)");
        lbText2.setHorizontalAlignment(SwingConstants.CENTER);
        lbIm1 = new JLabel(new ImageIcon(Img));
        lbIm2 = new JLabel(new ImageIcon(CompressedImg));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        frame.getContentPane().add(lbText1, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        frame.getContentPane().add(lbText2, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        frame.getContentPane().add(lbIm2, c);
        frame.pack();
        frame.setVisible(true);
	} 
    public static void main(String[] args) {
      MyCompression ren = new MyCompression();
        ren.showIms(args);
    }
    
}
