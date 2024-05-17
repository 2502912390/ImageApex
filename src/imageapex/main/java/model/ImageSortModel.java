package imageapex.main.java.model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;

@Data
public class ImageSortModel {//对多张图像的操作类

    public static boolean isFormalImage(String fileName) { // 判断文件是否为图片 支持jpg/jpeg/png/gif/bmp
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                fileName.endsWith(".bmp");
    }

    // 初始化图片列表
    public static ArrayList<ImageModel> createImageList(String path) throws IOException {// 根据输入的路径 返回该路径下所有的 ImageModel
        ArrayList<ImageModel> imageList = new ArrayList<>(); // 默认根据name进行排序
        if (path.equals("") || path == null)
            return null;
        //遍历文件树
        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                //获取文件名 转小写 并判断是否是所需文件 是则创建一个ImageModel对象加入imgList
                String fileName = file.getFileName().toString().toLowerCase();
                if (isFormalImage(fileName)) {
                    imageList.add(new ImageModel(file.toString())); // 获取绝对路径
                }
                return FileVisitResult.CONTINUE;
            }

            // 只访问当前文件夹 不进行递归访问
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.toString().equals(path)) {
                    return FileVisitResult.CONTINUE;
                } else
                    return FileVisitResult.SKIP_SUBTREE;
            }

            // 处理访问系统文件的异常
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
        return imageList;
    }


    public static int getListImageNum(ArrayList<ImageModel> im) {// 返回文件夹内图片数量
        return im.size();
    }


    public static String getListImageSize(ArrayList<ImageModel> im) {// 返回文件夹内的图片总大小
        long totalSize = 0;
        for (ImageModel i : im) {
            totalSize += i.getFileLength();
        }
        return FormatModel.getFormatSize(totalSize);
    }

    // 刷新文件夹 返回新列表
    public static ArrayList<ImageModel> renewList(String path) {// 根据输入路径 刷新文件夹 返回新列表
        ArrayList<ImageModel> list;
        try {
            list = createImageList(path);
        } catch (IOException e) {
            return null;
        }
        return list;
    }


    public static ArrayList<ImageModel> renewList(String path, String mode) {// 带排序的刷新 对文件夹排序 mode->排序模式
        ArrayList<ImageModel> list = renewList(path);
        switch (mode) {
            case SortParam.SBND://按名称降序 翻转即可
                assert list != null;
                Collections.reverse(list);
                return list;
            case SortParam.SBSR://按大小升序
                assert list != null;
                list.sort(new SortBySize());
                return list;
            case SortParam.SBSD://按大小降序
                assert list != null;
                list.sort(new SortBySize());
                Collections.reverse(list);
                return list;
            case SortParam.SBDR://按修改日期升序
                assert list != null;
                list.sort(new SortByDate());
                return list;
            case SortParam.SBDD://按修改日期降序
                assert list != null;
                list.sort(new SortByDate());
                Collections.reverse(list);
                return list;
        }
        return list;
    }
}