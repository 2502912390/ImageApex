package imageapex.show.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.*;
import javafx.scene.transform.Scale;
import imageapex.show.DisplayWindow;
import imageapex.main.java.components.DialogBox;
import imageapex.main.java.components.DialogType;
import imageapex.main.java.controllers.AbstractController;
import imageapex.main.java.controllers.ControllerUtil;
import imageapex.main.java.controllers.HomeController;
import imageapex.main.java.model.ImageListModel;
import imageapex.main.java.model.ImageModel;
import imageapex.show.java.model.SwitchPics;
import imageapex.main.java.model.SelectedModel;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Translate;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

/**
 * 展示窗口的控制器
 */
public class DisplayWindowController extends AbstractController implements Initializable {

    @FXML
    @Getter
    public StackPane rootPane;
    public HBox toolbar;
    private Stage stage; //获取当前窗口

    @FXML
    public JFXButton saveButton;

    @FXML
    @Setter
    @Getter
    private ImageView imageView;

    @Setter
    @Getter
    private Image image;
    private ImageModel imageModel;
    public ArrayList<ImageModel> imageModelArrayList;

    @Getter
    private JFXSnackbar snackbar; //下方通知条
    private SwitchPics sw;
    private HomeController hc;

    private int width ;
    private int height ;

    // 设置涂鸦的圆形半径
    private double radius;

    private PixelWriter pixelWriter;
    private ColorPicker colorPicker;

    public DisplayWindowController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerUtil.controllers.put(this.getClass().getSimpleName(), this);
        hc = (HomeController) ControllerUtil.controllers.get(HomeController.class.getSimpleName());

        toolbar.translateYProperty().bind(rootPane.heightProperty().divide(5).multiply(2));

        saveButton.setVisible(false);//默认不可见，只有点击编辑才可见

        snackbar = new JFXSnackbar(rootPane);
        stage = DisplayWindow.getStage();
        radius=6.0;

        // 创建一个ColorPicker组件 用于选择颜色
        colorPicker = new ColorPicker();
        colorPicker.setValue(Color.RED);
        toolbar.getChildren().add(colorPicker);
        colorPicker.setVisible(false);//只有编辑才会显现

