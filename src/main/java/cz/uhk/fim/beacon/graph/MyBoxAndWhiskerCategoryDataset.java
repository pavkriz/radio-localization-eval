package cz.uhk.fim.beacon.graph;

import cz.uhk.fim.beacon.stats.NumberValue;
import java.io.File;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Kriz on 28. 12. 2015.
 */
public class MyBoxAndWhiskerCategoryDataset extends DefaultBoxAndWhiskerCategoryDataset {
    Map<String, List<Number>> columns = new TreeMap<>();

    @Override
    public void add(List list, Comparable rowKey, Comparable columnKey) {
        super.add(list, rowKey, columnKey);
        columns.put(rowKey.toString() + columnKey.toString(), list);
    }

    public void saveColumns(String filenamePrefix, String filenameSuffix) {      
        for (String group : columns.keySet()) {
            File file = new File(filenamePrefix + group + filenameSuffix);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            
            try (PrintWriter pw = new PrintWriter(filenamePrefix + group + filenameSuffix)) {
                columns.get(group).forEach(pw::println);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveColumnCharacteristics(String filenamePrefix, String filenameSuffix, String rowKey, int minColKey, int maxColKey) {
        File file = new File(filenamePrefix + filenameSuffix);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        
        try (PrintWriter pw = new PrintWriter(filenamePrefix + filenameSuffix)) {
            for (int i = minColKey; i <= maxColKey; i++) {
                BoxAndWhiskerItem item = (BoxAndWhiskerItem)this.data.getObject(rowKey, String.valueOf(i));
                pw.println(i + ";" + item.getMinOutlier() + ";" + item.getMinRegularValue() + ";" + item.getMedian() + ";" + item.getMaxRegularValue() + ";" + item.getMaxOutlier());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
