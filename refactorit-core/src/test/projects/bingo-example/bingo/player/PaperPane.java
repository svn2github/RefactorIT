package bingo.player;

import java.awt.*;
import javax.swing.*;

/**
 * Ideally, this class should display a tiled "paper" image.
 * Currently, it just displays a solid background.
 */
public class PaperPane extends JPanel {
    PaperPane(Image image) {
        super(false);
        setBackground(new Color(1.0f, 0.99f, 0.89f));
    }
}
