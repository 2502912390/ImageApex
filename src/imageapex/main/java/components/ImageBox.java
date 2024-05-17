package imageapex.main.java.components;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.effects.JFXDepthManager;
import imageapex.show.DisplayWindow;
import imageapex.main.java.controllers.PopupMenuController;
import imageapex.main.java.model.ImageModel;
import imageapex.main.java.model.SelectionModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter
public class ImageBox extends VBox {//缩略图单元

    private ImageModel im;//原始图片
    private ImageView2 imageView2;//缩略图
    private JFXPopup popUpMenu;//弹窗
    @Getter
    private JFXCheckBox checkBox = new JFXCheckBox();//复选框

    public ImageBox(ImageModel im) {//构造一个缩略图
        this.im = im;
        ImageView2 imageView = new ImageView2(new Image(im.getImageFile().toURI().toString(),
                100,
                100,
                true,
                true,
                true));
        this.imageView2 = imageView;                                //图片
        WhiteRippler riv = new WhiteRippler(imageView);     //一个水波纹点击效果的包装
        ImageLabel imageLabel = new ImageLabel(im.getImageName());  //标签 - 文件名
        imageLabel.setStyle("-fx-padding:7 0 7 -2;");

        HBox hBox = new HBox(checkBox, imageLabel);//添加复选框和标签
        hBox.setAlignment(Pos.CENTER);
        hBox.setStyle("-fx-padding:5 5 3 5;");

        getChildren().addAll(riv, hBox);
        setMaxSize(170, 170);
        setAlignment(Pos.BOTTOM_CENTER);

        //创建一个可以显示文本信息的可悬停的提示框
        String tooltip = String.format("名称: %s\n大小: %s", im.getImageName(), im.getFormatSize());
        Tooltip.install(this, new Tooltip(tooltip));//install方法将tip和当前ImageBox联系起来

        JFXDepthManager.setDepth(this, 0);
        initMouseAction();
//        initPopUpMenu();
        initCheckBox();
    }

    /**
     * 设置选中与否的属性绑定
     */
    ImageBox imageBox = this;
    private void initCheckBox() {
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    SelectionModel.add(imageBox);
                } else {
                    SelectionModel.remove(imageBox);
                }
            }
        });
    }

    //设置为选中状态
    public void setIsSelect(){
        this.checkBox.setSelected(true);
    }

    public void setNoSelect(){
        this.checkBox.setSelected(false);
    }


    private void initPopUpMenu() {//右键弹出菜单
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/imageapex/main/resources/fxml/PopupMenu.fxml"));
        loader.setController(new PopupMenuController(this));
        try {
            popUpMenu = new JFXPopup(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        popUpMenu.setAutoHide(true);
    }


    private void initMouseAction() {//监听鼠标事件
        //鼠标点击事件
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                // 鼠标左键双击看大图
                DisplayWindow dw = new DisplayWindow();
                dw.setImage(im);
                dw.start(new Stage());
            } else if (event.getButton() == MouseButton.SECONDARY) {
                // 鼠标右键菜单
                initPopUpMenu();
                popUpMenu.show(this,
                        JFXPopup.PopupVPosition.TOP,
                        JFXPopup.PopupHPosition.LEFT,
                        100, 100);
            }
        });

        //当鼠标指向时
        this.setOnMouseMoved(event -> {
            this.setStyle("-fx-background-color:rgba(0, 0, 0, 0.07);");
        });

        //当鼠标离开时
        this.setOnMouseExited(event -> {
            this.setStyle("-fx-background-color:transparent;");
        });

    }
}
