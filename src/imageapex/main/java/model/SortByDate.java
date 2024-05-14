package imageapex.main.java.model;

import java.util.Comparator;


public class SortByDate implements Comparator<ImageModel> {//按日期排序
    @Override
    public int compare(ImageModel o1, ImageModel o2) {
        return Long.compare(o1.getImageLastModified(), o2.getImageLastModified());
    }
}
