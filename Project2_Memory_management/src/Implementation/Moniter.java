package Implementation;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class Moniter extends JPanel {
	static JLabel result=new JLabel("",JLabel.CENTER);
	JLabel inf=new JLabel("",JLabel.CENTER);
	public Moniter() {
		this.setLayout(null);
		
		setSize(330, 100);
		setBackground(new Color(91,118,198));
		JLabel head = new JLabel("模拟过程", JLabel.CENTER);
		
		add(head);
		head.setBounds(0, 10, 330, 30);
		head.setBackground(new Color(91,118,198));
		head.setOpaque(true);
		head.setForeground(Color.WHITE);
		
		add(result);
		result.setBounds(0, 50, 330, 30);
		result.setBackground(new Color(91,118,198));
		result.setOpaque(true);
		result.setForeground(Color.WHITE);
		
		add(inf);
		inf.setBounds(0, 90, 330, 30);
		inf.setBackground(new Color(91,118,198));
		inf.setOpaque(true);
		inf.setForeground(Color.WHITE);
	}
	
	public void showInf(int ins,boolean IsContained,int remove) {
		/*
		 * ins——当前执行代码所在的逻辑页页号
		 * IsContained——该页是否在内存中
		 * remove——将要移出的逻辑页页号
		 */
		if(IsContained) {
			// 在内存中，显示信息
			if(ins!=-1)
			    this.result.setText("第"+ins+"条指令在内存中，不需要调度");
			else
				this.result.setText("第-条指令在内存中，不需要调度");
			this.inf.setText("");
		}else {
			// 不在内存中，显示信息
			if(remove!=-1)
			    this.result.setText("调出第"+remove+"页，调入第"+ins+"页");
			else
				this.result.setText("调出第-页，调入第"+ins+"页");
			this.inf.setText("");
		}
	}
	
	
	public void clear() {
		// 清空显示器
		this.result.setText("");
		this.inf.setText("");
	}
}
