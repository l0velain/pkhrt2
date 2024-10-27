package ui;

import cartridge.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class window extends JFrame implements ActionListener {
    private static final JButton open = new JButton("Open");
    private static final JButton save = new JButton("Save");
    private static final JRadioButton male = new JRadioButton("Male");
    private static final JRadioButton female = new JRadioButton("Female");
    private static final JTextField name = new JTextField(12);
    private static final JLabel label = new JLabel("Last saved: (never)", SwingConstants.CENTER);
    private static final JPanel namePanel = new JPanel();
    private static final JPanel textPanel = new JPanel();
    private static final JPanel radioPanel = new JPanel();
    private static final JPanel buttonPanel = new JPanel();
    private static final ButtonGroup radioGroup = new ButtonGroup();

    private static final JFileChooser ofd = new JFileChooser();

    private static File savefile = null;
    private static player p = null;

    public window(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        textPanel.add(name);
        namePanel.setLayout(new BorderLayout());
        namePanel.add(label, BorderLayout.NORTH);
        namePanel.add(textPanel, BorderLayout.SOUTH);
        radioPanel.add(male);
        radioPanel.add(female);
        buttonPanel.add(open);
        buttonPanel.add(save);

        radioGroup.add(male);
        radioGroup.add(female);

        ofd.setFileFilter(new FileNameExtensionFilter("NDS savefile","sav", "SAV"));

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(namePanel, BorderLayout.NORTH);
        this.getContentPane().add(radioPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        name.setDocument(new textlimit(7));
        label.setEnabled(false);
        name.setEnabled(false);
        male.setEnabled(false);
        female.setEnabled(false);
        save.setEnabled(false);

        open.addActionListener(this);
        save.addActionListener(this);

        this.pack();
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if((JButton) e.getSource() == open) {
            int res = ofd.showOpenDialog(this);
            if(res == JFileChooser.APPROVE_OPTION) {
                save.setText("Save");
                savefile = ofd.getSelectedFile();
                if(cartridge.isValid(savefile)) {
                    p = new player(savefile);
                    if(p.getGender() == player.MALE)
                        male.setSelected(true);
                    else if(p.getGender() == player.FEMALE)
                        female.setSelected(true);
                    male.setEnabled(true);
                    female.setEnabled(true);
                    name.setEnabled(true);
                    save.setEnabled(true);
                    name.setText(p.getName());
                } else
                    JOptionPane.showMessageDialog(null, "Please select a valid savefile from Pokémon White 2/Black 2.", "Error",JOptionPane.ERROR_MESSAGE);
            }
        } else if((JButton) e.getSource() == save) {
            if(male.isSelected())
                p.setGender(player.MALE);
            else if(female.isSelected())
                p.setGender(player.FEMALE);
            p.setName(name.getText());
            p.writeGender(savefile);
            p.writeName(savefile);
            checksum c = new checksum(savefile);
            c.updateChecksums();
            Date time = new java.util.Date(System.currentTimeMillis());
            label.setText("  Last saved: " + new SimpleDateFormat("HH:mm:ss").format(time));
        }
    }
}
