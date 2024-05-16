package imageapex.main.java.model;

import lombok.Data;

import java.io.File;


@Data
public class ImageModel {//单元图片类

    private String imageFilePath;   // 绝对路径
    private String imageParentPath; // 图片所在文件夹路径
    private File imageFile;     //File文件
    private String imageName;   //文件名字
    private String imageNameNoExt;  // 不含拓展名的名字 不含.jpg png等
    private String imageType;  //文件类型 jpg png。。。

    private long fileLength;  //文件大小
    private long imageLastModified; // 图片修改时间

    public ImageModel(File file) {//根据File文件构造类
        this.imageFile = file;
        this.imageFilePath = file.getAbsolutePath();
        this.imageParentPath = file.getParent();
        this.imageName = file.getName();
        this.imageNameNoExt = imageName.substring(0, imageName.lastIndexOf("."));
        this.imageType = imageName.substring(imageName.indexOf(".") + 1).toLowerCase();
        this.fileLength = file.length();
        this.imageLastModified = file.lastModified();
    }

    public ImageModel(String path) {//根据string路径构造类
        this.imageFilePath = path;
        this.imageFile = new File(path);
        this.imageParentPath = imageFile.getParent();
        this.imageName = imageFile.getName();
        this.imageNameNoExt = imageName.substring(0, imageName.lastIndexOf("."));
        this.imageType = imageName.substring(imageName.indexOf(".") + 1).toLowerCase();
        this.fileLength = imageFile.length(); // 返回的单位是byte
        this.imageLastModified = imageFile.lastModified();
    }

    public String getFormatSize() {//获取照片的大小string
        return GenUtilModel.getFormatSize(this.fileLength);
    }

    public String getFormatTime() {//获取最后的修改时间string
        return GenUtilModel.getFormatTime(this.imageLastModified);
    }
}
