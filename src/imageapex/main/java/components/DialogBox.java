package imageapex.main.java.components;


import com.jfoenix.controls.*;
import imageapex.concat.SplicePreviewWindow;
import imageapex.show.java.controllers.DisplayWindowController;
import imageapex.main.java.controllers.AbstractController;
import imageapex.main.java.controllers.ControllerInstance;
import imageapex.main.java.controllers.HomeController;
import imageapex.main.java.model.ImageModel;
import imageapex.main.java.model.SelectedModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;




public class DialogBox extends JFXDialog { //对话框类

    @Setter
    private ImageModel targetImage;

    private AbstractController controller;
    private HomeController hc = (HomeController) ControllerInstance.controllers.get(HomeController.class.getSimpleName());
    private DisplayWindowController dwc = (DisplayWindowController) ControllerInstance.controllers.get(DisplayWindowController.class.getSimpleName());

    private DialogType type; //对话框类型 删除/重命名。。。

    //对话框的三个按钮
    @Getter
    private JFXButton leftButton;
    @Getter
    private JFXButton rightButton;
    @Getter
    private JFXButton midButton;

    @Getter
    private Label headingLabel;
    @Getter
    private Label bodyLabel;

    @Getter
    private JFXTextArea bodyTextArea;
    private JFXTextField bodyTextField;
    private JFXTextField bodyTextField1;
    private JFXTextField bodyTextField2;

    private ArrayList<ImageModel> sourceList;//要拼接的图像

    //创建对话框的内容组件
    @Getter
    private JFXDialogLayout layout = new JFXDialogLayout();

    public DialogBox(AbstractController controller, DialogType type, ImageModel targetImage) {//基本构造函数
        this.controller = controller;
        this.type = type;
        this.targetImage = targetImage;

        leftButton = new JFXButton();
        leftButton.getStyleClass().add("dialog-cancel");
        leftButton.setText("取消");

        rightButton = new JFXButton();
        rightButton.getStyleClass().add("dialog-confirm");
        rightButton.setText("确认");

        midButton = new JFXButton();
        midButton.getStyleClass().add("dialog-confirm");
        midButton.setText("中间");
        midButton.setVisible(false);//设置默认不可见

        //默认情况下，两按钮都是关闭对话框操作而不做任何事
        setCloseAction(leftButton);
        setCloseAction(rightButton);
        setCloseAction(midButton);

        this.setOverlayClose(true);
        layout.setMaxWidth(500);

        switch (type) {
            case INFO:
                makeInfoDialog();
                break;
            case DELETE:
                makeDeleteDialog();
                break;
            case RENAME:
                if(SelectedModel.singleOrMultiple==0){//单选
                    makeRenameDialog();
                }else if(SelectedModel.singleOrMultiple==1){//多选 待办
//                    System.out.println("test++++++++++++");
                    makeMulRenameDialog();
                }
//                System.out.println(SelectedModel.singleOrMultiple); //for debug
                break;
            case REPLACE:
                makeReplaceDialog();
                break;
            case CHOICE://选择拼接方式
                makeChoiceDialog();
            default:
        }
    }


    public DialogBox(AbstractController controller, DialogType type, ImageModel targetImage, String headingText) {//设置标题构造函数
        this(controller, type, targetImage);
        setHeadingLabel(headingText);
    }


    public DialogBox(AbstractController controller, DialogType type, ImageModel targetImage, String headingText, ArrayList<ImageModel> sourceList) {//用于图像拼接构造函数
        this(controller, type, targetImage);
        this.sourceList=sourceList;
        setHeadingLabel(headingText);
    }

    public DialogBox(AbstractController controller, DialogType type, ImageModel targetImage, String headingText, String bodyText) {//添加了文本框的构造函数
        this(controller, type, targetImage, headingText);
        setBodyLabel(bodyText);
    }

    public void setHeadingLabel(String headingText) {//为对话框添加标题
        headingLabel = new Label(headingText);
        headingLabel.getStyleClass().add("dialog-heading");
        layout.setHeading(headingLabel);
    }

    public void setBodyLabel(String bodyText) {//为对话框添加主体
        bodyLabel = new Label(bodyText);
        bodyLabel.getStyleClass().add("dialog-body");

        if (type == DialogType.INFO) {
            setBodyTextArea(bodyText);
        } else {
            layout.setBody(bodyLabel);
        }
    }

    //向对话框传入其他内容 vbox等
    public void setBodyContent(Node... body) {
        layout.setBody(body);
    }

    /**
     * 展示对话框
     */
    @Override
    public void show() {
        if (leftButton != null && rightButton != null && midButton != null)
            layout.setActions(leftButton,midButton,rightButton);
        else
            System.out.println("ERROR: 未指定对话框按钮");
        this.setContent(layout);
        this.show(controller.getRootPane());
    }

    private void setBodyTextArea(String text) {
        bodyTextArea = new JFXTextArea(text);
        bodyTextArea.getStyleClass().addAll("dialog-text-area", "dialog-body");
        bodyTextArea.setEditable(false);
        layout.setBody(bodyTextArea);
    }

    //重命名弹框
    private void setBodyTextField() {
        bodyTextField = new JFXTextField();
        bodyTextField.setText(targetImage.getImageNameNoExt());//不修改
        bodyTextField.getStyleClass().addAll("rename-text-field", "dialog-body");
        layout.setBody(bodyTextField);
    }

