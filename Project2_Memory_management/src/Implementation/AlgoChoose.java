package Implementation;

import java.awt.Color;

import javax.swing.*;

public class AlgoChoose extends JPanel {
	public String[] items = new String[] { " FIFO(先进先出算法)", " LRU(最近最少使用页面淘汰算法)"};
	public JList<String> chooseList = new JList<String>(items); // 创建JList		
	public AlgoChoose() {
		setLayout(null);
		setBounds(550, 60, 330, 120);
		this.setBackground(new Color(91,118,198));
		// 添加标题
		JLabel label=new JLabel("页面置换算法选择",JLabel.CENTER);
		add(label);
		label.setBounds(0, 0, 330, 40);
		label.setForeground(Color.WHITE);
		// 添加选项列表
		add(chooseList);
		chooseList.setBounds(0, 42, 330, 80);
		chooseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chooseList.setBackground(new Color(91,118,198));
		chooseList.setForeground(Color.WHITE);
	}
}
