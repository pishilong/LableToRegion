


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lichaochen on 15/6/17.
 */
public class RegionMatrix {

    public static List <Region> matrix= new ArrayList<Region> ();

    public static void setupMatrix(){
        /* loop each mask file, to generate region Matrix*/
        List <Region> regions = Region.parseMaskFile("imageMask-file");
        Region.importImageLable("imageLabel-file", regions);
        Region.importFeature("feature-histogram-file", regions);
        matrix.addAll(regions);

    }
    public static void analyzeLabelToRegion()    {

        int regionTotalCount = matrix.size();
        List <Region>remainingRegions = new ArrayList<Region>();
        // loop to rebuild each region
        for (int j = 0; j< regionTotalCount; j++){
            // re-use List of Remaining Regions
            remainingRegions.clear();

            for(int i = 0; i<regionTotalCount; i++){
                if (i != j){
                    remainingRegions.add(matrix.get(i));
                }
            }
        }

    }


    public static double[][] generateEnhancedA(List<Region> remainingRegions){


        // remaining region count
        int regionCount = remainingRegions.size();
        log("regionCount :"+regionCount);
        // feature dimension count
        int featureDemCount = remainingRegions.get(0).feature.size();
        log("featureDemCount :"+featureDemCount);
        // image count - since we keep image/region order in matrix, so the image-id of last region is leveraged.
        int image_count =  remainingRegions.get(remainingRegions.size()-1).getImage_id();
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
            log("region image_id :"+eachRegion.image_id);
            log("region region_id :"+eachRegion.region_id);
            log("region weight :"+eachRegion.weightInImage);

            double [] columnData = ArrayUtil.getColumnFrom2DArray(enhancedA, columnID);
            log("initial columndata : ");
            ArrayUtil.printArray(columnData);

            // setup feature array
            Double featureArray [] =new Double [featureDemCount] ;
            eachRegion.feature.toArray(featureArray);
            log("feature part columndata : ");
            ArrayUtil.printArray(featureArray);

            // setup weight array
            double weightArray [] =  new double [image_count];
            weightArray[eachRegion.image_id-1] = eachRegion.weightInImage;
            log("weight part columndata : ");
            ArrayUtil.printArray(weightArray);

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
            log("column data (A+B) :");
            ArrayUtil.printArray(columnData);
            ArrayUtil.populate2DArrayByColumn(enhancedA, columnID, columnData);

        }
        log("Finish generating first N Columns which is (A+B) format, enhancedA: ");
        ArrayUtil.print2DArray(enhancedA);

        log("*** Generate next K (feature dimension count) columns which (1+0) format");
        // for next K (feature dimension count) columns
        // for each column, first K (feature dimension count) column data is 1, remaining N (image_count)column data is 0
        int id_nextK = 1;
        for( ; id_nextK <= featureDemCount; id_nextK++ , columnID++){
            double [] columnData = ArrayUtil.getColumnFrom2DArray(enhancedA, columnID );

            for (int i =0; i<featureDemCount; i++){
                columnData[i] = 1;
            }

            log("column data (1 0)");
            ArrayUtil.printArray(columnData);
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
            log("column data (0 -1)");
            ArrayUtil.printArray(columnData);
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


    public static void main(String args[]){


        testY();

        testA();

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



    public static void testA(){

        List<Region> list = new ArrayList<Region>();

        Region r1 = new Region(1, 0, 0.35);
        randomFeature(r1.feature,100);
        Region r2 = new Region(1, 1, 0.55);
        randomFeature(r2.feature,100);
        Region r3 = new Region(1, 2, 0.10);
        randomFeature(r3.feature,100);
        Region r4 = new Region(2, 0, 0.40);
        randomFeature(r4.feature,100);
        Region r5 = new Region(2, 1, 0.60);
        randomFeature(r5.feature,100);

        list.add(r1);
        list.add(r2);
        list.add(r3);
        list.add(r4);
        list.add(r5);

        double[][] testA_matrix = generateEnhancedA(list);
        ArrayUtil.print2DArray(testA_matrix);



    }


}