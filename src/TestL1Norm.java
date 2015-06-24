

import java.lang.reflect.Array;
import java.util.*;

import com.mathworks.toolbox.javabuilder.*;  // MATLAB Java Builder

import L1Norm.*;                             // our compiled package LeastR
import l1magic.*;							 // our compiled package l1magic


public class TestL1Norm {
	
    public static void main (String[] args) {
        try {
	    	System.out.println("TestL1Norm begins.....");
	        //A'= |A Im*m 0m*N  |
	        //    |B 0N*m -IN*N |
	    	//A': (m+N) *((regionsN-1)+m+N)
	    	
	    	//A: m*(regionsN-1)
	    	//B: N*(regionsN-1)
	    	
	    	//y' = | y     |
	    	//     | 0N*1  |
	    	
	    	//A'a = y'
	    	
	    	//a: (m*(regionsN-1)+m+N) * 1
	    	
	    	//m = kmeansK; N:imageN
	
	        int k = 1000;
	        int imageN = 700;
	        int regionsN = 700*20;
	        
	        int M = k+imageN;//~= 1700
	        int N = (regionsN-1) + k + imageN;//~= 18700
	
	        M = 1700;
	        N = 18700;
	
	        //creating test data....
	        //create sparse enough array A
	        Random r = new Random();
	        Random g = new Random();
	        double RATE = 0.0001;
	        double[][] A = new double[M][N];
	        for(int row = 0; row < M; row ++){
	            for(int col = 0; col < N; col ++ ){
	            	A[row][col] = 0;
	            	
	            	double gGuess = g.nextDouble();
	            	if(gGuess < RATE)
	            		A[row][col] = r.nextDouble();
	            }
	        }
	
	        //create sparse array y
	        double[] y = new double[M];
	        for(int i=0;i<M;i++){
	        	y[i] = 0;
	        	
	        	double gGuess = g.nextDouble();
	        	if(gGuess < RATE*20)//create sparse enough array
	        		y[i] = r.nextDouble();
	        }
	        
	        /****************************************************************
	         * calling L1Fun static method: double []x = calcL1(A, y);
	         * 
			 * @arg1 A: double [M][N] array, M(row)*N(column) 
			 * @arg2 y: double [M] array
			 * 
			 * @return: double [N] array
			 * **************************************************************/
	        L1Fun l1 = new L1Fun();
	        l1.init();
	        double []x = l1.calcL1(A, y);//Ax=y, find L1Norm result x
	        
	        //print output x 
	        ArrayList<Integer> regionIndexes = new ArrayList<Integer>();
	        for(int i = 0; i<x.length; i++){
	        	if(x[i] > 0){
	        		regionIndexes.add(i);
	        	}
	        }
	        System.out.println(Arrays.toString(x));
	        System.out.println(String.format("regions involved:%d", regionIndexes.size()));
	        System.out.println(regionIndexes);
	        System.out.println("Done.");
        } 
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}


