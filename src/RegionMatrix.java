import java.util.ArrayList;
import java.util.List;

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


    public static void populate2DArrayByColumn(double array[][], int column_id, int offset, double column_data[]){

        int row_count = column_data.length;

        // populate value staring from row[offset]
        for (int row_id =0; row_id< row_count ; row_id++){
            double[] eachRow = array[offset + row_id];
            eachRow[column_id] = column_data[row_id];
        }

    }


    public static void populate2DArrayByColumn(double array[][], int column_id, double column_data[]){

        populate2DArrayByColumn( array, column_id, 0, column_data);
    }


    public static double[] getColumnFromArray(double array[][], int column_id){

        int row_count = array.length;
        double [] columndata = new double[row_count];
        for (int i = 0 ; i < row_count; i++){
            columndata[i] = array[i][column_id];
        }
        return columndata;

    }

    public static double[][] generateEnhancedA(List<Region> remainingRegions){


        // remaining region count
        int regionCount = remainingRegions.size();
        // feature dimension count
        int featureDemCount = remainingRegions.get(0).feature.size();
        // image count - since we keep image/region order in matrix, so the image-id of last region is leveraged.
        int image_count =  remainingRegions.get(remainingRegions.size()-1).getImage_id();

        //array structure: (K+N, R+K+N)
        double enhancedA [][] ;
        enhancedA = new double [featureDemCount+image_count][regionCount+featureDemCount+image_count];

        int columnID = 0;
        // for first N(remaining region count) columns, 1 region is 1 column
        for( ; columnID < regionCount; columnID++){

            // get empty column data
            Region eachRegion = remainingRegions.get(columnID);
            double [] columnData = getColumnFromArray(enhancedA,columnID);

            // setup feature array
            Double featureArray [] = (Double []) eachRegion.feature.toArray();

            // setup weight array
            double weightArray [] =  new double [image_count];
            weightArray[eachRegion.image_id-1] = eachRegion.weightInImage;

            // load feature & weight into the column data
            for (int i=0, j=0, k=0; i < columnData.length; i++){

                if (j<featureArray.length){
                    columnData[i] = featureArray[j];
                    j++;
                    i++;
                }
                if (k<weightArray.length){
                    columnData[i] = weightArray[k];
                    k++;
                    i++;
                }

            }

            populate2DArrayByColumn(enhancedA,columnID,columnData);
        }
        // for next K (feature dimension count) columns
        // for each column, first K (feature dimension count) column data is 1, remaining N (image_count)column data is 0
        int id_nextK = 1;
        for( ; id_nextK <= featureDemCount; id_nextK++){
            double [] columnData = getColumnFromArray(enhancedA,columnID+id_nextK);

            for (int i =0; i<featureDemCount; i++){
                columnData[i] = 1;
            }

        }
        columnID = columnID+id_nextK;

        // for next N (image_count) columns
        // for each column, first K (feature dimension count) column data is 0, remaining N (image_count)column data is -1

        int id_nextN = 1;
        for( ; id_nextN <= image_count; id_nextN++){
            double [] columnData = getColumnFromArray(enhancedA,columnID+id_nextN);

            for (int i = featureDemCount; i<columnData.length; i++){
                columnData[i] = -1;
            }

        }
        // at this moment, columnID should be the last column index of the Matrix
        columnID = columnID+id_nextN;

        return enhancedA;


    }

    public static void test (double array []){

    }

    public static void main(String args[]){
        List test = new ArrayList();
        test.add("hello");
        test.add("world");

        System.out.println(test.get(0));

        double testmatrix [][] = {{2,3,4,5},{13,423,23,23},{4,8,12,24}};
        System.out.println(testmatrix.length);

        for(double[] row : testmatrix){
            String line = "" ;
            for(double item: row){
                line =  line + item + ",";
            }
            System.out.println(line);
        }
        double column[] = {100,200};
        populate2DArrayByColumn(testmatrix, 2, 1, column);

        for(double[] row : testmatrix){
            String line = "" ;
            for(double item: row){
                line =  line + item + ",";
            }
            System.out.println(line);
        }

        for (double item : getColumnFromArray(testmatrix,2)) {
            System.out.print(item+",");
        }





    }


}