package package1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GUIwindow
{
	public static JLabel[] banner = new JLabel[9]; //顶部标签栏
	public static ElevatorSystem[] elevts = new ElevatorSystem[5]; //对应5部电梯
    public static JLabel[] labels = new JLabel[20]; //楼层标签
    public static ArrayList[] upqueue = new ArrayList[20], downqueue = new ArrayList[20]; //上升和下降请求队列
    public static JComboBox[] chooseFloor=new JComboBox[20]; //楼层选择按键
    public static JButton[][] elev = new JButton[5][20];//每部电梯对应的每个楼层的状态
    public static JButton[] upBt = new JButton[20];//上楼按键
    public static JButton[] downBt = new JButton[20];//下楼按键   
    public static JLabel[] showfloor=new JLabel[5];  //数码显像管显示楼层数
    public static TextArea logs = new TextArea();//输出提示信息
    public static ArrayList<ElevatorSystem> elevators = new ArrayList<ElevatorSystem>();//电梯组
   
    public static boolean[] upqueLock = new boolean[20], downqueLock = new boolean[20];

    private static JButton showHelp=new JButton();//选择查看帮助按钮
    private static String Help =
    	    "<html>" +
    	    "<h2>帮助：</h2>" +
    	    "<ul>" +
    	    "<li>左侧为电梯控制及演示区，右侧为电梯运行信息区</li>" +
    	    "<li>测试模式：直接点击某层楼层选择框内的楼层数，表示该层有人想前往对应楼层</li>" +
    	    "<li>乘客模式：乘客先在某层的点击上升按钮或下降按钮，再在对应楼层选择框内选择想要前往的内层</li>" +
    	    "<li>点击下方第一行的按钮可控制电梯处于“正常”或者“报警”状态，故障电梯将无法使用</li>" +
    	    "<li>点击下方第二行的按钮可控制对应电梯处于开门、关门状态</li>" +
    	    "<li>在对应楼层点击按钮后，该楼层标签会变为绿色，表示该层楼有乘客</li>" +
    	    "</ul>" +
    	    "<p>&nbsp;&nbsp;祝您使用愉快!</p>" +
    	    "</html>";




    public static void init()
    {
    	
    	ImageIcon originIcon = new ImageIcon(GUIwindow.class.getResource("/images/数字1.png"));
        ImageIcon icon = new ImageIcon(originIcon.getImage().getScaledInstance(23, 25, Image.SCALE_SMOOTH));
        
        ImageIcon originIcon1 = new ImageIcon(GUIwindow.class.getResource("/images/上箭头.png"));
        ImageIcon up_arrow = new ImageIcon(originIcon1.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        
        ImageIcon originIcon2 = new ImageIcon(GUIwindow.class.getResource("/images/下箭头.png"));
        ImageIcon down_arrow = new ImageIcon(originIcon2.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        
        ImageIcon originIcon3 = new ImageIcon(GUIwindow.class.getResource("/images/help.png"));
        ImageIcon get_help = new ImageIcon(originIcon3.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        
        showHelp.setBackground(Color.WHITE);
        showHelp.setIcon(get_help);
        showHelp.addActionListener(e -> {
        	// 查看帮助窗口
            JOptionPane.showMessageDialog(null, Help, "Help", JOptionPane.DEFAULT_OPTION);
        });
        
        for (int i = 0; i < 20; i++)
        {
            // 队列锁初始化
            upqueLock[i] = true;
            downqueLock[i] = true;

            // 楼号初始化
            labels[i] = new JLabel(String.valueOf(i + 1));
            labels[i].setBackground(Color.WHITE);
            labels[i].setOpaque(true);
            labels[i].setHorizontalAlignment(JLabel.CENTER);

            // 请求队列初始化
            // 上升等待队列
            upqueue[i] = new ArrayList<Integer>();
            // 下降等待队列
            downqueue[i] = new ArrayList<Integer>();
            
         // 上楼键初始化
            upBt[i] = new JButton();
            for(int j=0;j<20;j++) {
            	upBt[i].setBackground(new Color(255,208,232));
            	upBt[i].setIcon(up_arrow);
            }
            final int nowfloor=i;
            upBt[i].addActionListener(e -> {
            		upqueue[nowfloor].add(nowfloor+1);
                    labels[nowfloor].setBackground(Color.GREEN);
                    logs.append("第" + (nowfloor + 1) + "楼有人要上楼\n");
            });
            
         // 下楼键初始化
            downBt[i] = new JButton();
            for(int j=0;j<20;j++) {
            	downBt[i].setBackground(new Color(255,208,232));
            	downBt[i].setIcon(down_arrow);
            }
            final int nowfloor1=i;
            downBt[i].addActionListener(e -> {
            		downqueue[nowfloor1].add(nowfloor1+1);
                    labels[nowfloor1].setBackground(Color.GREEN);
                    logs.append("第" + (nowfloor1 + 1) + "楼有人要下楼\n");
            });

         // 楼层选择键初始化
            chooseFloor[i] = new JComboBox();
            chooseFloor[i].addItem("楼层数");
            for(int k = 1; k <= 20; k++) {
            	if(k!=i+1)
            	   chooseFloor[i].addItem(String.valueOf(k));
            }
            final int finalI = i;
            chooseFloor[i].addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    if (ItemEvent.SELECTED == e.getStateChange() && !chooseFloor[finalI].getSelectedItem().toString().equals("楼层数"))
                    {
                    	if(Integer.parseInt(chooseFloor[finalI].getSelectedItem().toString())>finalI+1) {
                           upqueue[finalI].add(Integer.parseInt(chooseFloor[finalI].getSelectedItem().toString()));
                    	   logs.append("第" + (finalI + 1) + "楼有人要去" + upqueue[finalI] + "楼\n");
                    	}
                    	else if(Integer.parseInt(chooseFloor[finalI].getSelectedItem().toString())<finalI+1) {
                    	   downqueue[finalI].add(Integer.parseInt(chooseFloor[finalI].getSelectedItem().toString()));
                    	   logs.append("第" + (finalI + 1) + "楼有人要去" + downqueue[finalI] + "楼\n");
                    	}
                        labels[finalI].setBackground(Color.GREEN);
                        chooseFloor[finalI].setSelectedIndex(0);
                        
                    }
                }
            });
        }
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 20; j++) {
                elev[i][j] = new JButton("");
                elev[i][j].setOpaque(true);
                elev[i][j].setBackground(Color.gray);
            }
            
            elev[i][0].setBackground(Color.GREEN);
            
            elev[i][0].setText("正常");
            // 故障/正常
            elev[i][0].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton button = (JButton) e.getSource();
                    int elevatorIndex = -1;
                    // 根据点击的按钮确定是哪个电梯的故障/正常按钮
                    for (int k = 0; k < 5; k++) {
                        if (button == elev[k][0]) {
                            elevatorIndex = k;
                            break;
                        }
                    }
                    if (elev[elevatorIndex][0].getText().equals("正常")) {
                        elev[elevatorIndex][0].setText("报警");
                        elev[elevatorIndex][0].setBackground(Color.RED);
                        elevts[elevatorIndex].setCurrentState(-2);
                        logs.append("电梯" + (elevatorIndex + 1) + "故障,暂时无法使用\n");
                    } else {
                        elev[elevatorIndex][0].setText("正常");
                        elev[elevatorIndex][0].setBackground(Color.GREEN);
                        elevts[elevatorIndex].setCurrentState(2);
                        logs.append("电梯" + (elevatorIndex + 1) + "恢复正常运行\n");
                    }
                }
            });

            // 开门/关门
            elev[i][1].setText("关门");
            elev[i][1].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton button = (JButton) e.getSource();
                    int elevatorIndex = -1;
                    // 根据点击的按钮确定是哪个电梯的开门/关门按钮
                    for (int k = 0; k < 5; k++) {
                        if (button == elev[k][1]) {
                            elevatorIndex = k;
                            break;
                        }
                    }
                    if (elev[elevatorIndex][1].getText().equals("关门")) {
                        elev[elevatorIndex][1].setText("开门");
                        elev[elevatorIndex][1].setBackground(Color.YELLOW);
                        logs.append("电梯" + (elevatorIndex + 1) + "开门\n");
                    } else {
                        elev[elevatorIndex][1].setText("关门");
                        elev[elevatorIndex][1].setBackground(Color.GRAY);
                        logs.append("电梯" + (elevatorIndex + 1) + "关门\n");
                    }
                }
            });
        }
        
        //为数码显像管设置初始电梯楼层
        for(int i=0;i<5;i++) {
        	showfloor[i] = new JLabel();
        	showfloor[i].setBackground(new Color(135, 206, 250)); // 淡蓝色
        	showfloor[i].setOpaque(true);//设置为不透明
        	showfloor[i].setIcon(icon);
        	showfloor[i].setHorizontalAlignment(JLabel.CENTER);
        }

        // 初始化电梯并加入电梯组
        elevts[0] = new ElevatorSystem(1, 0, elev[0],showfloor[0]);
        elevators.add(elevts[0]);
        elevts[1] = new ElevatorSystem(2, 0, elev[1],showfloor[1]);
        elevators.add(elevts[1]);
        elevts[2] = new ElevatorSystem(3, 0, elev[2],showfloor[2]);
        elevators.add(elevts[2]);
        elevts[3] = new ElevatorSystem(4, 0, elev[3],showfloor[3]);
        elevators.add(elevts[3]);
        elevts[4] = new ElevatorSystem(5, 0, elev[4],showfloor[4]);
        elevators.add(elevts[4]);
        
        JFrame frame = new JFrame("电梯系统");
        frame.setLayout(new GridLayout(1, 2));
        GridLayout grid = new GridLayout(22, 9);//设置窗口布局为22行9列
        Container myCon = new Container();
        myCon.setLayout(grid);
        // 使窗口在运行时自动最大化
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // 标签
        banner[0]=new JLabel("楼层");  
        banner[1]=new JLabel("电梯1");  
        banner[2]=new JLabel("电梯2");
        banner[3]=new JLabel("电梯3");
        banner[4]=new JLabel("电梯4");
        banner[5]=new JLabel("电梯5");
        banner[6]=new JLabel("上楼");
        banner[7]=new JLabel("下楼");
        banner[8]=new JLabel("楼层选择");
        for(int i=0;i<9;i++) {
        	banner[i].setHorizontalAlignment(JLabel.CENTER);
        	myCon.add(banner[i]);
        }

        // 按钮
        for (int i = 20; i > 0; i--)
        {
        	myCon.add(labels[i - 1]);
        	myCon.add(elev[0][i - 1]);
        	myCon.add(elev[1][i - 1]);
        	myCon.add(elev[2][i - 1]);
        	myCon.add(elev[3][i - 1]);
        	myCon.add(elev[4][i - 1]);
            if(i!=20)
            	myCon.add(upBt[i-1]);
            else
            	myCon.add(new JPanel());
            if(i!=1)
            	myCon.add(downBt[i - 1]);
            else 
            	myCon.add(new JPanel());
            myCon.add(chooseFloor[i - 1]);
        }
        myCon.add(new JLabel("电梯层数"));
        for(int i=0;i<5;i++) {
        	myCon.add(showfloor[i]);
        }
        myCon.add(showHelp);
        myCon.add(new JPanel());
        myCon.add(new JPanel()); 
        
        //设置输出电梯运行信息窗口
        logs.setEditable(false);
        logs.setFont(new Font("宋体",Font.BOLD,18));
        frame.add(myCon);
        JScrollPane pane = new JScrollPane(logs);
        frame.add(pane);

        frame.setSize(new Dimension(2000, 1500));
        frame.setVisible(true);
        
        //初次加载显示帮助窗口
    	JOptionPane.showMessageDialog(null, Help, "Help", JOptionPane.DEFAULT_OPTION);

        
    }

    static class ColorProcess extends Thread {
    	ColorProcess()
        {
            start();
        }
        public void run() {
            while (true) {
            	for (int i = 0; i < 20; i++)
                    if (upqueue[i].isEmpty() && downqueue[i].isEmpty()) labels[i].setBackground(Color.WHITE);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class elevatorManager extends Thread
    {
        elevatorManager()
        {
            start();
        }

        public void adjust(int index, int i) throws InterruptedException
        {
            // 最优调度电梯位于当前楼层下方
            if (elevators.get(index).getCurrentFloor()< i)
            {
                elevators.get(index).setCurrentState(1);
                elevators.get(index).addUp(i);
                elevators.get(index).setMaxUp(i);
                logs.append("电梯" + (index + 1) + "开始上升\n");
                Thread.sleep(700);
                return;
            }

            // 最优调度电梯位于当前楼层上方
            if (elevators.get(index).getCurrentFloor()> i)
            {
                elevators.get(index).setCurrentState(-1);
                elevators.get(index).addDown(i);
                elevators.get(index).setMinDown(i);
                logs.append("电梯" + (index + 1) + "开始下降\n");
                Thread.sleep(700);
                return;
            }

            // 最优调度电梯位于当前楼层
            if (elevators.get(index).getCurrentFloor() == i)
            {
                elevators.get(index).setCurrentState(1);
                logs.append("电梯" + (index + 1) + "启动\n");
                Thread.sleep(700);
                return;
            }
        }

        public void run()
        {  
            while (true)
            {
                for (int i = 0; i < 20; i++)
                {
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    while (!upqueLock[i]);
                    if (!upqueue[i].isEmpty()) //i楼上升请求队列不为空
                    {
                        int  selectid= -1,  distance = 1000000;

                        //检索可用且离i楼最近的电梯
                        for (int k = 0; k < 5; k++)
                        {
                            //电梯k停止
                            if (elevators.get(k).getCurrentState() == 0 && !upqueue[i].isEmpty())
                            {
                                if (Math.abs(elevators.get(k).getCurrentFloor() - i) < distance)
                                {
                                    selectid = k;
                                    distance = Math.abs(elevators.get(k).getCurrentFloor() - i);
                                }
                            }

                            //电梯k所在楼层高于等于i楼+电梯k为下降状态+电梯k要去的最低楼层高于等于i
                            if (elevators.get(k).getCurrentFloor() >= i && elevators.get(k).getCurrentState() == -1 && elevators.get(k).downMin() >= i)
                            {
                                if (Math.abs(elevators.get(k).getCurrentFloor() - i) < distance)
                                {
                                    selectid = -1;
                                    distance = Math.abs(elevators.get(k).getCurrentFloor() - i);
                                }
                            }

                            //电梯k所在楼层低于等于i楼+电梯k为上升状态+电梯k要去的最高楼层高于等于i
                            if (elevators.get(k).getCurrentFloor() <= i && elevators.get(k).getCurrentState() == 1 && elevators.get(k).upMax() >= i)
                            {
                                if (Math.abs(elevators.get(k).getCurrentFloor() - i) < distance)
                                {
                                    selectid = -1;
                                    distance = Math.abs(elevators.get(k).getCurrentFloor() - i);
                                }
                            }
                        }

                        if (selectid != -1 && !upqueue[i].isEmpty()) //有可用电梯
                        {
                            try
                            {
                                adjust(selectid, i);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }


                for (int i = 0; i < 20; i++)
                {
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }


                    while (!downqueLock[i]);
                    if (!downqueue[i].isEmpty()) //i楼下降请求队列不为空
                    {
                        int index = -1,  distance = 1000000;
                        //检索可用且离i楼最近的电梯
                        for (int k = 0; k < 5; k++)
                        {
                            //电梯k停止
                            if (elevators.get(k).getCurrentState() == 0 && !downqueue[i].isEmpty())
                            {
                                if (Math.abs(elevators.get(k).getCurrentFloor() - i) < distance)
                                {
                                    index = k;
                                    distance = Math.abs(elevators.get(k).getCurrentFloor() - i);
                                }
                            }

                            //电梯k所在楼层高于等于i楼+电梯k为下降状态+电梯k要去的最低楼层低于等于i
                            if (elevators.get(k).getCurrentFloor() >= i && elevators.get(k).getCurrentState() == -1 && elevators.get(k).downMin() <= i){
                                if (Math.abs(elevators.get(k).getCurrentFloor() - i) < distance)
                                {
                                    index = -1;
                                    distance = Math.abs(elevators.get(k).getCurrentFloor() - i);
                                }
                            }

                            //电梯k所在楼层低于等于i楼+电梯k为上升状态+电梯k要去的最高楼层低于等于i
                            if (elevators.get(k).getCurrentFloor() <= i && elevators.get(k).getCurrentState() == 1 && elevators.get(k).upMax() <= i){
                                if (Math.abs(elevators.get(k).getCurrentFloor() - i) < distance)
                                {
                                    index = -1;
                                    distance = Math.abs(elevators.get(k).getCurrentFloor() - i);
                                }
                            }
                        }

                        if (index != -1 && !downqueue[i].isEmpty())
                        {
                            try
                            {
                                adjust(index, i);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                try
                {
                    Thread.sleep(700);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args)
    {
        init();
        ColorProcess cp = new ColorProcess();
        elevatorManager elevatormanager = new elevatorManager();
        elevts[0].start();
        elevts[1].start();
        elevts[2].start();
        elevts[3].start();
        elevts[4].start();
    }
}