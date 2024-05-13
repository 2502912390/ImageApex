package imageapex.main.java.model;

import com.jfoenix.controls.JFXSnackbar;
import com.sun.jna.platform.FileUtils;
import imageapex.main.java.components.CustomDialog;
import imageapex.main.java.components.DialogType;
import imageapex.main.java.controllers.ControllerUtil;
import imageapex.main.java.controllers.HomeController;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.coobird.thumbnailator.Thumbnails;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class SelectedModel {//被选中照片操作类
    /**
     * 复制：如果遇到文件重复 -> 1.若是源文件夹与目的文件夹相同则重命名  2.若是在不同文件夹，可选择覆盖或跳过
     * 剪切：如果遇到文件重复 -> 直接覆盖
     * 重命名：如果遇到文件重复 -> 直接覆盖
     */

    @Getter
    private static Path sourcePath;//暂存需要操作的单张图片的路径

    @Getter
    private static ArrayList<Path> sourcePathList = new ArrayList<>();//保存已经选择的多张图片路径
    private static Path targetPath;//要复制、剪切到的目标路径

    @Setter
    @Getter
    private static int copyOrMove = -1; // 选择标志位 0->复制 1->剪切

    @Getter
    @Setter
    private static int singleOrMultiple = -1; // 选择单选/多选 0->单选 1->多选

    @Getter
    @Setter
    private static int waitingPasteNum = 0;

    @Getter
    @Setter
    private static int havePastedNum = 0;// 统计对文件的操作数量 复制+剪切

    private static int coverImage = 0;//统计覆盖了图片数量

    private static HomeController hc = (HomeController) ControllerUtil.controllers.get("HomeController");//获取home控制类 此处主要是用于更新粘贴/剪切后显示的图像列表

    //单选一张图片时 设置操作的源路径
    public static boolean setSourcePath(@NonNull ImageModel im) { //根据输入的图片/文件 设置源路径
        sourcePath = im.getImageFile().toPath();
        singleOrMultiple = 0;
        return true;
    }

    public static boolean setSourcePath(@NonNull File f) {
        sourcePath = f.toPath();
        singleOrMultiple = 0;
        return true;
    }

    public static boolean setSourcePath(String imagePath) {
        sourcePath = new File(imagePath).toPath();
        singleOrMultiple = 0;
        return true;
    }

    //多选图片时，设置操作的原路径列表
    public static boolean setSourcePath(ArrayList<ImageModel> imList) {
        sourcePathList.clear();    // 每次点击都需要清空List, 不创建对象以节约空间与时间
        for (ImageModel im : imList) {
            setSourcePath(im);
            sourcePathList.add(sourcePath);
        }
        singleOrMultiple = 1;
        return true;
    }


    public static boolean pasteImage(String path) {//根据单选图片/多选图片执行复制/剪切操作
        havePastedNum = 0;
        coverImage = 0;

        if (singleOrMultiple == 0) {//单选粘贴
            try {
                microPaste(path);
            } catch (IOException e) {
                System.err.println("粘贴失败");
                return false;
            }
        } else if (singleOrMultiple == 1) {//多选粘贴
            try {
                for (Path p : sourcePathList) {
                    sourcePath = p;
                    microPaste(path);
                }
            } catch (IOException e) {
                System.err.println("粘贴失败");
                return false;
            }
        }

        if (coverImage != 0) {
            hc.getSnackbar().enqueue(new JFXSnackbar.SnackbarEvent("覆盖了 " + coverImage + " 张图片"));
        }

        hc.refreshImagesList(hc.getSortComboBox().getValue());//更新显示的图像列表
        return true;
    }

    public static boolean replaceImage() {//将源路径（sourcePath）中的文件复制到目标路径（targetPath）
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("替换失败!");
            return false;
        }
        return true;
    }

    private static void microPaste(String path) throws IOException {// 根据copyOrMove复制、剪切操作
        if (copyOrMove == 0) {//复制
            if (getBeforePath().equals(path)) {//将该图片复制到当前文件夹
                boolean flag = false;//当前文件夹是否有重复图片标志位

                //获取当前文件夹下所有文件
                String[] flist = new File(path).list();
                String sourceFileName = sourcePath.getFileName().toString();

                // 当前文件夹有这个照片 重命名
                for (String s : flist) {
                    if (sourceFileName.equals(s) & !flag) {
                        targetPath = new File(suffixName(path, "_copy")).toPath();
                        flag = true;
                    }
                }

                //当前文件夹没有这个照片 根据当前路径构建目标路径
                if (!flag) {
                    targetPath = new File(otherPath(path)).toPath();
                }

                //复制文件
                Files.copy(sourcePath, targetPath);
                havePastedNum++;
            } else {//将该图片复制到别的文件夹
                targetPath = new File(otherPath(path)).toPath();

                if (!imageRepeat(path)) {// 没有重复的图片 直接复制
                    Files.copy(sourcePath, targetPath);
                    havePastedNum++;
                } else {//提示错误
                    show();
                }
            }
        } else if (copyOrMove == 1) {//剪切
            targetPath = new File(otherPath(path)).toPath();

            if (imageRepeat(path))//有重复
                coverImage++;
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);//不管目标路径是否有重复 直接覆盖
            havePastedNum++;

            if (havePastedNum == waitingPasteNum)
                copyOrMove = -1;  // 剪切完了以后就置 -1 -> 使得按粘贴键没反应
        }
    }

    private static boolean imageRepeat(String path) {//判断是否有重复图片
        String targetImageName = targetPath.getFileName().toString();
        try {
            if (SearchImageModel.accurateSearch(targetImageName, Objects.requireNonNull(ImageListModel.initImgList(path))) != null) {
                // 找到有重复的图片
                if (SearchImageModel.accurateSearch(targetImageName, Objects.requireNonNull(ImageListModel.initImgList(path))) != null) {
                    // 有重复图片
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private synchronized static void show() {
        ImageModel im = new ImageModel(targetPath.toFile());
        new CustomDialog(hc, DialogType.REPLACE, im,
                "替换或跳过文件",
                "\n目标已包含一个名为\"" + im.getImageName() + "\"的文件\n").show();
    }

    public static boolean renameImage(String newName) {//根据单选图片/多选图片执行重命名操作
        if (singleOrMultiple == 0) {//单张图片
            try {
                microRename(newName);
            } catch (IOException e) {
                System.err.println("重命名失败");
                return false;
            }
        } else if (singleOrMultiple == 1) {//多张图片 bug
            Path[] imArray = new Path[sourcePathList.size()];
            sourcePathList.toArray(imArray);//所选文件的所有文件名

            for (int i = 0; i < imArray.length; i++) {
                sourcePath = imArray[i];
                try {
                    String beforeName = newName.substring(0, newName.lastIndexOf("."));
                    String afterName = newName.substring(newName.lastIndexOf("."));

                    //wait to debug
                    microRename(beforeName + String.format("_%04d", i + 1) + afterName);
                } catch (IOException e) {
                    System.err.println("重命名失败");
                    return false;
                }
            }
        }
        singleOrMultiple = -1;
        return true;
    }

    private static void microRename(String newName) throws IOException {//重命名
        targetPath = new File(otherName(newName)).toPath();
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 4.删除图片选项
     *
     * @return 返回删除成功的图片个数
     */
    public static int deleteImage() {
        int success = 0;
        // 删除图片文件进入回收站，不直接删除
        if (singleOrMultiple == 0) {
            try {
                microDelete();
                success++;
            } catch (IOException e) {
                System.err.println("删除失败");
                return 0;
            }
        } else if (singleOrMultiple == 1) {
            for (Path p : sourcePathList) {
                sourcePath = p;
                try {
                    microDelete();
                    success++;
                } catch (IOException e) {
                    System.err.println("删除失败");
                    return 0;
                }
            }
        }
        singleOrMultiple = -1;
        return success;
    }

    // 删除的微操作
    private static void microDelete() throws IOException {
        FileUtils fileUtils = FileUtils.getInstance();
        if (fileUtils.hasTrash()) {
            fileUtils.moveToTrash(new File[]{(sourcePath.toFile())});
        }
    }


    /**
     * 5.压缩图片选项
     *
     * @param desSize 目标大小
     */
    // 压缩图片 desSize 目标字节数 最终压缩结果向1MB靠近
    public static int compressImage(int desSize) {
        if (singleOrMultiple == 0) {
            try {
                if (!microCompress(desSize))
                    return 0;
            } catch (IOException e) {
                System.err.println("压缩失败");
                return 0;
            }
            return 1;
        } else if (singleOrMultiple == 1) {
            int success = 0;
            for (Path p : sourcePathList) {
                sourcePath = p;
                try {
                    if (microCompress(desSize))
                        success++;
                } catch (IOException e) {
                    System.err.println("压缩失败");
                    return 0;
                }
            }
            return success;
        }
        singleOrMultiple = -1;
        return 0;
    }

    // 压缩图片微操作
    private static boolean microCompress(int desSize) throws IOException {
        byte[] imageBytes = GenUtilModel.getByteByFile(sourcePath.toFile());
        if (imageBytes == null || imageBytes.length < desSize * 1024) {
            // 不需要压缩了
            return false;
        }
        double accuracy = 0;
        if (imageBytes.length > desSize * 1024) {
            accuracy = getAccuracy(imageBytes.length / 1024.0);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(imageBytes.length);
            Thumbnails.of(sourcePath.toFile())
                    .scale(accuracy)  // 分辨率
                    .outputQuality(accuracy)  // 图片质量
                    .toOutputStream(bos);
//                        .toFile(newFile);  // 速度略慢
            imageBytes = bos.toByteArray();
        }
        String newImagePath = suffixName(getBeforePath(), "_only");
        File newFile = new File(newImagePath);
        return GenUtilModel.getFileByByte(imageBytes, newFile);
    }

    private static double getAccuracy(double imageSize) {
        double accuracy = 0;
        if (imageSize < 1024 * 2) {
            accuracy = 0.71;
        } else if (imageSize < 1024 * 4) {
            accuracy = 0.66;
        } else if (imageSize < 1024 * 8) {
            accuracy = 0.61;
        } else {
            accuracy = 0.59;
        }
        return accuracy;
    }

    /**
     * 文件名处理私有方法
     */
    // 检查路径后缀
    private static String checkPath(String path) {
        StringBuilder sb = new StringBuilder(32);
        if (!path.endsWith("\\")) {
            sb.append(path).append("\\");
        } else {
            sb.append(path);
        }
        return sb.toString();
    }

    // 获得图片绝对路径的前部分
    private static String getBeforePath() {
        String path = sourcePath.toString();
        return path.substring(0, path.lastIndexOf("\\"));
    }

    // 修改路径 复制/剪切
    private static String otherPath(String newPath) {
        StringBuilder sb = new StringBuilder(32);
        sb.append(checkPath(newPath)).append(sourcePath.getFileName().toString()); // 获得文件名
        return sb.toString();
    }

    // 修改名字 重命名
    private static String otherName(String newName) {
        StringBuilder sb = new StringBuilder(32);
        String path = sourcePath.toString().substring(0, sourcePath.toString().lastIndexOf("\\"));
        sb.append(path).append("\\").append(newName);
        return sb.toString();
    }

    // 分割.jpg后缀 处理名字前半部分冲突
    private static String suffixName(String newPath, String suffix) {
        StringBuilder sb = new StringBuilder(32);
        String sourceName = sourcePath.getFileName().toString();
        String nameBefore = sourceName.substring(0, sourceName.indexOf(".")); // 只有一个名字 没有.
        String nameAfter = sourceName.substring(sourceName.indexOf(".")); // 带有. .jpg
        sb.append(checkPath(newPath)).append(nameBefore).append(suffix).append(nameAfter);
        return sb.toString();
    }
}
