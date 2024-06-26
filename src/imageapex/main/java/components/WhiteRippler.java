package imageapex.main.java.components;

import com.jfoenix.controls.JFXRippler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * 继承自{@link JFXRippler}，设置特定样式
 * */
public class WhiteRippler extends JFXRippler {
    public WhiteRippler(Node control) {
        super(control);
        super.setRipplerFill(Color.WHITE);
        setAlignment(Pos.BOTTOM_CENTER);
        setPrefHeight(170);
        setPrefWidth(170);
    }
}
