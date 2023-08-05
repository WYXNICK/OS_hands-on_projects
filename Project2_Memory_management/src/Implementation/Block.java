package Implementation;

import java.awt.*;

import javax.swing.*;

public class Block extends JLabel {
	public Block() {
		setLayout(null);//设置布局方式
		setText("空");
		setHorizontalAlignment(SwingConstants.CENTER);
		this.setBackground(new Color(169,193,202));
		setOpaque(true);
		setForeground(Color.white);
		setFont(new Font("楷体", Font.BOLD, 20));
	}
}
