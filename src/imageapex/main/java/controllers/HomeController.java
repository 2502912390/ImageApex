package imageapex.main.java.controllers;

import com.jfoenix.controls.*;
import imageapex.main.java.components.*;
import imageapex.main.java.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.net.URL;
import java.util.*;
import javafx.scene.paint.Color;

import javafx.scene.shape.*;

public class HomeController extends AbstractController implements Initializable {//主窗口界面控制器
    //文件夹工具(信息)栏功能按钮
    @FXML
    @Getter
    public JFXButton pasteButton;//粘贴按钮
    @FXML
    public JFXTextField searchTextField;//搜索输入栏
    @FXML
    public JFXButton closeSearchButton;//关闭搜索按钮
    @FXML
    public JFXButton gotoButton;//GO
    @FXML
    public AnchorPane anchorPane;//全局面板
    @FXML
    public JFXButton selectAllButton;//全选按钮
    @FXML
    @Getter
    private JFXButton refreshButton;//刷新按钮

    @FXML
    private ToolBar infoBar;//缩略图上方工具栏

    //状态信息
    @FXML
    private Label folderInfoLabel;//当前状态信息栏 照片总数和所占内存
    @FXML
    public Label selectedNumLabel;//选中的照片数量

    @FXML
    @Getter
    private JFXComboBox<String> sortComboBox;//下拉菜单 选择排序选项

    @Getter
    @Setter
    private boolean comboBoxClicked = false;

    @FXML
    private JFXTextField pathTextField;//路径文本区域

    @Getter
    private JFXSnackbar snackbar; //存放临时（弹窗）消息

    //面板
    @FXML
    @Getter
    private StackPane rootPane; //根面板

    //按照添加顺序排列子组件，并在一行或一列填满时自动将后续的子组件推到下一行或下一列
    @FXML
    private FlowPane imageListPane = new FlowPane();//缩略图像列表面板

    @FXML
    private ScrollPane scrollPane; //缩略图区域 当内容超出容器的可视范围时，ScrollPane会自动添加滚动条，使用户可以滚动查看隐藏的内容。
    @FXML
    private AnchorPane folderPane;//树形文件夹面板

    private String currentPath = "";//存放当前文件夹路径

    @Getter
    private Stack<String> pathStack1 = new Stack<>();//保存前后路径栈
    @Getter
    private Stack<String> pathStack2 = new Stack<>();

    private  int firstLoad;

    private ArrayList<ImageModel> curImgList = new ArrayList<>();//当前图片列表

    private double zoomFactor = 1.0;// 缩略图缩放因子为1.0

    // 声明一个变量来存储框选起始点
    private Point2D dragStartPoint = null;

