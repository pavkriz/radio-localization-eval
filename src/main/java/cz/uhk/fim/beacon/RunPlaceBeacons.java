package cz.uhk.fim.beacon;

import com.firebase.client.Firebase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Kriz on 29. 12. 2015.
 */
public class RunPlaceBeacons extends JFrame {
    JTextField floor = new JTextField("J3NP");
    JLabel img = new JLabel();
    Firebase myFirebaseRef = new Firebase("https://torrid-inferno-5053.firebaseio.com/");

    public RunPlaceBeacons() {
        add(floor, BorderLayout.NORTH);
        add(new JScrollPane(img), BorderLayout.CENTER);
        img.setIcon(new ImageIcon("img/J3NP-beacony.png"));
        img.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        img.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                imageMouseClicked(e);
            }
        });
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
    }

    private void imageMouseClicked(MouseEvent e) {
        String name = JOptionPane.showInputDialog(this, "ID beaconu mezera ID pro paper1 (napr. 39 1)");
        if (name != null && !name.equals("")) {
            String[] ids = name.split(" ");
            Firebase record = myFirebaseRef.child("beacons").child(ids[0]);
            record.child("floor").setValue(floor.getText());
            record.child("x").setValue(e.getX());
            record.child("y").setValue(e.getY());
            record.child("paper1Number").setValue(Integer.parseInt(ids[1]));
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater( ()-> {
            new RunPlaceBeacons().setVisible(true);
        });
    }
}