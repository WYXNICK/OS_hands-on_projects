package Implementation;

import java.awt.*;

import javax.swing.*;


public class SpeedChoose extends JPanel {
	public JTextField speedInput = new JTextField();
	public JButton getSpeedButton = new JButton();
//	public JLabel outputLabel = new JLabel();

    public SpeedChoose() {
        this.setSize(400, 30);
        this.setLayout(null);
        this.setBackground(new Color(217, 244, 223));
        add(speedInput);
        speedInput.setBounds(80, 0, 100, 30);
        speedInput.setBackground(new Color(217, 244, 223));
        speedInput.setText("500");
        
        
        add(getSpeedButton);
        getSpeedButton.setBounds(5,0,70,30);
        getSpeedButton.setBackground(new Color(154,155,198));
        getSpeedButton.setText("确认");
        getSpeedButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));

        // 添加单位
        JLabel unitLabel = new JLabel(" 执行速度(ms)");
        add(unitLabel);
        unitLabel.setBounds(180, 0, 150, 30);
        unitLabel.setForeground(new Color(40, 100, 156));
    }

}

