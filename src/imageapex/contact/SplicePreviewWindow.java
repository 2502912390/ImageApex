package imageapex.contact;

import com.jfoenix.controls.JFXDecorator;
import imageapex.main.java.model.ImageModel;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import imageapex.contact.javaControllers.SplicePreviewController;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 图片拼接窗口
 */
public class SplicePreviewWindow extends Application {

    public static double windowWidth;//保存合适的窗口大小
    public static double windowHeight;

    private SplicePreviewController sp;//
    private ArrayList<ImageModel> imageModelList;//要拼接的图像

    String mode;//用于选择拼接方式 H V Grid

    @Override
    public void init() throws Exception {
        super.init();
        //根据屏幕大小自适应设置长宽
        try {
            Rectangle2D bounds = Screen.getScreens().get(0).getBounds();
            windowWidth = bounds.getWidth() / 1.5;
            windowHeight = bounds.getHeight() / 1.5;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/imageapex/contact/resources/fxml/SplicePreview.fxml"));

        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(new JFXDecorator(stage, root), windowWidth, windowHeight);

        sp = fxmlLoader.getController();  //通过FXMLLoader获取窗口的controller实例
        if(mode.equals("H")){
            sp.setImageModelList1(imageModelList);//对imageModelList横向拼接
        } else if (mode.equals("V")) {
            sp.setImageModelList0(imageModelList);//对imageModelList竖直拼接
        }else if(mode.equals("Grid")){//九宫格
//            System.out.println("999");//for_test
            sp.setImageModelList2(imageModelList);
        }else{//涂鸦

        }

        //加载css样式文件
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.addAll(this.getClass().getResource("/imageapex/contact/resources/css/contact.css").toExternalForm());


        stage.setTitle("图片拼接预览");
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/imageapex/main/resources/iconsOfMain/app.png")));
        stage.setScene(scene);
        stage.show();
    }

    public void initImageList(ArrayList<ImageModel> imageModelList,String mode){
        this.imageModelList = imageModelList;
        this.mode=mode;
    }
}