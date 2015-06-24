import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by lichaochen on 15/6/13.
 */
public class Region {

    int imageId;
    int regionId;

    /* weight: region size percentage in image */
    double weightInImage;

    double contribute;

    /* final label ID, after label selection using label histogram*/
    int labelId;

    /*feature histogram*/
    List<Double> feature = new ArrayList<Double>();

    /*Label Histogram, structure <labelId, counter>, used for label propagation phase*/
    Map<Integer, Double> labelHistogram = new HashMap<Integer, Double>();


    public Region(){
    }

    public Region (int image_id, int region_id, double weightInImage){
        this.imageId = image_id;
        this.regionId = region_id;
        this.weightInImage = weightInImage;
    }

    public double getFeatureDistance(Region another){
            double distance = 0;

            for (int i = 0;  i< this.feature.size(); i++){
                distance = distance+ (feature.get(i) - another.feature.get(i))*(feature.get(i) - another.feature.get(i));
            }

            distance = Math.sqrt(distance);

        return distance;
    }

    public void setFeature(List<Double> feature) {
        this.feature = feature;
    }

    /*parse image mask file, to generate Regions of that image
          populate regions with imageId, regionId, weighInImage,
          those can be obtained from mask file */
    public static List<Region> parseMaskAndFeature(String maskDirName, String featureDirName) throws Exception{
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
            File featureFile = new File(featureDirName + "/" + imageID + ".his");
            HashMap<Integer, List<Double>> featureMap = importFeature(featureFile);

            System.out.println("Parsing image " + imageID + ".....");
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
                int regionID = entry.getKey();
                Region region = new Region(imageID, regionID, weight);
                region.setFeature(featureMap.get(regionID));
                regions.add(region);
            }
        }
        System.out.println("Finished to parse mask and feature files");
        return regions;
    }



    /*parse feature histogram file, populate feature according to the file*/
    public static HashMap<Integer, List<Double>> importFeature (File featureFile) throws Exception{
        HashMap<Integer, List<Double>> featureMap = new HashMap<Integer, List<Double>>();
        FileInputStream is = new FileInputStream(featureFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                List<Double> featureVector = new ArrayList<Double>();
                ArrayList<String> regionInfo = new ArrayList<String>(Arrays.asList(line.split(" ")));
                int regionID = Integer.parseInt(regionInfo.remove(0));
                for (String feature: regionInfo){
                    featureVector.add(Double.parseDouble(feature));
                }
                featureMap.put(regionID, featureVector);
            }
        } finally {
            bufferedReader.close();
        }
        return featureMap;
    }

    /*parse image label file, populate initial label histogram of each regions in the image*/
    public static void importImageLable(String labelFile,  List<Region> regions) throws Exception{
        System.out.println("Start to import image label as init region label histogram....");
        File imageLableFile = new File(labelFile);

        int imageID = 0;
        FileInputStream is = new FileInputStream(imageLableFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                imageID ++;
                System.out.println("Processing image " + imageID + "'s label....." );
                Map <Integer, Double> labelHistogram = new HashMap<Integer, Double>();
                String[] labels = line.split(" ");
                for (String labelID : labels){
                    labelHistogram.put(Integer.parseInt(labelID), 1d);
                }
                for(int i = -1; i < 8; i ++){
                    if (labelHistogram.get(i) == null) {
                        labelHistogram.put(i, 0d);
                    }
                }
                for (Region region: regions) {
                    if (region.getImageId() == imageID) {
                       // region.setLabelHistogram(labelHistogram);
                        region.setLabelHistogram(new HashMap<Integer, Double>(labelHistogram));

                    }
                }
            }
        } finally {
            bufferedReader.close();
        }
        System.out.println("Finished to import image label histogram.");

    }


    public int getImageId() {
        return imageId;
    }

    public int getRegionId() {
        return regionId;
    }

    public double getWeightInImage() {
        return weightInImage;
    }

    public int getLabelId() {
        return labelId;
    }

    public void setLabelHistogram(Map<Integer, Double> labelHistogram) {
        this.labelHistogram = labelHistogram;
    }

    public void labelPropagation(List <Region> contributors){


        for (Integer label_id : this.labelHistogram.keySet()){
            double eachLabelContributeFromOthers = 0;

            for (Region contributor : contributors){
                if (isCommonLabel(label_id, contributor)){
                    //        update others
                    contributor.addLabel(label_id, contributor.contribute);
                    eachLabelContributeFromOthers = eachLabelContributeFromOthers+ contributor.contribute*contributor.weightInImage;
                }

            }
            //        update myself
            this.addLabel(label_id,eachLabelContributeFromOthers);
        }



    }

    public void addLabel(Integer label_id, Double contribute){
        Double label_count = this.labelHistogram.get(label_id);

        this.labelHistogram.put(label_id, label_count + contribute);
    }

    public boolean isLabelContained(Integer label_id){
        return (this.labelHistogram.get(label_id) > 0 );
    }

    public boolean isCommonLabel(Integer label_id, Region another){
        return (this.isLabelContained(label_id) && another.isLabelContained(label_id));
    }

    public void selectLabel(){
        Double max_count = 0d;
        int highest_rank_label=0;
        for(int eachLabel : this.labelHistogram.keySet()){
            if (this.labelHistogram.get(eachLabel) > max_count) {
                max_count =this.labelHistogram.get(eachLabel);
                highest_rank_label = eachLabel;
            }
        }

        this.labelId = highest_rank_label;

    }


    public static void main(String args[]) throws Exception{
        //Region r1 =  new Region(1, 4, 0.3);
        //System.out.println(r1.imageId);
        String projectDirName = System.getProperty("user.dir");

        String maskDirName =  projectDirName + "/mask";
        String featureDirName = projectDirName + "/histogram";
        List<Region> regions = parseMaskAndFeature(maskDirName, featureDirName);
        String labelFileName = projectDirName + "/imageLabels.txt";
        importImageLable(labelFileName, regions);
        RegionMatrix.log("hello");
    }

}
