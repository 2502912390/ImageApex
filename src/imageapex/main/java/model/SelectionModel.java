package imageapex.main.java.model;

import com.jfoenix.effects.JFXDepthManager;
import imageapex.main.java.components.ImageBox;
import imageapex.main.java.controllers.ControllerInstance;
import imageapex.main.java.controllers.HomeController;
import lombok.Getter;

import java.util.ArrayList;

public class SelectionModel {//选择图片的工具类

    private static ArrayList<ImageBox> selection = new ArrayList<>(); //用于暂存选中的缩略图单元ImageBox，方便设置选中时的样式
    @Getter
    public static ArrayList<ImageModel> imageModelList = new ArrayList<>(); //存放选中的图片本身，作为后续复制粘贴等批量操作的源
    private static HomeController hc = (HomeController) ControllerInstance.controllers.get(HomeController.class.getSimpleName());

    public static void add(ImageBox node) {//设置选中的效果
//        if (!contains(node)){
            JFXDepthManager.setDepth(node, 4);
            node.getImageView2().setTranslateY(node.getImageView2().getTranslateY() - 5);
            selection.add(node);
            imageModelList.add(node.getIm());
            hc.selectedNumLabel.setText("| 已选中 " + selection.size() + " 张");
//        }
        log();
    }

    public static void remove(ImageBox node) {//设置取消选中的效果
        JFXDepthManager.setDepth(node, 0);
        node.getImageView2().setTranslateY(node.getImageView2().getTranslateY() + 5);
        selection.remove(node);
        imageModelList.remove(node.getIm());
        hc.selectedNumLabel.setText("| 已选中 " + selection.size() + " 张");
        log();
    }

    public static void clear() {//清空选中
        while (!selection.isEmpty()) {
//            remove(selection.iterator().next());
            selection.iterator().next().getCheckBox().setSelected(false);
        }
    }

    private static boolean contains(ImageBox node) {
        for (ImageBox ib : selection) {
            if (ib.equals(node))
                return true;
        }
        return false;
    }

    //控制台输出
    private static void log() {
        System.out.println("Items in list: " + selection);
    }
}
