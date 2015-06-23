/**
 * Created by lichaochen on 15/6/21.
 */
public class ArrayUtil {
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

    public static double[] getColumnFrom2DArray(double array[][], int column_id){

        int row_count = array.length;
        double [] columndata = new double[row_count];
        for (int i = 0 ; i < row_count; i++){
            columndata[i] = array[i][column_id];
        }
        return columndata;
    }

    public static void print2DArray (double array[][]){
        for(double[] row : array){
            String line = "" ;
            for(double item: row){
                line =  line + item + ",";
            }
            System.out.println(line);
        }
    }

    public static void printArray (double array[]){
        for (double item : array) {
            System.out.print(item+",");
        }
        System.out.println();
    }

    public static void printArray (Double array[]){
        for (double item : array) {
            System.out.print(item+",");
        }
        System.out.println();
    }


    public static void main(String args[]){

    }
}
