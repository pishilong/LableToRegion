


import com.mathworks.toolbox.javabuilder.MWException;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by lichaochen on 15/6/17.
 */
public class RegionMatrix {

    public static List <Region> matrix= new ArrayList<Region> ();

    public static void setupMatrix() throws Exception{
        String projectDirName = System.getProperty("user.dir");

        String maskDirName =  projectDirName + "/mask";
        String featureDirName = projectDirName + "/histogram";
        List <Region> regions = Region.parseMaskAndFeature(maskDirName, featureDirName);

        String labelFileName = projectDirName + "/imageLabels.txt";
        Region.importImageLable(labelFileName, regions);

        /*
        String featureDirName = projectDirName + "/histogram";
        Region.importFeature(featureDirName, regions);
        */
        matrix.addAll(regions);

    }
    public static void analyzeLabelToRegion() throws MWException {

        int regionTotalCount = matrix.size();

        //  since we keep image/region order in matrix, so the image-id of last region is leveraged to get image count.
        int imageTotalCount = matrix.get(matrix.size()-1).getImageId();

        int featureDemCount = matrix.get(matrix.size()-1).feature.size();

        List <Region>remainingRegions = new ArrayList<Region>();
        // loop to rebuild each region
        for (int j = 0; j< regionTotalCount; j++){
            log("re-construct regionId:" +j);
            Region reconstructedRegion = matrix.get(j);

            // re-use List of Remaining Regions
            remainingRegions.clear();

            for(int i = 0; i<regionTotalCount; i++){
                if (i != j){
                    remainingRegions.add(matrix.get(i));
                }
            }

            double [][] enhancedA = generateEnhancedA(remainingRegions);
            double [] enhancedY = generateEnhancedY(reconstructedRegion, imageTotalCount+featureDemCount);
//            log("enhanced A :");
//            ArrayUtil.print2DArray(enhancedA);
//            log("enhanced Y :");
//            ArrayUtil.printArray(enhancedY);


            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log("start - solve matrix eq" + df.format(new Date()));
            double [] solutionX = L1Fun.calcL1(enhancedA,enhancedY);
            log("end - solve matrix eq" + df.format(new Date()));

            ArrayUtil.printArray(solutionX);
            List regionContributors = getContributorList(solutionX,remainingRegions);
            reconstructedRegion.labelPropagation(regionContributors);
        }

        for(Region r : matrix){
            r.selectLabel();
        }
    }

    public static void generateReport(){
        log("generate report");
    }

    public static List<Region> getContributorList ( double [] solutionX, List<Region> candidateRegions ){

        int regionCount = candidateRegions.size();
        List<Region> contributors = new ArrayList<Region>();

        for(int i = 0; i < regionCount; i++){
            if(solutionX[i] != 0){
                contributors.add(candidateRegions.get(i));
            }
        }
        return contributors;

    }


