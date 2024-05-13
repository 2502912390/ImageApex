package imageapex;

import com.jfoenix.controls.JFXDecorator;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

//重命名bug  多选重命名
//鼠标滚轮bug
//点缩略图选中 单击拖拽多选功能
//取消选中状态
//第一张或最后一张的提示信息有bug

public class Main extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ImageApex");

        //根据屏幕大小自适应设置长宽
        double width = 800;
        double height = 600;

        try {
            Rectangle2D bounds = Screen.getScreens().get(0).getBounds();
            width = bounds.getWidth() / 1.45;
            height = bounds.getHeight() / 1.35;
        } catch (Exception e){
            e.printStackTrace();
        }

        Parent root = FXMLLoader.load(getClass().getResource("/imageapex/main/resources/fxml/Home.fxml"));
        Scene scene = new Scene(new JFXDecorator(primaryStage, root), width, height);

        //加载css样式文件
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.addAll(Main.class.getResource("/imageapex/main/resources/css/main.css").toExternalForm());

        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/imageapex/main/resources/icons/app.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
        System.out.println("Starting ...");
    }

    public static void main(String[] args) {
//        System.out.println("上传成功");
        launch(args);
    }
}
