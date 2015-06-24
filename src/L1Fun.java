
import com.mathworks.toolbox.javabuilder.*;  // MATLAB Java Builder
import L1Norm.*;
import l1magic.*;
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
	
//	        System.out.println("A:");
//	        System.out.println(A); 
//	        System.out.println("y:");
//	        System.out.println(y); 
//	        System.out.println(Arrays.toString(inY));
	        
	        
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
			double rho = 0.059;
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
}



//%----------------------- Set optional items ------------------------
//opts=[];
//
//% Starting point
//opts.init=2;        % starting from a zero point
//
//% termination criterion
//opts.tFlag=5;       % run .maxIter iterations
//opts.maxIter=100;   % maximum number of iterations
//
//% normalization
//opts.nFlag=0;       % without normalization
//
//% regularization
//opts.rFlag=1;       % the input parameter 'rho' is a ratio in (0, 1)
//opts.rFlag=0; %!!!the program uses the input values for 'nameda' and 'row'
//%opts.rsL2=0.01;     % the squared two norm term
//
//%----------------------- Run the code LeastR -----------------------
//fprintf('\n mFlag=0, lFlag=0 \n');
//opts.mFlag=0;       % treating it as compositive function 
//opts.lFlag=0;       % Nemirovski's line search
//
//HashMap<String, String> opts = new HashMap<String, String>();
//opts.put("init", "2");
//opts.put("tFlag", "5");
//opts.put("maxIter", "100");
//opts.put("nFlag", "0");
//opts.put("rFlag", "0");
//opts.put("mFlag", "0");
//opts.put("lFlag", "0");


