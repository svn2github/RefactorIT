package bingo.shared;

import java.awt.*;
import javax.swing.*;

public class Utilities {

    /**
     * Create a horizontal Box and add a group of evenly spaced
     * JComponents to it.
     */
    public static Box makeEvenlySpacedBox(JComponent compList[]) {
	Box box = Box.createHorizontalBox();
	int numComponents = compList.length;
        int i = 0;

        while (i < numComponents) {
            box.add(Box.createGlue());
            box.add(compList[i++]);
        }
        box.add(Box.createGlue());
	return box;
    }

    /**
     * Add a label-value pair to a container that uses
     * GridBagLayout.
     */
    public static void addParameterRow(Container container,
                                       JLabel label,
                                       Component component) {
        GridBagLayout gridbag = null;
        try {
            gridbag = (GridBagLayout)(container.getLayout());
        } catch (Exception e) {
            System.err.println("Hey!  You called addRow with"
                               + " a container that doesn't "
                               + " use GridBagLayout!");
            return;
        }

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        //c.weighty = 1.0;
        c.insets = new Insets(0, 5, 0, 5);

        gridbag.setConstraints(label, c);
        container.add(label);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        gridbag.setConstraints(component, c);
        container.add(component);
    }

}
