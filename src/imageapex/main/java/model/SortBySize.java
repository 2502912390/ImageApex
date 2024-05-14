package imageapex.main.java.model;

import java.util.Comparator;

public class SortBySize implements Comparator<ImageModel> {//按大小排序
    @Override
    public int compare(ImageModel o1, ImageModel o2) {
        return Long.compare(o1.getFileLength(), o2.getFileLength());
    }
}