    private void setMulBodyTextField(){//设置多张图片重命名的body
        bodyTextField = new JFXTextField();
        bodyTextField.setText(targetImage.getImageNameNoExt());//只设置不带扩展名的
        bodyTextField.getStyleClass().addAll("rename-text-field", "dialog-body");
        bodyTextField.setEditable(true);

        bodyTextField1 = new JFXTextField();
//        bodyTextField1.setText("启始编号");
        bodyTextField1.getStyleClass().addAll("rename-text-field", "dialog-body");
        bodyTextField1.setEditable(true);

        bodyTextField2 = new JFXTextField();
//        bodyTextField2.setText("编号位数");
        bodyTextField2.getStyleClass().addAll("rename-text-field", "dialog-body");
        bodyTextField2.setEditable(true);

        VBox vbox=new VBox();
        vbox.getChildren().addAll(bodyTextField, bodyTextField1,bodyTextField2);

        layout.setBody(vbox);
    }

    /**
     * 设置按钮关闭对话框
     */
    private void setCloseAction(JFXButton button) {
        button.setOnAction(event -> {
            this.close();
        });
    }

    /**
     * 展示正在加载圆圈spinner
     */
    public void setLoadingSpinner() {
        JFXSpinner spinner = new JFXSpinner(-1);
        layout.setBody(spinner);
    }

    /**
     * 构造一个删除功能的对话框
     */
    private void makeDeleteDialog() {
        rightButton.setText("删除");
        rightButton.setStyle("-fx-text-fill: RED;");
        rightButton.setOnAction(event -> {
            int n;
            n = SelectedModel.deleteImage();
            if (n > 0) {
                controller.getSnackbar().enqueue(new JFXSnackbar.SnackbarEvent("成功删除 " + n + " 张图片"));    //显示删除成功通知。
                if (dwc != null) {
                    try {
                        dwc.showNextImg();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                controller.getSnackbar().enqueue(new JFXSnackbar.SnackbarEvent("删除失败"));    //显示删除成功通知。
            }
            hc.refreshImagesList(hc.getSortComboBox().getValue());
            this.close();
        });
    }

    /**
     * 构造一个重命名功能的对话框
     */
    private void makeRenameDialog() {
        setBodyTextField();
        rightButton.setOnAction(event -> {
            if (SelectedModel.renameImage(bodyTextField.getText()+"."+targetImage.getImageType()))
                controller.getSnackbar().enqueue(new JFXSnackbar.SnackbarEvent("重命名成功"));
            this.close();
            hc.refreshImagesList(hc.getSortComboBox().getValue());
        });
    }

    //多文件重命名对话框 待办
    private void makeMulRenameDialog() {
        setMulBodyTextField();
        rightButton.setOnAction(event -> {
            if (SelectedModel.renameImage(bodyTextField.getText()+"."+targetImage.getImageType(),bodyTextField1.getText(),bodyTextField2.getText()))
                controller.getSnackbar().enqueue(new JFXSnackbar.SnackbarEvent("重命名成功"));
            this.close();
            hc.refreshImagesList(hc.getSortComboBox().getValue());
        });
    }

    /**
     * 构造一个展示信息的对话框
     */
    private void makeInfoDialog() {
        rightButton.getStyleClass().add("dialog-confirm");
        rightButton.setText("确认");
    }

    /**
     * 构造一个询问替换的对话框
     */
    private void makeReplaceDialog() {
        leftButton.setText("跳过");
        rightButton.setText("替换");
        rightButton.setStyle("-fx-text-fill: BLUE;");
        leftButton.setOnAction(event -> {
            System.out.println("选择跳过");
            this.close();
            controller.getSnackbar().enqueue(new JFXSnackbar.SnackbarEvent("选择跳过"));
            hc.refreshImagesList(hc.getSortComboBox().getValue());
        });
        rightButton.setOnAction(event -> {
            System.out.println("选择替换");
            if (SelectedModel.replaceImage()) {
                controller.getSnackbar().enqueue(new JFXSnackbar.SnackbarEvent("替换成功"));
            }
            hc.refreshImagesList(hc.getSortComboBox().getValue());
            this.close();
            SelectedModel.setHavePastedNum(SelectedModel.getHavePastedNum() + 1);
        });
    }

    //选择拼接弹窗
    private void makeChoiceDialog() {
        leftButton.setText("竖直拼接");
        leftButton.setStyle("-fx-text-fill: #ff0000;");

        midButton.setVisible(true);//只有选择九宫格时才可见
        midButton.setText("横向拼接");
        midButton.setStyle("-fx-text-fill: #22ff00;");

        rightButton.setText("九宫格拼接");
        rightButton.setStyle("-fx-text-fill: #0022ff;");

        leftButton.setOnAction(event -> {
            SplicePreviewWindow previewWindow = new SplicePreviewWindow();
            previewWindow.initImageList(sourceList,"V");
            //打开窗口
            try {
                previewWindow.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        midButton.setOnAction(event -> {//横向拼接
            SplicePreviewWindow previewWindow = new SplicePreviewWindow();
            previewWindow.initImageList(sourceList,"H");
            //打开窗口
            try {
                previewWindow.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        rightButton.setOnAction(event -> {
            if(sourceList.size()!=9){
                new JFXSnackbar(hc.getRootPane()).enqueue(new JFXSnackbar.SnackbarEvent("目前选了"+sourceList.size()+"张照片，"+"请选择九张照片捏~"));
            }else{
                SplicePreviewWindow previewWindow = new SplicePreviewWindow();
                previewWindow.initImageList(sourceList,"Grid");
                //打开窗口
                try {
                    previewWindow.start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