    public HomeController() {
        //将本类的实例添加到全局映射中
        ControllerInstance.controllers.put(this.getClass().getSimpleName(), this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {//初始化FXML文件
        //设置内边距 水平垂直边距 启动缓存
        imageListPane.setPadding(new Insets(10));
        imageListPane.setVgap(10);
        imageListPane.setHgap(10);
        imageListPane.setCache(true);

        //folderPane面板不可调整大小
        SplitPane.setResizableWithParent(folderPane, false);

        snackbar = new JFXSnackbar(rootPane);//使Snackbar和rootPane相关联

        infoBar.setBackground(Background.EMPTY); //信息栏设置透明背景
        closeSearchButton.setVisible(false);//取消搜索按钮默认不可见

        createPasteButton();
        createSortBox();
        creativeIntroImage();       //设置欢迎页必须在scrollPane之后设置，否则会被imageListPane空白页覆盖
        createSearchText();
        createPathText();
        initMouseAction();
        System.out.println("imageapex.Main window initialization done...");
    }

    // 缩略图面板层级（从底到顶）:
    // AnchorPane > ScrollPane > FlowPane(imageListPane)

    private void initMouseAction() {
        imageListPane.setOnScroll(event -> {
            if (event.isControlDown()) {//按下ctrl+鼠标滚动  实现缩放缩略图
//                System.out.println("ctrl***************"); //for_test
                // 检测到Ctrl键被按下
                double deltaY = event.getDeltaY();
                if (deltaY > 0) {
                    // 向上滚动鼠标滚轮，放大图片
                    zoomFactor *= 1.1;
                } else {
                    // 向下滚动鼠标滚轮，缩小图片
                    zoomFactor /= 1.1;
                }
//                imageListPane.getChildren().clear();
                for (Node node : imageListPane.getChildren()) {
                    if (node instanceof ImageBox) { // 检查节点是否为ImageBox类型
                        ImageBox imageBox = (ImageBox) node; // 将节点转换为ImageBox类型
                        imageBox.setPrefWidth(imageBox.getWidth()*zoomFactor);
                        imageBox.setPrefHeight(imageBox.getHeight()*zoomFactor);
//                        System.out.println(imageBox.getWidth()*zoomFactor);
                    }
                }
            }else{//只滚动
                //初始加载后的位置
                int index = firstLoad - 1;

                int times = 30; // 每次滚动加载30张
                while (times > 0) {
                    index++;
                    if (event.getDeltaY() <= 0 && index < curImgList.size()) {
                        ImageBox imageBox = new ImageBox(curImgList.get(index)); //装图片和文件名的盒子，一上一下放置图片和文件名
                        imageListPane.getChildren().add(imageBox);
                    } else {
                        break;
                    }
                    times--;
                }
//                System.out.println("***");
            }
        });

        // 添加鼠标拖拽事件处理程序
        imageListPane.setOnMousePressed(event -> {
            // 记录鼠标按下的起始点
            dragStartPoint = new Point2D(event.getX(), event.getY());
        });

        imageListPane.setOnMouseDragged(event -> {
            if (dragStartPoint != null) {
                // 获取当前鼠标位置
                double x = event.getX();
                double y = event.getY();

                // 计算鼠标拖拽框的位置和大小
                double minX = Math.min(dragStartPoint.getX(), x);
                double minY = Math.min(dragStartPoint.getY(), y);
                double width = Math.abs(x - dragStartPoint.getX());
                double height = Math.abs(y - dragStartPoint.getY());

                // 创建一个矩形框用于显示鼠标拖拽范围
                Rectangle selectionBox = new Rectangle(minX, minY, width, height);
                selectionBox.setFill(Color.TRANSPARENT);//填充颜色透明
                selectionBox.setStroke(Color.BLUE);//边框颜色

                // 将矩形框添加到imageListPane中
                imageListPane.getChildren().add(selectionBox);

                // 获取在框选范围内的ImageBox，并进行一些操作
                for (Node node : imageListPane.getChildren()) {
                    if (node instanceof ImageBox) {
                        ImageBox imageBox = (ImageBox) node;
                        if (selectionBox.getBoundsInParent().intersects(imageBox.getBoundsInParent())) {
                            imageBox.setIsSelect();
                        } else {
                            imageBox.setNoSelect();
                        }
                    }
                }
            }
        });



        imageListPane.setOnMouseReleased(event -> {
            // 清除框选范围的矩形框
            imageListPane.getChildren().removeIf(node -> node instanceof Rectangle);
            // 重置拖拽起始点
            dragStartPoint = null;
        });

//        anchorPane.setOnMouseClicked(event -> {
//            //清除选择
//            System.out.println("+++++++++++++++++++++++++++");
//        });
    }

    public void placeImages(ArrayList<ImageModel> imageModelList, String folderPath) {//将图片列表放置到显示图片的面板
        //检查列表是否空，可能处于初始页面
        if (imageModelList == null)
            return;

        // 每次生成前重置
        imageListPane.getChildren().clear();
        scrollPane.setContent(imageListPane);
        SelectionModel.clear();
        unSelectAll();
        sortComboBox.setVisible(true); //默认排序盒子不出现，除非触发了构建缩略图操作

        //设置初始加载缩略图的数目
        firstLoad = Math.min(imageModelList.size(), 80);

        //更新当前地址
        pathTextField.setText(folderPath);
        currentPath = folderPath;

        //文件夹信息栏设置
        if (imageModelList.isEmpty()) {
            folderInfoLabel.setText("此文件夹下无可识别图片");
            return;
        } else {
            //统计图片总数和总占用空间大小
            int total = ImageSortModel.getListImageNum(imageModelList);
            String size = ImageSortModel.getListImageSize(imageModelList);
            folderInfoLabel.setText(String.format("%d 张图片，共 %s ", total, size));
            selectedNumLabel.setText("| 已选中 0 张");
        }

        //+++ 多线程
        //初始加载 firstload 张缩略图
        int i;
        for (i = 0; i < firstLoad; i++) {
//            System.out.println(i); //for test
            ImageBox imageBox = new ImageBox(imageModelList.get(i)); //装图片和文件名的盒子，一上一下放置图片和文件名
            imageBox.getImageView2().setFitHeight(imageBox.getImageView2().getImage().getHeight() * zoomFactor);
            imageBox.getImageView2().setFitWidth(imageBox.getImageView2().getImage().getWidth() * zoomFactor);
            imageListPane.getChildren().add(imageBox);
        }
    }

    public void refreshImagesList(String sort) {//根据排序方式排序图片列表 并放置
        SelectionModel.clear();
        SelectedModel.getSourcePathList().clear();
        curImgList = ImageSortModel.renewList(currentPath, sort);
        placeImages(curImgList, currentPath);
    }

    public void initEnterFolder(String path) {//
        currentPath = path;
        //入栈以便于后续前进后退
        if (pathStack1.isEmpty() || !pathStack1.peek().equals(path)) {
            pathStack1.push(path);
            pathStack2.clear();
        }
        placeImages(ImageSortModel.renewList(currentPath), currentPath);
    }

    @FXML
    private void creativeIntroImage() {//初始化欢迎界面
        ImageView welcomeImage = new ImageView(new Image("/imageapex/main/resources/images/intro.png"));
        welcomeImage.setFitWidth(850);
        welcomeImage.setPreserveRatio(true);
        HBox hBox = new HBox(welcomeImage);
        hBox.setAlignment(Pos.CENTER);
        StackPane stackPane = new StackPane(hBox);
        scrollPane.setContent(stackPane);
    }

    public void showNotFoundPage() {//显示no found界面
        ImageView img = new ImageView(new Image("/imageapex/main/resources/images/no_result.png"));
        img.setFitHeight(500);
        img.setPreserveRatio(true);
        HBox hBox = new HBox(img);
        hBox.setAlignment(Pos.CENTER);
        StackPane stackPane = new StackPane(hBox);
        scrollPane.setContent(stackPane);
    }

    private void createSortBox() {//初始化排序框
        sortComboBox.getItems().addAll(SortParam.SBNR, SortParam.SBND, SortParam.SBSR, SortParam.SBSD, SortParam.SBDR, SortParam.SBDD);
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                refreshImagesList(newValue);
                if (!comboBoxClicked)
                    setComboBoxClicked(true);
            }
        });
    }

    private void createSearchText() {// 检测到回车调用搜索方法
        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                searchImage();
        });
    }

    private void createPathText() {// 检测到回车 跳转到对应文件夹
        pathTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                gotoPath();
        });
    }

    private void createPasteButton() {
        if (SelectedModel.getSourcePath() == null || SelectedModel.getCopyOrCut() == -1) {//没有选择要复制或移动的文件
            pasteButton.setDisable(true);//粘贴按钮设置为禁用状态
        }
    }

    //各按钮事件操作------------------

    /**
     * 前进、后退和返回父目录
     */
    @FXML
    private void moveBack() {
        if (!pathStack1.isEmpty()) {
            if (pathStack1.peek().equals(currentPath)) {
                pathStack1.pop();
            }
            String path = pathStack1.pop();
            pathStack2.push(currentPath);
            placeImages(ImageSortModel.renewList(path), path);
        } else {
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("后退到底啦"));
        }
    }

    @FXML
    private void moveForward() {
        if (!pathStack2.isEmpty()) {
            String path = pathStack2.pop();
            pathStack1.push(currentPath);
            placeImages(ImageSortModel.renewList(path), path);
        } else {
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("前进到尽头啦"));
        }
    }

    @FXML
    private void toParentDir() {
        String parent;
        if (currentPath.lastIndexOf("\\") == 2) {
            if (currentPath.length() == 3) {
                snackbar.enqueue(new JFXSnackbar.SnackbarEvent("到达根目录"));
                return;
            } else {
                parent = currentPath.substring(0, currentPath.lastIndexOf("\\") + 1);
            }
        } else {
            parent = currentPath.substring(0, currentPath.lastIndexOf("\\"));
        }
        placeImages(ImageSortModel.renewList(parent), parent);
        pathStack1.push(parent);
    }

    /**
     * 地址栏导航操作
     */
    @FXML
    private void gotoPath() {
        String path = pathTextField.getText();
        //用于处理以反斜杠 "\" 结尾的情况，需去掉反斜杠
        //(导航至磁盘根目录 即路径地址长度等于3的情况 除外)
        if (path.endsWith("\\") && path.length() != 3)
            path = path.substring(0, path.length() - 1);

        File directory = new File(path);
        if (!directory.exists()) {
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("路径不正确"));
        } else {
            ArrayList<ImageModel> list = ImageSortModel.renewList(path);
            // placeImages方法中已处理列表为空时的情况
            if (list != null)
                placeImages(list, path);
        }
    }

    /**
     * 刷新按钮操作
     */
    @FXML
    private void refresh() {
        unSelectAll();
        closeSearch();
        refreshImagesList(sortComboBox.getValue());
        snackbar.enqueue(new JFXSnackbar.SnackbarEvent("已刷新"));
    }

    /**
     * 粘贴按钮操作
     */
    @FXML
    private void paste() {
        SelectedModel.pasteImage(currentPath);
        if (SelectedModel.getHavePastedNum() == SelectedModel.getWaitingPasteNum()) {
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("粘贴成功"));
            refreshImagesList(sortComboBox.getValue());
        }
        if (SelectedModel.getSourcePath() == null || SelectedModel.getCopyOrCut() == -1) {
            pasteButton.setDisable(true);
        }
    }

    @FXML
    private void searchImage() {//搜索并统计图片
        String key = searchTextField.getText();
        if (key.equals("")) return; // 搜索内容为空时即返回
        ArrayList<ImageModel> result = SearchImageModel.fuzzySearch(key, curImgList);
        placeImages(result, currentPath);
        if (result.size() == 0) {
            folderInfoLabel.setText("未找到图片");
            showNotFoundPage();
        } else {
            folderInfoLabel.setText("共找到 " + result.size() + " 个结果");
        }
        closeSearchButton.setVisible(true);
    }


    @FXML
    private void closeSearch() {//关闭搜索并重新加载图片显示
        closeSearchButton.setVisible(false);
        searchTextField.setText("");
        refreshImagesList(sortComboBox.getValue());
    }


    @FXML
    private void selectAll() {//全选
        //如果没有可选的内容
        if (imageListPane.getChildren().isEmpty()) {
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent("无内容可选"));
            return;
        }
        //避免重复选择，先清空已选
        SelectionModel.clear();
        for (Node node : imageListPane.getChildren()) {
            ImageBox imageBox = (ImageBox) node;
            imageBox.getCheckBox().setSelected(true);
        }
    }

    @FXML
    private void unSelectAll() {//取消所有选择的图片
        SelectionModel.clear();
    }

    /**
     * 展示软件关于页面
     */
    @FXML
    private void showAboutDetail() {
        VBox vBox = new VBox();

        ImageView icon = new ImageView(new Image("imageapex/main/resources/iconsOfMain/app_icon_300px.png"));
        icon.setFitHeight(100);
        icon.setPreserveRatio(true);

        Label author = new Label("Author:\n" + "陈志超  杨生源  温鑫\n\n");
        author.getStyleClass().add("normal-text-b");
        author.setTextAlignment(TextAlignment.CENTER);

        String repo = "感谢您的使用！\n" +
                "Thank you for your use!\n";

        JFXTextArea bodyTextArea = new JFXTextArea(repo);
        bodyTextArea.getStyleClass().addAll("dialog-text-area", "dialog-body");
        bodyTextArea.setEditable(false);
        bodyTextArea.setPrefHeight(100);

        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(icon, author, bodyTextArea);

        DialogBox dialog = new DialogBox(this, DialogType.INFO, null, "关于 ImageApex");
        dialog.setBodyContent(vBox);
        dialog.show();
    }

}
