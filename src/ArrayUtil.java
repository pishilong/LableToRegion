import java.util.*;

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

        String arr[][] = {{"1","2","3"},{"4","5","6"},{"7","8","9"}};
        System.out.println(Arrays.deepToString(arr));
        System.out.println(Arrays.toString(arr));

        HashMap<Double,Integer> map = new HashMap<Double, Integer>();
        map.put(1.3, 1);
        map.put(2.5, 2);
        map.put(0.3, 3);
        map.put(1.2, 4);
        List <Map.Entry<Double,Integer>> mHashMapEntryList=new LinkedList<Map.Entry<Double, Integer>>(map.entrySet());



        Collections.sort(mHashMapEntryList, new Comparator< Map.Entry<Double,Integer>>() {

            public int compare(Map.Entry<Double,Integer> firstMapEntry,
                               Map.Entry<Double,Integer>secondMapEntry) {
                return firstMapEntry.getKey().compareTo(secondMapEntry.getKey());
            }
        });

        for (int i = 0; i < mHashMapEntryList.size(); i++) {
            System.out.println(mHashMapEntryList.get(i));
        }

        TreeSet <Integer> imageset = new TreeSet<Integer>();
        imageset.add(5);
        imageset.add(3);
        imageset.add(10);
        imageset.add(3);

        for(Integer i: imageset){
            System.out.println(i);
        }

        List list = new ArrayList(imageset);
        System.out.println(list.indexOf(5));
        System.out.println(list.indexOf(3));






    }
}
