package imageapex.concat.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import imageapex.main.java.controllers.ControllerUtil;
import imageapex.main.java.controllers.HomeController;
import imageapex.main.java.model.ImageModel;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

//拼接类Contrller
public class SplicePreviewController implements Initializable {
    @FXML
    private StackPane rootPane;
    @FXML
    private JFXButton saveButton;//保存按钮

    @FXML
    private ImageView imageView0;//竖

    @FXML
    private ImageView imageView1;//横

    @FXML
    private ImageView imageView2;//九宫格

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox vBox0;//存储竖直拼接后的图像

    @FXML
    private HBox hBox1;//存储水平拼接后图像

    @FXML
    GridPane gridPane = new GridPane();//九宫格网格面板

    private ImageModel imageModel;

    private ArrayList<ImageModel> imageModelList;//要拼接的图像
    private HomeController hc;
    private JFXSnackbar snackbar;//弹窗显示

    String mode;//拼接的模式

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        vBox0.setAlignment(Pos.CENTER);
        vBox0.setSpacing(0);

        hBox1.setAlignment(Pos.CENTER);
        hBox1.setSpacing(0);

        //该类放入全局映射
        ControllerUtil.controllers.put(this.getClass().getSimpleName(), this);
        hc = (HomeController) ControllerUtil.controllers.get(HomeController.class.getSimpleName());

        //saveButton的位置会根据rootPane的大小自动调整
        saveButton.translateYProperty().bind(rootPane.heightProperty().divide(15).multiply(5));
        saveButton.translateXProperty().bind(rootPane.widthProperty().divide(15).multiply(6));
        snackbar = new JFXSnackbar(hc.getRootPane());
    }

    //先调用这个设置好图片，否则会导致空指针 bug
    //将set图片竖直拼接
    public void setImageModelList0(ArrayList<ImageModel> set) {
        this.mode="V";
        this.imageModelList = set;
        scrollPane.setContent(vBox0);

        int number = 0;
        //将图片加入到垂直盒子中
        for (ImageModel im : imageModelList) {
            Image image = new Image(im.getImageFile().toURI().toString());
            ImageView imageView = new ImageView(image);

            if (number == 0) {
                this.imageModel = im;
                this.imageView0 = imageView;
                number++;
            }

            imageView.setSmooth(true);
            imageView.setFitWidth(800); //此处指定了拼接出的图片的宽度，注释掉此句则保持原图尺寸
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-margin:0;-fx-padding:0;");

            vBox0.getChildren().add(imageView);//拼接
        }
    }

     public void setImageModelList1(ArrayList<ImageModel> set) {
        this.mode="H";
         this.imageModelList = set;
         scrollPane.setContent(hBox1);

         int number = 0;
         for (ImageModel im : imageModelList) {
             Image image = new Image(im.getImageFile().toURI().toString());
             ImageView imageView = new ImageView(image);

             if (number == 0) {
                 this.imageModel = im;
                 this.imageView1 = imageView;
                 number++; // 计数器加1
             }

             imageView.setSmooth(true);
             imageView.setFitHeight(600);
             imageView.setPreserveRatio(true);
             imageView.setStyle("-fx-margin:0;-fx-padding:0;");
             hBox1.getChildren().add(imageView);
         }
     }

    //九宫格
     public void setImageModelList2(ArrayList<ImageModel> set) {
         this.mode="Grid";
         this.imageModelList = set;
         scrollPane.setContent(gridPane);

         int row = 0;
         int col = 0;

         for (ImageModel im : imageModelList) {
             Image image = new Image(im.getImageFile().toURI().toString());
             ImageView imageView = new ImageView(image);

             if (row == 0 && col == 0) {
                 this.imageModel = im;
                 this.imageView2 = imageView;
             }

             imageView.setSmooth(true);
             imageView.setFitWidth(300);//设置成正方形 且不能保持原比例 否则效果不好 +++
             imageView.setFitHeight(300);
             imageView.setPreserveRatio(false);
             imageView.setStyle("-fx-margin:0;-fx-padding:0;"); // 设置图片的样式，去除外边距和内边距

             gridPane.add(imageView, col, row); // 将ImageView对象添加到网格面板中的指定位置
             col++; // 列计数器加1
             if (col == 3) { // 如果列计数器达到3，则换行
                 col = 0;
                 row++;
             }
         }
     }

    //截图 = 保存 与saveButton绑定
    @FXML
    private void snap() {
        WritableImage wa;
        Stage stage;

        if(this.mode.equals("H")){//bug 空指针？？？ 解决 原因是setImageModelList2没有对mode复制
            wa = imageView1.getParent().snapshot(null, null);
            stage = (Stage) imageView1.getScene().getWindow();
        }else if(this.mode.equals("V")){
            wa = imageView0.getParent().snapshot(null, null);
            stage = (Stage) imageView0.getScene().getWindow();
        }else{
//            System.out.println("99999");//for_test
            wa = gridPane.getParent().snapshot(null, null);
            stage= (Stage) gridPane.getScene().getWindow();
        }

        //设置图片名字包含当前系统时间
        Date date = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

        String prefix = imageModelList.get(0).getImageNameNoExt();//获取不带扩展名的名字
        try {
            BufferedImage buff = SwingFXUtils.fromFXImage(wa, null);
            System.out.println("buff = " + buff);

            //保存为png格式图像
            ImageIO.write(buff, "png",
                    //保存到当前文件夹
                    new File(imageModel.getImageParentPath() + "\\" + prefix + "_concat_" + dateFormat.format(date) + ".png"));

            //刷新界面
            hc.refreshImagesList(hc.getSortComboBox().getValue());
            stage.close(); //为了处理卡顿关闭该窗口
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("拼接完成，已创建副本")); //信息条提示
        } catch (IOException e) {
            stage.close();
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("拼接失败，图片过长"));
            e.printStackTrace();
        }
    }
}