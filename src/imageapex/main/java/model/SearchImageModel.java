package imageapex.main.java.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SearchImageModel {//对imagelist下的所有图片查找类
    @Getter
    private static int foundNumbers = 0;//找到的图片数量


    public static ArrayList<ImageModel> fuzzySearch(String name, ArrayList<ImageModel> imageModelList) {//根据name不区分大小写地寻找匹配的图片并返回
        //对大小写不敏感，若敏感，去除CASE_INSENSITIVE
        Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        ArrayList<ImageModel> result = new ArrayList<ImageModel>();
        for (ImageModel im : imageModelList) {
            Matcher matcher = pattern.matcher(im.getImageName());
            if (matcher.find()) {
                foundNumbers++;
                result.add(im);
            }
        }
        //未找到
        return result;
    }


    public static ImageModel accurateSearch(String name, ArrayList<ImageModel> imageModelList) {//实现精准查找并返回一个ImageModel，需要文件的全称包括后缀
        //对大小写不敏感，若敏感，去除CASE_INSENSITIVE
        Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        for (ImageModel im : imageModelList) {
            Matcher matcher = pattern.matcher(im.getImageName());
            if (matcher.matches()) {
                foundNumbers += 1;
                return im;
            }
        }
        //未找到
        return null;
    }
}
