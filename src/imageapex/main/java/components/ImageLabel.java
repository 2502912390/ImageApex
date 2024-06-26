package imageapex.main.java.components;

import javafx.scene.control.Label;
import javafx.scene.text.Font;


import static javafx.geometry.Pos.CENTER;

/**
 * 图片文件名标签，添加特定样式
 */
public class ImageLabel extends Label {

    public ImageLabel(String text) {
        super(text);
        setAlignment(CENTER);
        setFont(Font.font(16));
//        super.setWrapText(true);
        setStyle("-fx-padding:15 0 0 0;");
    }

}
