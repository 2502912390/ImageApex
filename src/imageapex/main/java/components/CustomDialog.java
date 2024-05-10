package imageapex.main.java.components;


import com.jfoenix.controls.*;
import imageapex.concat.SplicePreviewWindow;
import imageapex.show.java.controllers.DisplayWindowController;
import imageapex.main.java.controllers.AbstractController;
import imageapex.main.java.controllers.ControllerUtil;
import imageapex.main.java.controllers.HomeController;
import imageapex.main.java.model.ImageModel;
import imageapex.main.java.model.SelectedModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 自定义、可复用的对话框，减少使用对话框时的重复代码
 */
public class CustomDialog extends JFXDialog {

    @Setter
    private ImageModel targetImage;

    private AbstractController controller;
    private HomeController hc = (HomeController) ControllerUtil.controllers.get(HomeController.class.getSimpleName());
    private DisplayWindowController dwc = (DisplayWindowController) ControllerUtil.controllers.get(DisplayWindowController.class.getSimpleName());

    private DialogType type;

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

    private ArrayList<ImageModel> sourceList;//要拼接的图像

    @Getter
    private JFXDialogLayout layout = new JFXDialogLayout();

    /**
     * @param controller  对话框出现所在的界面的控制器
     *                    如：需要在主界面弹出，则传入{@link HomeController}的实例
     * @param type        对话框种类，详见{@link DialogType}
     * @param targetImage 待处理的目标图片对象
     */
    public CustomDialog(AbstractController controller, DialogType type, ImageModel targetImage) {
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
                makeRenameDialog();
                break;
            case REPLACE:
                makeReplaceDialog();
                break;
            case CHOICE://选择拼接方式
                makeChoiceDialog();
            default:
        }
    }

    /**
     * @param controller  对话框出现所在的界面的控制器
     *                    如需要在主界面弹出，则传入{@link HomeController}的实例
     * @param type        对话框种类，详见{@link DialogType}
     * @param targetImage 待处理的目标图片对象
     * @param headingText 对话框标题
     */
    public CustomDialog(AbstractController controller,
                        DialogType type, ImageModel targetImage,
                        String headingText) {
        this(controller, type, targetImage);
        setHeadingLabel(headingText);
    }

    //用于图像拼接构造函数
    public CustomDialog(AbstractController controller,
                        DialogType type, ImageModel targetImage,
                        String headingText,ArrayList<ImageModel> sourceList) {
        this(controller, type, targetImage);
        this.sourceList=sourceList;
        setHeadingLabel(headingText);
    }

    /**
     * @param controller  对话框出现所在的界面的控制器
     *                    如需要在主界面弹出，则传入{@link HomeController}的实例
     * @param type        对话框种类，详见{@link DialogType}
     * @param targetImage 待处理的目标图片对象
     * @param headingText 对话框标题
     * @param bodyText    正文
     */
    public CustomDialog(AbstractController controller,
                        DialogType type, ImageModel targetImage,
                        String headingText, String bodyText) {
        this(controller, type, targetImage, headingText);
        setBodyLabel(bodyText);
    }

    public void setHeadingLabel(String headingText) {
        headingLabel = new Label(headingText);
        headingLabel.getStyleClass().add("dialog-heading");
        layout.setHeading(headingLabel);
    }

    public void setBodyLabel(String bodyText) {
        bodyLabel = new Label(bodyText);
        bodyLabel.getStyleClass().add("dialog-body");
        if (type == DialogType.INFO) {
            setBodyTextArea(bodyText);
        } else {
            layout.setBody(bodyLabel);
        }
    }

    /**
     * 向对话框主体传入其他内容
     */
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

    /**
     * 重命名用到
     */
    private void setBodyTextField() {
        bodyTextField = new JFXTextField();
        bodyTextField.setText(targetImage.getImageName());
        bodyTextField.getStyleClass().addAll("rename-text-field", "dialog-body");
        layout.setBody(bodyTextField);
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
            if (SelectedModel.renameImage(bodyTextField.getText()))
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
