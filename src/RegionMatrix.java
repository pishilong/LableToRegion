


import com.mathworks.toolbox.javabuilder.MWException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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


//        String featureDirName = projectDirName + "/histogram";
//        Region.importFeature(featureDirName, regions);

        matrix.addAll(regions);

    }
    public static void analyzeLabelToRegion() throws MWException {

        int regionTotalCount = matrix.size();

        //  since we keep image/region order in matrix, so the image-id of last region is leveraged to get image count.
        //  int imageTotalCount = matrix.get(matrix.size()-1).getImageId();


        int featureDemCount = matrix.get(matrix.size()-1).feature.size();

        List <Region>remainingRegions = new ArrayList<Region>();
        HashMap<Integer, Double> knnMap = new HashMap<Integer, Double>();
        List <Map.Entry<Integer,Double>> knnEntryList =new LinkedList<Map.Entry<Integer, Double>>();

        
        L1Fun l1fun = new L1Fun();
        l1fun.init();
        
        // loop to rebuild each region
        for (int j = 0; j< regionTotalCount; j++){
//            for (int j = 0; j< 20; j++){
            log("**************************************************************************************************" );
            log("********************************************************" );
            log("re-construct regionId:" +j);
            log("********************************************************" );
            Region reconstructedRegion = matrix.get(j);

            // re-use List of Remaining Regions
            remainingRegions.clear();
            knnMap.clear();
            knnEntryList.clear();

            for(int i = 0; i<regionTotalCount; i++){
                if (i != j){
                    double dis= reconstructedRegion.getSim(matrix.get(i));
                    knnMap.put(i , dis);
                }
            }

            knnEntryList.addAll(knnMap.entrySet());

            Collections.sort(knnEntryList, Collections.reverseOrder(
                    new Comparator< Map.Entry<Integer,Double>>() {

                public int compare(Map.Entry<Integer,Double> firstMapEntry,
                                   Map.Entry<Integer,Double>secondMapEntry) {
                    return firstMapEntry.getValue().compareTo(secondMapEntry.getValue());
                }
            }));
            // knn, k = 1500
            int K = 1000;
            for (int i = 0; i <K; i++) {
                int regionIndex = knnEntryList.get(i).getKey();
                remainingRegions.add(matrix.get(regionIndex));
            }


//            for(int i = 0; i<regionTotalCount; i++){
//                if (i != j){
//                    remainingRegions.add(matrix.get(i));
//                }
//            }
            Collections.sort(remainingRegions, new Comparator<Region>() {

                        public int compare(Region firstRegion,
                                           Region secondRegion) {

                            if (firstRegion.imageId == secondRegion.imageId) {
                                return ((Integer) firstRegion.regionId).compareTo(secondRegion.regionId);
                            } else {
                                return ((Integer) firstRegion.imageId).compareTo(secondRegion.imageId);
                            }

                        }
                    }
            );

            TreeSet <Integer> imageIdSet = new TreeSet<Integer>();
            for(Region r: remainingRegions){
                imageIdSet.add(r.imageId);
            }
            int imageTotalCount = imageIdSet.size();

            double [][] enhancedA = generateEnhancedA(remainingRegions);

            double [] enhancedY = generateEnhancedY(reconstructedRegion, imageTotalCount+featureDemCount);

//            log("enhanced A :");
//            ArrayUtil.print2DArray(enhancedA);
//            log("enhanced Y :");
//            ArrayUtil.printArray(enhancedY);


            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log("start - solve matrix eq" + df.format(new Date()));
            double [] solutionX = l1fun.calcL1(enhancedA,enhancedY);
            log("end - solve matrix eq" + df.format(new Date()));

//          ArrayUtil.printArray(solutionX);
            List regionContributors = getContributorList(solutionX,remainingRegions);

            reconstructedRegion.labelPropagation(regionContributors);
            log("********************************************************" );
            log("region contributor count :"+regionContributors.size() );
            log("********************************************************" );

            //generate report on finishing every 100 regions
            if (j%20 == 0) {
                for(Region r : matrix){
                    r.selectLabel();
                }

                try {
                    generateReport();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for(Region r : matrix){
            r.selectLabel();
        }
    }

    public static void generateReport() throws IOException {

        log("generate report");

        File hisFile = new File("website/public/regionLabel/regionLabel.his");
        if(!hisFile.exists()) hisFile.createNewFile();
        FileWriter hisWriter = new FileWriter(hisFile);

        for ( Region r: matrix){
            String line= r.imageId+","+r.regionId;
            for(int i = -1; i <= 7; i++){
                line = line + " "+r.labelHistogram.get(i);
            }
            line = line + " " + r.getLabelId();
            hisWriter.write(line);
            hisWriter.write(System.lineSeparator());
        }

        hisWriter.close();
    }

    public static List<Region> getContributorList ( double [] solutionX, List<Region> candidateRegions ){

        int regionCount = candidateRegions.size();
        List<Region> contributors = new ArrayList<Region>();

        for(int i = 0; i < regionCount; i++){
            if(solutionX[i] != 0){
                candidateRegions.get(i).contribute = Math.abs(solutionX[i]);
                contributors.add(candidateRegions.get(i));
            }
        }
        return contributors;

    }

    public static double[][] generateEnhancedA(List<Region> remainingRegions){


        TreeSet <Integer> imageIdSet = new TreeSet<Integer>();
        for(Region r: remainingRegions){
            imageIdSet.add(r.imageId);
        }
        List <Integer>imageIdSeq = new ArrayList<Integer>(imageIdSet);

        // remaining region count
        int regionCount = remainingRegions.size();
        log("regionCount :"+regionCount);
        // feature dimension count
        int featureDemCount = remainingRegions.get(0).feature.size();
        log("featureDemCount :"+featureDemCount);
        // image count - since we keep image/region order in matrix, so the image-id of last region is leveraged.
        int image_count =  imageIdSeq.size();
        log("image_count :"+image_count);

        //array structure: (K+N, R+K+N)
        double enhancedA [][] ;
        enhancedA = new double [featureDemCount+image_count][regionCount+featureDemCount+image_count];
        log("row count of enhanced A :"+enhancedA.length);
        log("column count of enhanced A :"+enhancedA[0].length);

        int columnID = 0;
        // for first N(remaining region count) columns, 1 region is 1 column
//        log("Generating first N Columns which is (A+B) format");
        for( ; columnID < regionCount; columnID++){
//            log("**** each region *****");
//            log("column id :"+columnID);
            // get empty column data
            Region eachRegion = remainingRegions.get(columnID);
//            log("region imageId :"+eachRegion.imageId);
//            log("region regionId :"+eachRegion.regionId);
//            log("region weight :"+eachRegion.weightInImage);

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
            int imageSeqNum = imageIdSeq.indexOf(eachRegion.imageId);
            weightArray[imageSeqNum] = eachRegion.weightInImage;
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
            r.labelHistogram.put(i,0d);
        }
        for(int labelId : labels){
            r.labelHistogram.put(labelId,1d);
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
        r5.addLabel(3, 1.0);
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


}