    public static double[][] generateEnhancedA(List<Region> remainingRegions){


        // remaining region count
        int regionCount = remainingRegions.size();
        log("regionCount :"+regionCount);
        // feature dimension count
        int featureDemCount = remainingRegions.get(0).feature.size();
        log("featureDemCount :"+featureDemCount);
        // image count - since we keep image/region order in matrix, so the image-id of last region is leveraged.
        int image_count =  remainingRegions.get(remainingRegions.size()-1).getImageId();
        log("image_count :"+image_count);

        //array structure: (K+N, R+K+N)
        double enhancedA [][] ;
        enhancedA = new double [featureDemCount+image_count][regionCount+featureDemCount+image_count];
        log("row count of enhanced A :"+enhancedA.length);
        log("column count of enhanced A :"+enhancedA[0].length);

        int columnID = 0;
        // for first N(remaining region count) columns, 1 region is 1 column
        log("Generating first N Columns which is (A+B) format");
        for( ; columnID < regionCount; columnID++){
            log("**** each region *****");
            log("column id :"+columnID);
            // get empty column data
            Region eachRegion = remainingRegions.get(columnID);
            log("region imageId :"+eachRegion.imageId);
            log("region regionId :"+eachRegion.regionId);
            log("region weight :"+eachRegion.weightInImage);

            double [] columnData = ArrayUtil.getColumnFrom2DArray(enhancedA, columnID);
//            log("initial columndata : ");
//            ArrayUtil.printArray(columnData);

            // setup feature array
            Double featureArray [] =new Double [featureDemCount] ;
            eachRegion.feature.toArray(featureArray);
//            log("feature part columndata : ");
//            ArrayUtil.printArray(featureArray);

            // setup weight array
            double weightArray [] =  new double [image_count];
            weightArray[eachRegion.imageId -1] = eachRegion.weightInImage;
//            log("weight part columndata : ");
//            ArrayUtil.printArray(weightArray);

            // load feature & weight into the column data
            for (int i=0, j=0, k=0; i < columnData.length; i++){

                while (j<featureArray.length){
                    columnData[i] = featureArray[j];
                    j++;
                    i++;
                }
                while (k<weightArray.length){
                    columnData[i] = weightArray[k];
                    k++;
                    i++;
                }

            }
//            log("column data (A+B) :");
//            ArrayUtil.printArray(columnData);
            ArrayUtil.populate2DArrayByColumn(enhancedA, columnID, columnData);

        }
        log("Finish generating first N Columns which is (A+B) format, enhancedA: ");
//        ArrayUtil.print2DArray(enhancedA);

        log("*** Generate next K (feature dimension count) columns which (1+0) format");
        // for next K (feature dimension count) columns
        // for each column, first K (feature dimension count) column data is 1, remaining N (image_count)column data is 0
        int id_nextK = 1;
        for( ; id_nextK <= featureDemCount; id_nextK++ , columnID++){
            double [] columnData = ArrayUtil.getColumnFrom2DArray(enhancedA, columnID );

            for (int i =0; i<featureDemCount; i++){
                columnData[i] = 1;
            }

//            log("column data (1 0)");
//            ArrayUtil.printArray(columnData);
            ArrayUtil.populate2DArrayByColumn(enhancedA, columnID, columnData);

        }

        // for next N (image_count) columns
        // for each column, first K (feature dimension count) column data is 0, remaining N (image_count)column data is -1

        int id_nextN = 1;
        for( ; id_nextN <= image_count; id_nextN++, columnID++){
            double [] columnData = ArrayUtil.getColumnFrom2DArray(enhancedA, columnID);

            for (int i = featureDemCount; i<columnData.length; i++){
                columnData[i] = -1;
            }
//            log("column data (0 -1)");
//            ArrayUtil.printArray(columnData);
            ArrayUtil.populate2DArrayByColumn(enhancedA, columnID, columnData);
        }
        // at this moment, columnID should be the last column index of the Matrix
        log("all enhancedA is generated, with columns count :"+columnID);

        return enhancedA;


    }

    public static double[] generateEnhancedY (Region r, int arrayLength ){

        double[] enhancedY = new double[arrayLength];
        for(int i = 0; i < r.feature.size() ; i++){
                enhancedY[i] =  (Double)r.feature.get(i);
        }

        return enhancedY;
    }

    public static void log(Object msg){
        System.out.println(msg);
    }


    public static void main(String args[]) throws Exception {

//        testY();
//        testA();
//          testLabelPropagation();

        setupMatrix();
        analyzeLabelToRegion();
        generateReport();
		
        //genEnhancedAYInBatch();
    }


    public static void testY(){
        Region r = new Region(1, 3, 0.35);
        List<Double> f =r.feature;
        for(int i=0 ;i<100; i++){
            f.add((double) i);
        }

        double[] testy = generateEnhancedY(r, 200);
        ArrayUtil.printArray(testy);
    }

    public static void randomFeature(List<Double> feature, int featureDem){
        Random r = new Random();
        for(int i =0; i< featureDem; i++){
            feature.add(r.nextDouble());
        }
    }

    public static void initalLabel(Region r, int [] labels){
        for(int i = -1; i<= 7; i++){
            r.labelHistogram.put(i,0);
        }
        for(int labelId : labels){
            r.labelHistogram.put(labelId,1);
        }

    }

    public static void testA(){

        List<Region> list = testMatrix();
        double[][] testA_matrix = generateEnhancedA(list);
        ArrayUtil.print2DArray(testA_matrix);
    }