        System.out.println("Display window initialization done...");
    }

    public void initImage(ImageModel im) {

        if (im == null) {
            this.imageModel = null;
            imageView.setImage(null);
            return;
        }

        if (hc.isComboBoxClicked()) {
            imageModelArrayList = ImageListModel.refreshList(im.getImageFile().getParent(), hc.getSortComboBox().getValue());
        } else {
            imageModelArrayList = ImageListModel.refreshList(im.getImageFile().getParent());
        }

        this.imageModel = im;
        this.image = new Image(im.getImageFile().toURI().toString());
        this.imageView.setImage(image);
        this.sw = new SwitchPics(imageModelArrayList);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        //以下适应图片比例，避免宽度过大显示不全
        //获取当前窗口高度比例
        double winWidth, winHeight;
        if (stage != null) {
            winWidth = stage.getScene().getWidth();
            winHeight = stage.getScene().getHeight();
        } else {
            winWidth = DisplayWindow.windowWidth;
            winHeight = DisplayWindow.windowHeight;
        }
        double sysRatio = winWidth / winHeight;                 //窗口长宽比
        double ratio = image.getWidth() / image.getHeight();    //图片长宽比

        //清空上次设置后遗留下的数据
        imageView.fitWidthProperty().unbind();
        imageView.fitHeightProperty().unbind();
        imageView.setFitHeight(0);
        imageView.setFitWidth(0);

        //若图片长或宽比窗口大，缩小至窗口大小并随窗口绑定长宽，否则以原尺寸显示
        if (image.getWidth() > winWidth || image.getHeight() > winHeight) {
            if (ratio > sysRatio) {
                imageView.fitWidthProperty().bind(rootPane.widthProperty());
            } else {
                imageView.fitHeightProperty().bind(rootPane.heightProperty());
            }
        } else {
            imageView.fitWidthProperty().setValue(image.getWidth());
        }

        setImageMouseAction();
    }

    private void setImageMouseAction() {
        //以下实现滚轮的放大缩小
        imageView.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                //如果滚轮向下滑动，缩小
                if (event.getDeltaY() < 0) {
                    Scale scale = new Scale(0.9, 0.9, event.getX(), event.getY());
                    imageView.getTransforms().add(scale);
                }
                //如果滚轮向上滑动，放大
                if (event.getDeltaY() > 0) {
                    Scale scale = new Scale(1.1, 1.1, event.getX(), event.getY());
                    imageView.getTransforms().add(scale);
                }
            }
        });

        //记录鼠标点击的每次位置
        final double[] lastPosition = new double[2];
        imageView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                lastPosition[0] = event.getX();
                lastPosition[1] = event.getY();
            }
        });

        //以下实现鼠标拖拽移动
        imageView.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Translate tran = new Translate(event.getX() - lastPosition[0], event.getY() - lastPosition[1]);
                imageView.getTransforms().add(tran);
            }
        });

        // bug 添加涂鸦后拖拽会失效 解决 原因是冲突了
    }

    /**
     * 恢复初始缩放比例和位置
     */
    @FXML
    private void initStatus() {
//        saveButton.setVisible(false);

        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
        imageView.getTransforms().clear();
    }

    //-------------以下为工具栏按钮事件---------------
    @FXML
    private void zoomIn() {
//        saveButton.setVisible(false);

        imageView.setScaleX(imageView.getScaleX() * 1.25);
        imageView.setScaleY(imageView.getScaleY() * 1.25);
    }

    @FXML
    private void zoomOut() {
//        saveButton.setVisible(false);

        imageView.setScaleX(imageView.getScaleX() * 0.75);
        imageView.setScaleY(imageView.getScaleY() * 0.75);
    }

    //上一张图
    @FXML
    private void showPreviousImg() throws IOException {
        saveButton.setVisible(false);

        initStatus();

        //为了防止删除后显示空白，自动刷新
        if (imageModel != null)
            imageModelArrayList = ImageListModel.refreshList(imageModel.getImageFile().getParent());

        if (imageModelArrayList == null || imageModelArrayList.size() == 0) {
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("此文件夹图片已空"));
            initImage(null);
            stage.setTitle("无图片");
        } else {
            initImage(sw.lastImage(imageModel));
            stage.setTitle(imageModel.getImageName()); //更新图片名字
        }
    }

    //下一张图
    @FXML
    public void showNextImg() throws IOException {
        saveButton.setVisible(false);

        initStatus();

        //为了防止删除后显示空白，自动刷新
        if (imageModel != null)
            imageModelArrayList = ImageListModel.refreshList(imageModel.getImageFile().getParent());

        if (imageModelArrayList == null || imageModelArrayList.size() == 0) {
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("此文件夹图片已空"));
            this.imageModel = null;
            imageView.setImage(null);
            stage.setTitle("无图片");
        } else {
            initImage(sw.nextImage(imageModel));
            stage.setTitle(imageModel.getImageName()); //更新图片名字
        }
    }

    @FXML
    //幻灯片放映
    private void playSlide() {
        saveButton.setVisible(false);

        initStatus();   //比例重新设定
        toolbar.setVisible(false);  //使工具栏不可见

        stage.setFullScreen(true);  //设置窗口为全屏
        snackbar.enqueue(new JFXSnackbar.SnackbarEvent("开始幻灯片放映，点击任意键结束"));

        //以下实现定时器功能翻页
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //定时任务中安排切换下一页功能
                Platform.runLater(() -> {
                    try {
                        showNextImg();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        };
        Timer timer = new Timer();
        long delay = 5000;  // 定义开始等待时间
        long intervalPeriod = 3000;  //每次执行的间隔
        timer.scheduleAtFixedRate(task, delay, intervalPeriod); // 定时器执行

        //当鼠标点击时，暂停计时器，恢复工具栏
        imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stopSlide(timer, stage);
            }
        });

        //键盘输入任意键退出
        imageView.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                stopSlide(timer, stage);
            }
        });
    }

    //停止幻灯片播放
    private void stopSlide(Timer timer, Stage stage) {
        saveButton.setVisible(false);

        timer.cancel();
        toolbar.setVisible(true);
        stage.setFullScreen(false);
        stage.sizeToScene();
        snackbar.enqueue(new JFXSnackbar.SnackbarEvent("幻灯片放映结束"));

        //清空事件
        imageView.getScene().setOnKeyPressed(event -> {
        });
        imageView.setOnMouseClicked(event -> {
        });
    }

    @FXML
    public void showInfo() {
        saveButton.setVisible(false);

        if (imageModel == null) {
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("无属性展示"));
            return;
        }
        Image image = new Image(imageModel.getImageFile().toURI().toString());
        StringBuilder info = new StringBuilder();
        info.append("尺寸：").append(image.getWidth()).append(" × ").append(image.getHeight()).append("\n");
        info.append("类型：").append(imageModel.getImageType().toUpperCase()).append("\n");
        info.append("大小：").append(imageModel.getFormatSize()).append("\n");
        info.append("日期：").append(imageModel.getFormatTime()).append("\n");
        info.append("\n位置：").append(imageModel.getImageFilePath());
        new DialogBox(this, DialogType.INFO, imageModel,
                imageModel.getImageName(), info.toString()).show();
    }

    //删除
    @FXML
    private void delete() {
        saveButton.setVisible(false);

        if (imageModel == null) {
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("无文件可删除"));
            return;
        }
        SelectedModel.setSourcePath(imageModel);
        new DialogBox(this, DialogType.DELETE, imageModel,
                "删除图片",
                "删除文件: " + imageModel.getImageName() + "\n\n你可以在回收站处找回。").show();
    }

    @FXML
    private void fullScreen() {
//        saveButton.setVisible(false);

        if (stage.isFullScreen()) {
            stage.setFullScreen(false);
            stage.sizeToScene();
        } else {
            stage.setFullScreen(true);
        }
    }

    @FXML
    private void compress() {
        saveButton.setVisible(false);

        SelectedModel.setSourcePath(imageModel.getImageFilePath());
        int success = SelectedModel.compressImage(800);
        if (success != 0) {
            initImage(imageModel);
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("已压缩" + success + "张图片并创建副本"));
            try {
                // 刷新缩略图列表
                hc.placeImages(ImageListModel.initImgList(imageModel.getImageParentPath()),
                        imageModel.getImageParentPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("没有图片执行压缩。压缩条件:大于800KB"));
        }
    }

    //实现涂鸦照片保存功能
    @FXML
    private void snap(){
//        System.out.println("222222");//for_test
        if(saveButton.isVisible()){//bug
            WritableImage wa = imageView.snapshot(null, null);//+++ 编辑像素操作/颜色处理

            //设置图片名字包含当前系统时间
            Date date = new Date();
            Stage stage = (Stage) imageView.getScene().getWindow();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

            String prefix = imageModel.getImageNameNoExt();//获取不带扩展名的名字
            try {
                BufferedImage buff = SwingFXUtils.fromFXImage(wa, null);
                System.out.println("buff = " + buff);

                //保存为png格式图像
                ImageIO.write(buff, "png",
                        //保存到当前文件夹
                        new File(imageModel.getImageParentPath() + "\\" + prefix + "_edit_" + dateFormat.format(date) + ".png"));

                //刷新界面
                hc.refreshImagesList(hc.getSortComboBox().getValue());
                stage.close(); //为了处理卡顿关闭该窗口
                snackbar.enqueue(new JFXSnackbar.SnackbarEvent("涂鸦副本保存成功")); //信息条提示
            } catch (IOException e) {
                stage.close();
                snackbar.enqueue(new JFXSnackbar.SnackbarEvent("保存失败"));
                e.printStackTrace();
            }
        }
    }

    @FXML //涂鸦
    private void edit() {
        System.out.println("11111111111");
        saveButton.setVisible(true);
        colorPicker.setVisible(true);

        //bug? 初始化 image为null
        width = (int) image.getWidth();
        height = (int) image.getHeight();

        // 创建可写图像对象
        WritableImage writableImage = new WritableImage(width, height);
        PixelReader pixelReader = image.getPixelReader();
        pixelWriter = writableImage.getPixelWriter();

        // 将原始图像的像素复制到可写图像中
        for (int x = 0; x < width; x++) {//copy操作
            for (int y = 0; y < height; y++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }

        // 将可写图像显示在 ImageView 中
        imageView.setImage(writableImage);

        imageView.setOnMouseDragged(event -> {
            // 获取鼠标位置
            double x = event.getX();
            double y = event.getY();

            imageView.setOnScroll(new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent event) {
                    //如果滚轮向下滑动，缩小
                    if (event.getDeltaY() < 0) {
                        radius=radius*0.9;
                    }
                    //如果滚轮向上滑动，放大
                    if (event.getDeltaY() > 0) {
                        radius=radius*1.1;
                    }
                }
            });

            // 在以鼠标位置为中心的圆形区域内绘制像素点
            for (int i = (int) (x - radius); i <= x + radius; i++) {
                for (int j = (int) (y - radius); j <= y + radius; j++) {
                    // 检查当前像素点是否在圆形内
                    if (Math.sqrt(Math.pow(i - x, 2) + Math.pow(j - y, 2)) <= radius) {
                        int pixelX = i;
                        int pixelY = j;
                        if (pixelX >= 0 && pixelX < width && pixelY >= 0 && pixelY < height) {
//                            pixelWriter.setColor(pixelX, pixelY, colorPicker.getValue());
                            pixelWriter.setColor(pixelX, pixelY, colorPicker.getValue());
                        }
                    }
                }
            }

        });
    }
}

//效果不好
// 添加鼠标事件监听器
//        imageView.setOnMouseDragged(event -> {
//            // 获取鼠标位置
//            double x = event.getX();
//            double y = event.getY();
//
//            // 设置涂鸦颜色 +++添加可选颜色操作
//            Color color = Color.RED;
//
//            // 在图像上绘制点
//            int pixelX = (int) x;
//            int pixelY = (int) y;
//            if (pixelX >= 0 && pixelX < width && pixelY >= 0 && pixelY < height) {
//                pixelWriter.setColor(pixelX, pixelY, color);
//            }
//        });