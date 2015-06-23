
import com.mathworks.toolbox.javabuilder.*;  // MATLAB Java Builder
import L1Norm.*;
import l1magic.*;
//import L1NormBatch.*;
import java.util.*;

public class L1Fun {
	
    /***********************************************************************
     * find L1Norm answers for equation Ax=y
     * 
     * @arg1 inA: M(row)*N(column) double data array
     * @arg2 inY: array with double data; data count:M
     * 
     * @return: array with double data array; data count:N
     * *********************************************************************/
	public static double[] calcL1(double[][] inA, double[] inY) throws MWException{
		int M = inA.length;
		int N = inA[0].length;
		int yNum = inY.length;
		
		System.out.println(String.format("inA: %d * %d", M, N));
		System.out.println(String.format("inY: %d", yNum));
		
		if(yNum != M){
			System.out.println(String.format("Matrix dimensions must agree! A.row:%d y.row:%d", M, yNum));
			return null;
		}

        L1Norm l1norm = null;
        L1eq l1eq = null;
        MWNumericArray y = null;
        MWNumericArray x0 = null;
        MWNumericArray A = null;
        MWStructArray opts = null;
        Object[] l1result = null;
        Object[] l1result2 = null;
        
		double[] output = null;
        
        try {
        	l1norm = new L1Norm();
        	l1eq = new L1eq();
        	
        	output  = new double[N];   //final output
        	//double[][] arrayA  = new double[M][N];//M rows!
            double[][] arrayY  = new double[M][1];//M rows!
            double[][] arrayX0 = new double[N][1];

            String strY = "";
            //transform y into M*1 array
            for(int i=0;i<M;i++){
            	arrayY[i][0] = inY[i];
            }
        	
	        A = new MWNumericArray(inA, MWClassID.DOUBLE);
	        y = new MWNumericArray(arrayY, MWClassID.DOUBLE);
	        x0 = new MWNumericArray(arrayX0, MWClassID.DOUBLE);
	
	        System.out.println("A:");
//	        System.out.println(A); 
	        System.out.println("y:");
//	        System.out.println(y); 
	        System.out.println(Arrays.toString(inY));
	        
	        
			String[] inputStructFields = {"init", "tFlag", "maxIter",
					"nFlag", "rFlag", "mFlag", "lFlag"};
			opts = new MWStructArray(1, 1, inputStructFields);
			opts.set("init", 1, Integer.valueOf(2)); //% starting from a zero point
			opts.set("tFlag", 1, Integer.valueOf(5));//% run .maxIter iterations
			opts.set("maxIter", 1, Integer.valueOf(1000));//% maximum number of iterations
			opts.set("nFlag", 1, Integer.valueOf(0));//% without normalization
			opts.set("rFlag", 1, Integer.valueOf(0));//% when rFlag=1,the input parameter 'rho' is a ratio in (0, 1)
			opts.set("mFlag", 1, Integer.valueOf(0));//% treating it as compositive function 
			opts.set("lFlag", 1, Integer.valueOf(0));//% Nemirovski's line search
			System.out.println("opts:");
			System.out.println(opts); 

	        /*
	         * [x1, funVal1, ValueL1]= LeastR(A, y, rho, opts);
	         */
			double rho = 0.0;
			System.out.println("calculating LeastR, please wait.....");  
	        l1result = l1norm.LeastR(1, A, y, rho, opts);
//	        System.out.println("leastR result:");    
//	        System.out.println(l1result[0]); 
	        
	        //get output to normal 1*N array
            MWNumericArray ma  = (MWNumericArray) l1result[0];
            Object[] array = ma.toArray(); 
            output = new double[array.length];
            for(int i = 0; i < array.length; i++){
              double[] a = (double[])array[i];
              output[i] = a[0];
            }

            
//	        /*
//	         * xp = l1eq_pd(x0, A, [], y, 1e-3);
//	         * first x0 is the starting point, it is set to all zeros.
//	         * second x0 here is used as an empty input.
//	        */
//	        System.out.println("calculating l1eq_pd.....");  
//	        l1result2 = l1eq.l1eq_pd(1, x0, A, x0, y, 1e-4);
//	        System.out.println("l1eqpd result2:");    
//	        System.out.println(l1result2[0]); 
//
//	        //set output2 to 1*N array
//            MWNumericArray ma2  = (MWNumericArray) l1result2[0];
//            Object[] array2 = ma2.toArray(); 
//            double []output2 = new double[array2.length];
//            for(int i = 0; i < array2.length; i++){
//              double[] a2 = (double[])array2[i];
//              output2[i] = a2[0];
//            }
//            System.out.println(Arrays.toString(output2));
            
		} catch (MWException e) {
	        System.out.println("Exception: " + e.toString());
	    } finally {
	        MWArray.disposeArray(A);
	        MWArray.disposeArray(y);
	        MWArray.disposeArray(l1result);
	        MWArray.disposeArray(l1result2);
	        MWStructArray.disposeArray(opts);
	        l1norm.dispose();
	        l1eq.dispose();
	    }
        
        return output;
	}
	
//    /***********************************************************************
//     * find L1Norm answers for equation Ax=y in batch mode
//     * 
//     * @arg1 inA: M(row)*N(column) double data array (contains all regions)
//     * @arg2 inY: P(row)*Q(column) double data array (Q times' of y in column mode)
//     * 
//     * constraint: M must equals P!!
//     * 
//     * @return: result array [N][Q]. each column is a x N*1 column vector
//     *          so result contains Q times' result of x.
//     * *********************************************************************/
//	public static double[][] calcL1InBatch(double[][] inA, double[][] inY) throws MWException{
//		int M = inA.length;
//		int N = inA[0].length;
//		int P = inY.length;
//		int Q = inY[0].length;
//		
//		System.out.println(String.format("inA: %d * %d", M, N));
//		System.out.println(String.format("inY: %d", P));
//		
//		if(P != M){
//			System.out.println(String.format("Matrix dimensions must agree! A.row:%d y.row:%d", M, P));
//			return null;
//		}
//
//        L1NormBatch l1normBatch = null;
//        MWNumericArray y = null;
//        MWNumericArray A = null;
//        MWStructArray opts = null;
//        Object[] l1result = null;
//        
//		double[][] output = null;
//        
//        try {
//        	l1normBatch = new L1NormBatch();
//        	output  = new double[N][Q];   //final output
//
//	        A = new MWNumericArray(inA, MWClassID.DOUBLE);
//	        y = new MWNumericArray(inY, MWClassID.DOUBLE);
//
//			String[] inputStructFields = {"init", "tFlag", "maxIter",
//					"nFlag", "rFlag", "mFlag", "lFlag"};
//			opts = new MWStructArray(1, 1, inputStructFields);
//			opts.set("init", 1, Integer.valueOf(2)); //% starting from a zero point
//			opts.set("tFlag", 1, Integer.valueOf(5));//% run .maxIter iterations
//			opts.set("maxIter", 1, Integer.valueOf(1000));//% maximum number of iterations
//			opts.set("nFlag", 1, Integer.valueOf(0));//% without normalization
//			opts.set("rFlag", 1, Integer.valueOf(0));//% when rFlag=1,the input parameter 'rho' is a ratio in (0, 1)
//			opts.set("mFlag", 1, Integer.valueOf(0));//% treating it as compositive function 
//			opts.set("lFlag", 1, Integer.valueOf(0));//% Nemirovski's line search
//			System.out.println("opts:");
//			System.out.println(opts); 
//
//	        /*
//	         * [x1, funVal1, ValueL1]= LeastR(A, y, rho, opts);
//	         */
//			double rho = 0.06;
//			System.out.println("calculating LeastR, please wait.....");  
////	        l1result = l1norm.LeastR(1, A, y, rho, opts);
//			l1result = l1normBatch.LeastRBatch(1, A, y, rho, opts);
////	        System.out.println("leastR result:");    
////	        System.out.println(l1result[0]); 
//	        
//	        //get output to normal 1*N array
//            MWNumericArray ma  = (MWNumericArray) l1result[0];
//            Object[] array = ma.toArray(); 
//            for(int i = 0; i < array.length; i++){
//              double[] a = (double[])array[i];
//              for(int j = 0; j< a.length; j++){
//            	  output[i][j] = a[j];
//              }
//            }
//
//		} catch (MWException e) {
//	        System.out.println("Exception: " + e.toString());
//	    } finally {
//	        MWArray.disposeArray(A);
//	        MWArray.disposeArray(y);
//	        MWArray.disposeArray(l1result);
//	        MWStructArray.disposeArray(opts);
//	        l1normBatch.dispose();
//	    }
//        
//        return output;
//	}
}