    public static List<Region> testMatrix(){
        List<Region> list = new ArrayList<Region>();

        Region r1 = new Region(1, 0, 0.35);
        randomFeature(r1.feature,100);
        int[] l1= {1, 3, 5};
        initalLabel(r1,l1);

        Region r2 = new Region(1, 1, 0.55);
        int[] l2= { 4, 7};
        initalLabel(r2,l2);
        randomFeature(r2.feature,100);


        Region r3 = new Region(1, 2, 0.10);
        int[] l3= { 3, 7};
        initalLabel(r3,l3);
        randomFeature(r3.feature,100);

        Region r4 = new Region(2, 0, 0.40);
        int[] l4= {1, 4, 7};
        initalLabel(r4,l4);
        randomFeature(r4.feature,100);

//        Region r5 = new Region(2, 1, 0.60);
//        int[] l1= {1, 3, 5};
//        initalLabel(r1,l1);
//        randomFeature(r5.feature,100);

        list.add(r1);
        list.add(r2);
        list.add(r3);
        list.add(r4);
//        list.add(r5);

        return list;

    }

    public static void testProcess(){

        matrix = testMatrix();
        try {
            analyzeLabelToRegion();
        } catch (MWException e) {
            e.printStackTrace();
        }
    }

    public static void testLabelPropagation(){
        Region r5 = new Region(2, 1, 0.60);
        int[] l1= {1, 3, 5};
        initalLabel(r5,l1);
        randomFeature(r5.feature,100);
        r5.addLabel(3);
        r5.selectLabel();

//        List <Region> list = testMatrix();
//        double[] resolutionX =new double[5+100+2];
//        resolutionX[2] = 0.8;
//        resolutionX[3] = 1.3;
//
//        List<Region> contributor = getContributorList(resolutionX, list);
//
//        for(Region r: contributor){
//            log("Contributor, image - "+r.getImageId() +" region -" +r.getRegionId());
//        }
//
//        r5.labelPropagation(contributor);

        log("Great Job");


    }
    
	public static void saveMatrixToFile(String fileName, double[][]matrix) throws Exception{
		File file = new File(fileName);  //存放数组数据的文件
		if(!file.exists()) file.createNewFile();
		 
		FileWriter textWriter = new FileWriter(file);  //文件写入流
 
		//将数组中的数据写入到文件中。每行各数据之间TAB间隔.
		int m = matrix.length;
		int n = matrix[0].length;
		
		log("matrix size: " + m + "*" + n);
		
		for(int i=0;i<m;i++){
			String fileContent = "";
			log("row:"+ i);
			for(int j=0;j<n;j++){
				fileContent += matrix[i][j]+" ";
			}
			fileContent += "\n";
			textWriter.write(fileContent);
		}
		
		textWriter.close();
    }
	
    
	/*
	 * 
	 * */
//    public static void genEnhancedAYInBatch() throws Exception {
//        int regionTotalCount = matrix.size();
//
//        //  since we keep image/region order in matrix, so the image-id of last region is leveraged to get image count.
//        int imageTotalCount = matrix.get(matrix.size()-1).getImageId();
//        int featureDemCount = matrix.get(matrix.size()-1).feature.size();
//
//        double [][] enhancedA = generateEnhancedA(matrix);
//        double [][] enhancedYMatrix = new double[imageTotalCount+featureDemCount][regionTotalCount];
//        
//        // loop to rebuild each region
//        for (int j = 0; j< regionTotalCount; j++){
//            log("re-construct regionId:" +j);
//            Region reconstructedRegion = matrix.get(j);
//
//            double [] enhancedY = generateEnhancedY(reconstructedRegion, imageTotalCount+featureDemCount);
//            ArrayUtil.populate2DArrayByColumn(enhancedYMatrix,j,enhancedY);
//        }
//        
//        double [][] solutionMatrixX = L1Fun.calcL1InBatch(enhancedA,enhancedYMatrix);
//        ArrayUtil.print2DArray(solutionMatrixX);
//        
//        //write enhancedA & enhancedY to files
////        String projectDirName = System.getProperty("user.dir");
////        RegionMatrix.saveMatrixToFile(projectDirName + "/AMatrix.txt", enhancedA);
////        RegionMatrix.saveMatrixToFile(projectDirName + "/YMatrix.txt", enhancedYMatrix);
//    }


}