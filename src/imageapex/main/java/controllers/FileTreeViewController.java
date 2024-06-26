package imageapex.main.java.controllers;

import com.jfoenix.controls.JFXTreeView;
import imageapex.main.java.model.SortParam;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FileTreeViewController implements Initializable {//文件目录树类别

    @FXML
    private JFXTreeView<File> fileTreeView;//显示树形数据结构

    private HomeController hc;

    public FileTreeViewController() {
        //将本类的实例添加到全局映射中
        ControllerInstance.controllers.put(this.getClass().getSimpleName(), this);
        hc = (HomeController) ControllerInstance.controllers.get(HomeController.class.getSimpleName());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setFileTreeView();
    }

    private void setFileTreeView() {//以根节点构建目录树
        //定义目录树
        File[] rootList = File.listRoots();//获取系统中所有的根目录 C D F。。
        TreeItem<File> mainTreeItem = new TreeItem<>(rootList[0]);

        for (File root : rootList) {//遍历每个根节点 为每个根节点添加其子节点
            TreeItem<File> rootItem = new TreeItem<>(root);//以根目录为树
            try {
                addItems(rootItem, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mainTreeItem.getChildren().add(rootItem);
        }

        fileTreeView.setRoot(mainTreeItem);
        fileTreeView.setShowRoot(false);

        //根据文件是否展开来显示不同的文件夹图标
        fileTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {//设置单元格工厂
            @Override
            public TreeCell<File> call(TreeView<File> param) {
                TreeCell<File> treeCell = new TreeCell<File>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        if (!empty) {
                            super.updateItem(item, empty);
                            HBox hBox = new HBox();//用于存放图标和标签

                            Label label = new Label(isListRoots(item));//获取目录对应的文本 根目录需要特殊判断
                            this.setGraphic(hBox);

                            if (this.getTreeItem().isExpanded()) {//设置展开图标
                                ImageView folderImage = new ImageView("imageapex/main/resources/iconsOfMain/opened_folder.png");
                                folderImage.setPreserveRatio(true);
                                folderImage.setFitWidth(22);
                                hBox.getChildren().add(folderImage);//加图片
                                this.setGraphic(hBox);
                            } else if (!this.getTreeItem().isExpanded()) {//设置默认图标
                                ImageView folderImage = new ImageView("imageapex/main/resources/iconsOfMain/folder.png");
                                folderImage.setPreserveRatio(true);
                                folderImage.setFitWidth(22);
                                hBox.getChildren().add(folderImage);//加图片
                                this.setGraphic(hBox);
                            }
                            hBox.getChildren().add(label);//加文字
                        } else if (empty) {
                            this.setGraphic(null);
                        }
                    }
                };
                return treeCell;
            }
        });

        //获取点击操作并刷新当前结点
        fileTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<File>>() {//添加了一个监听器
            @Override
            public void changed(ObservableValue<? extends TreeItem<File>> observable, TreeItem<File> oldValue, TreeItem<File> newValue) {
                //此处点击可获得文件夹绝对路径
                String path = newValue.getValue().getAbsolutePath();
                System.out.println(path);
                try {
                    //入栈以便于后续前进后退
                    hc.initEnterFolder(path);
                    // 只要点击一次排序后以后每次进入新页面就置为"按名字升序"
                    hc.getSortComboBox().setValue(SortParam.SBNR);
                    addItems(newValue, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //待优化 寻找更合适的算法
    public void addItems(TreeItem<File> in, int flag) throws IOException {//向树视图中的指定节点(in)添加子节点 添加flag用于控制递归调用的深度 太大的话会加载很慢
        File[] filelist = in.getValue().listFiles();//当前节点所代表的目录中的所有文件和子目录
        //flag判断当前遍历的层数
        if (filelist != null) {
            if (flag == 0) {//第一层级的遍历，会先移除当前节点的所有子节点
                in.getChildren().remove(0, in.getChildren().size());
            }
            if (filelist.length > 0) {
                for(File f:filelist){
                    if(f.isDirectory()&!f.isHidden()){//是一个目录且不是隐藏文件
                        TreeItem<File> newItem = new TreeItem<File>(f);//为该目录创建节点
                        if (flag < 2) {//设置为2还能接受
                            addItems(newItem, flag + 1);
                        }
//                        addItems(newItem, flag + 1); //fortest
                        in.getChildren().add(newItem);//将新创建的节点添加为当前节点的子节点
                    }
                }
            }
        }
    }

    public String isListRoots(File item) {//判断文件是否为根目录
        File[] rootlist = File.listRoots();
        for (File isListRoots : rootlist) {
            if (item.toString().equals(isListRoots.toString())) {
                return item.toString();
            }
        }
        return item.getName();
    }
}
