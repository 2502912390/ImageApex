package imageapex.show.java.model;

import com.jfoenix.controls.JFXSnackbar;
import imageapex.show.java.controllers.DisplayWindowController;
import imageapex.main.java.controllers.ControllerInstance;
import imageapex.main.java.model.ImageModel;

import java.io.IOException;
import java.util.ArrayList;

public class SwitchImages {//处理上下图片切换类

    private ArrayList<ImageModel> ilist;

    JFXSnackbar snackbar;
    DisplayWindowController dw;

    public SwitchImages() {
        dw = (DisplayWindowController) ControllerInstance.controllers.get(DisplayWindowController.class.getSimpleName());
        snackbar = new JFXSnackbar(dw.getRootPane());
    }

    public SwitchImages(ArrayList<ImageModel> ilist) {
        this();
        this.ilist = ilist;
    }

    //返回下一张照片
    public ImageModel nextImage(ImageModel im) throws IOException {
        int i = 0;
        for (i = 0; i < ilist.size(); i++) {
            if (ilist.get(i).getImageName().equals(im.getImageName())) {
                if (i == ilist.size() - 1) {
                    System.out.println("已到达最后一张, 正在查看第一张");
                    snackbar.enqueue(new JFXSnackbar.SnackbarEvent("正在查看第一张"));
                }

                break;
            }
        }
        return ilist.get((i + 1) % (ilist.size()));

    }

    //返回上一张照片
    public ImageModel lastImage(ImageModel im) throws IOException {
        int i = 0;
        for (i = 0; i < ilist.size(); i++) {
            if (ilist.get(i).getImageName().equals(im.getImageName())) {
                if (i == 0) {
                    System.out.println("已到达第一张照片, 正在查看最后一张");
                    snackbar.enqueue(new JFXSnackbar.SnackbarEvent("正在查看最后一张"));
                    i = ilist.size();
                }
                break;
            }
        }
        return ilist.get((i - 1) % (ilist.size()));

    }
}
