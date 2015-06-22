import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by lichaochen on 15/6/13.
 */
public class Region {

    int image_id;
    int region_id;

    /* weight: region size percentage in image */
    double weightInImage;

    /* final label ID, after label selection using label histogram*/
    int label_id;

    /*feature histogram*/
    List<Double> feature = new ArrayList<Double>();

    /*Label Histogram, structure <label_id, counter>, used for label propagation phase*/
    Map <Integer, Integer> label_histogram = new HashMap<Integer, Integer>();


    public Region(){
    }

    public Region (int image_id, int region_id, double weightInImage){
        this.image_id = image_id;
        this.region_id = region_id;
        this.weightInImage = weightInImage;
    }

    /*parse image mask file, to generate Regions of that image
      populate regions with image_id, region_id, weighInImage,
      those can be obtained from mask file */
    public static List<Region> parseMaskFile(String maskDirName) throws Exception{
        System.out.println("Start to parse mask directory......");
        List <Region> regions = new ArrayList<Region>();

        File maskDir = new File(maskDirName);
        File[] files = maskDir.listFiles();
        LinkedList<File> filelist = new LinkedList<File>();

        for (int i = 0; i < files.length; i ++){
            String fileName = files[i].getName();
            //过滤隐藏文件
            if (fileName.endsWith("mask")) {
                filelist.add(files[i]);
            }
        }
        System.out.println("Total " + filelist.size() + "mask files.");

        //按文件名（图像名）排序
        Collections.sort(filelist, new Comparator<File>(){
            @Override
            public int compare(File o1, File o2) {
                int file1Name = Integer.parseInt(o1.getName().split("\\.")[0]);
                int file2Name = Integer.parseInt(o2.getName().split("\\.")[0]);
                if(file1Name < file2Name)
                    return -1;
                if(file1Name > file2Name)
                    return 1;
                return 0;
            }
        });

        for (File file: filelist) {
            int imageID = Integer.parseInt(file.getName().split("\\.")[0]);
            System.out.println("Parsing " + imageID + ".mask .....");
            List<String[]> fileContent = new ArrayList<String[]>();
            HashMap<Integer, Integer> regionStatic = new HashMap<Integer, Integer>();
            int rowNumber = 0;
            int colNumber = 0;

            FileInputStream is = new FileInputStream(file);
            BufferedReader cacheReader = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                while ((line = cacheReader.readLine()) != null) {
                    String[] rowContent = line.split(" ");
                    fileContent.add(rowContent);
                    rowNumber++;
                }
            } finally {
                cacheReader.close();
            }

            colNumber = fileContent.get(0).length;
            int imageSize = rowNumber * colNumber;

            for (int row = 0; row < rowNumber - 1; row++) {
                for (int col = 0; col < colNumber - 1; col++) {
                    int regionID = Integer.parseInt(fileContent.get(row)[col]);
                    if (regionStatic.get(regionID) == null){
                        regionStatic.put(regionID, 0);
                    }
                    regionStatic.put(regionID, regionStatic.get(regionID) + 1);
                }
            }

            for (Map.Entry<Integer, Integer> entry : regionStatic.entrySet()) {
                double weight = (double)entry.getValue() / (double) imageSize;
                Region region = new Region(imageID, entry.getKey(), weight);
                regions.add(region);
            }
        }
        return regions;
    }

    /*parse feature histogram file, populate feature according to the file*/
    public static void importFeature (String featureFile,  List<Region> regions){


    }

    /*parse image label file, populate initial label histogram of each regions in the image*/
    public static void importImageLable(String labelFile,  List<Region> regions){


    }


    public int getImage_id() {
        return image_id;
    }

    public int getRegion_id() {
        return region_id;
    }

    public double getWeightInImage() {
        return weightInImage;
    }

    public int getLabel_id() {
        return label_id;
    }

    public void labelPropagation(List <Region> contributors){
        for (Integer label_id : this.label_histogram.keySet()){
            for (Region contributor : contributors){
                if (isCommonLabel(label_id, contributor)){
                    this.addLabel(label_id);
                    contributor.addLabel(label_id);
                }
            }
        }
    }

    public void addLabel(Integer label_id){
        int label_count = this.label_histogram.get(label_id);

        this.label_histogram.put(label_id, label_count+1);
    }

    public boolean isLabelContained(Integer label_id){
        return (this.label_histogram.get(label_id) > 0 );
    }

    public boolean isCommonLabel(Integer label_id, Region another){
        return (this.isLabelContained(label_id) && another.isLabelContained(label_id));
    }




    public static void main(String args[]) throws Exception{
        //Region r1 =  new Region(1, 4, 0.3);
        //System.out.println(r1.image_id);
        String maskDir = System.getProperty("user.dir") + "/mask";
        parseMaskFile(maskDir);
    }

}
