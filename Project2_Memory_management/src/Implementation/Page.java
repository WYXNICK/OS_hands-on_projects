package Implementation;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class Page extends JPanel {
	public Block[] blocks = new Block[10];
	static int cnt = 0;
	public JLabel logicalPage = new JLabel("第-页", JLabel.CENTER);
	public int lPage = -1;

	public Page() {
		this.setSize(100, 480);
		this.setOpaque(true);
		this.setLayout(null);
		this.setBackground(new Color(75,205,231));
		this.logicalPage.setFont(new Font("楷体", Font.BOLD, 20));
		JLabel label = new JLabel("内存块" + cnt , JLabel.CENTER);
		label.setFont(new Font("楷体", Font.BOLD, 20));
		label.setForeground(Color.white);
		cnt++;
		add(label);
		label.setBounds(0, 0, 100, 30);
		for (int i = 0; i < 10; i++) {
			Block b = new Block();
			add(b);
			b.setBounds(0, (i + 1) * 40, 100, 30);
			blocks[i] = b;
		}

		logicalPage.setForeground(Color.white);
		add(logicalPage);
		logicalPage.setBounds(0, 450, 100, 30);
	}

	public void change(int logicalPage) {
		this.lPage = logicalPage;
		if(lPage!=-1)
		    this.logicalPage.setText("第" + lPage + "页");
		else
			this.logicalPage.setText("第-页");
		for (int i = 0; i < 10; i++) {
			this.blocks[i].setText("" + (lPage * 10 + i));
		}
	}

	public void clear() {
		// 清空页
		for (int i = 0; i < 10; i++) {
			blocks[i].setText("空");
		}
		lPage = -1;
		this.logicalPage.setText("第-页");
	}

	public int getLogicalPage() {
		return lPage;
	}
}
