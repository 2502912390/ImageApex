package imageapex.main.java.controllers;

import com.jfoenix.controls.*;
import com.jfoenix.controls.JFXListView;
import imageapex.main.java.components.DialogBox;
import imageapex.main.java.components.DialogType;
import imageapex.main.java.components.ImageBox;
import imageapex.main.java.model.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import lombok.Getter;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PopupMenuController implements Initializable {//缩略图右键菜单控制器
    @FXML
    private JFXListView<?> popupList;

    private ImageModel im;//图片
    private ImageBox imageBox;//缩略图
    private HomeController hc;

    @Getter
    private JFXSnackbar snackbar;//显示临时消息

    public PopupMenuController() {
        //将本类的实例添加到全局映射中
        ControllerInstance.controllers.put(this.getClass().getSimpleName(), this);
        //获取HomeController实例
        hc = (HomeController) ControllerInstance.controllers.get(HomeController.class.getSimpleName());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //信息条初始化
        snackbar = new JFXSnackbar(hc.getRootPane());
    }

    public PopupMenuController(ImageBox imageBox) {
        this();
        this.imageBox = imageBox;
        this.im = imageBox.getIm();
    }

    @SuppressWarnings("unused")
    @FXML
    private void action() {//根据缩略图右键操作选择弹出对应提示信息
        ArrayList<ImageModel> sourceList = SelectionModel.getImageModelList();
        switch (popupList.getSelectionModel().getSelectedIndex()) {
            case 0://复制
                if (sourceList.isEmpty()) {//设置需要处理的路径和数量
                    SelectedModel.setSourcePath(im.getImageFilePath());
                    SelectedModel.setWaitingPasteNum(1);
                } else {
                    SelectedModel.setSourcePath(sourceList);
                    SelectedModel.setWaitingPasteNum(sourceList.size());
                }
                //设置粘贴模式
                SelectedModel.setCopyOrCut(0);
                //粘贴按钮可见
                hc.getPasteButton().setDisable(false);
                snackbar.enqueue(new JFXSnackbar.SnackbarEvent("已复制到剪贴板"));
                imageBox.getPopUpMenu().hide();
                break;
            case 1://剪切
                if (sourceList.isEmpty()) {//设置需要处理的路径和数量
                    SelectedModel.setSourcePath(im.getImageFilePath());
                    SelectedModel.setWaitingPasteNum(1);
                } else {
                    SelectedModel.setSourcePath(sourceList);
                    SelectedModel.setWaitingPasteNum(sourceList.size());
                }
                //设置剪切模式
                SelectedModel.setCopyOrCut(1);

                hc.getPasteButton().setDisable(false);
                snackbar.enqueue(new JFXSnackbar.SnackbarEvent("已剪切到剪贴板"));
                imageBox.getPopUpMenu().hide();
                break;
            //重命名
            case 2:
                if (sourceList.isEmpty()) {
                    SelectedModel.setSourcePath(im.getImageFilePath());
                } else {
                    SelectedModel.setSourcePath(sourceList);
                }

                new DialogBox(hc, DialogType.RENAME, im, "重命名图片").show();
                imageBox.getPopUpMenu().hide();
                break;
            //压缩
            case 3:
                imageBox.getPopUpMenu().hide();

                if (sourceList.isEmpty()) {
                    SelectedModel.setSourcePath(im.getImageFilePath());
                } else {
                    SelectedModel.setSourcePath(sourceList);
                }
                int success = SelectedModel.compressImage(800);

                hc.refreshImagesList(hc.getSortComboBox().getValue());
                if (success != 0) snackbar.enqueue(new JFXSnackbar.SnackbarEvent("已压缩" + success + "张图片并创建副本"));
                else snackbar.enqueue(new JFXSnackbar.SnackbarEvent(" 没有图片进行压缩 \n压缩条件:大于800KB"));
                break;

            //拼接
            case 4:
                if (sourceList.isEmpty() || sourceList.size() == 1) {
                    //未选择或只选了一张图片
                    snackbar.enqueue(new JFXSnackbar.SnackbarEvent("请选择两张或以上图片进行拼接"));
                } else {
                    new DialogBox(hc, DialogType.CHOICE, im, "选择拼接方式",sourceList).show();
//                    System.out.println("******");//for_test
                }

                imageBox.getPopUpMenu().hide();
                break;

            //删除
            case 5:
                if (sourceList.isEmpty()) {
                    SelectedModel.setSourcePath(im.getImageFilePath());
                    new DialogBox(hc, DialogType.DELETE, im,
                            "确认删除",
                            "要删除文件：" + im.getImageName() + " 吗？\n\n你可以在回收站处找回。").show();
                } else {
                    SelectedModel.setSourcePath(sourceList);
                    new DialogBox(hc, DialogType.DELETE, im,
                            "确认删除",
                            "要删除这" + sourceList.size() + "个文件吗？\n\n你可以在回收站处找回。").show();
                }
                imageBox.getPopUpMenu().hide();
                break;

            //属性
            case 6:
                Image image = new Image(im.getImageFile().toURI().toString());
                StringBuilder info = new StringBuilder();

                if (sourceList.isEmpty() || sourceList.size() == 1) {
                    info.append("尺寸：").append(image.getWidth()).append(" × ").append(image.getHeight()).append("\n");
                    info.append("类型：").append(im.getImageType().toUpperCase()).append("\n");
                    info.append("大小：").append(im.getFormatSize()).append("\n");
                    info.append("日期：").append(im.getFormatTime()).append("\n");
                    info.append("\n位置：").append(im.getImageFilePath());
                    new DialogBox(hc, DialogType.INFO, im,
                            im.getImageName(), info.toString()).show();
                } else {
                    info.append("数量：").append(sourceList.size()).append(" 个\n");
                    long totalSize = 0;
                    for (ImageModel im : sourceList) {
                        totalSize += im.getFileLength();
                    }
                    info.append("大小：").append(FormatModel.getFormatSize(totalSize)).append("\n");
                    info.append("位置：").append(im.getImageParentPath()).append("\n");
                    DialogBox dialog = new DialogBox(hc, DialogType.INFO, null, "多个文件", info.toString());
                    dialog.getBodyTextArea().setPrefHeight(150);
                    dialog.show();
                }

                imageBox.getPopUpMenu().hide();
                break;
            default:
        }
    }
}
