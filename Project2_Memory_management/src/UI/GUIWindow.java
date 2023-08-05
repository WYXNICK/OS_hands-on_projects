package UI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import Implementation.AlgoChoose;
import Implementation.Memory;
import Implementation.Moniter;
import Implementation.SpeedChoose;
import Implementation.WaitingList;

import java.text.DecimalFormat;
import java.math.RoundingMode;


public class GUIWindow extends JFrame {
    public AlgoChoose algSelBar = new AlgoChoose();
    static public Memory memory = new Memory();
    public Moniter moniter = new Moniter();
    public WaitingList waitingList = new WaitingList();
    static public JButton startButton = new JButton("开始执行");
    static public JButton clearButton = new JButton("清零重置");
    public boolean[] ins = new boolean[320];
    static public int speed = 500;
    JLabel lossnumLabel = new JLabel("<html>缺页数：<br>0<html>");
    JLabel lossrateLabel = new JLabel("缺页率：");

    private volatile boolean isRunning = true; // 判断进程是否重置

    public static void main(String args[]) {
        InitGlobalFont(new Font("Microsoft YaHei", Font.BOLD, 18)); // 统一设置字体
        GUIWindow f = new GUIWindow();
        f.setVisible(true);
    }

    public GUIWindow() {
        // 设置框架的基本属性
        setTitle("OS内存管理");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(135, 25, 900, 600);
        getContentPane().setLayout(null);
        getContentPane().setBackground(new Color(217, 244, 223));

        // 添加选择列表
        algSelBar.setLocation(5, 30);
        getContentPane().add(algSelBar);
        algSelBar.chooseList.setSelectedIndex(0);

        // 添加内存块
        getContentPane().add(memory);
        memory.setLocation(345, 30);

        // 添加显示器
        getContentPane().add(moniter);
        moniter.setLocation(5, 215);

        // 添加等待执行的指令队列
        getContentPane().add(waitingList);
        waitingList.setLocation(5, 320);

        //添加缺页标签
        getContentPane().add(lossnumLabel);
        getContentPane().add(lossrateLabel);
        lossnumLabel.setBackground(Color.black);
        lossrateLabel.setBackground(Color.black);
        lossnumLabel.setBounds(210, 320, 120, 70);
        lossrateLabel.setBounds(210, 395, 130, 70);


        // 添加开始按钮
        getContentPane().add(startButton);
        startButton.setBounds(5, 160, 163, 50);
        startButton.setBackground(new Color(154, 155, 198));
        startButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        startButton.setForeground(Color.BLACK);
        startButton.addActionListener(e -> {
            waitingList.lists[0].setBackground(new Color(209, 182, 225));
            start();
        });


        // 添加重置按钮
        getContentPane().add(clearButton);
        clearButton.setBounds(172, 160, 163, 50);
        clearButton.setBackground(new Color(154, 155, 198));
        clearButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        clearButton.setForeground(Color.BLACK);
        clearButton.addActionListener(e -> {
            isRunning = false; // 关闭线程的执行
            clear_stimulation();
        });


        // 添加速度输入框和按钮
        SpeedChoose speedChoose = new SpeedChoose();
        this.getContentPane().add(speedChoose);
        speedChoose.setLocation(0, 525);
        speedChoose.getSpeedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    GUIWindow.speed = Integer.parseInt(speedChoose.speedInput.getText());
                    // 在这里使用转换后的速度值进行处理
                    System.out.println("获取的速度为：" + speed);
                } catch (NumberFormatException e1) {
                    // 处理转换失败的情况
                    System.out.println("无效的速度值：" + speedChoose.speedInput.getText());
                }
            }
        });


    }

    // start
    public void start() {
        this.startButton.setEnabled(false);
        int index = this.algSelBar.chooseList.getSelectedIndex();
        if (0 == index) {
            isRunning = true;
            FIFO_Algorithm();
        } else {
            isRunning=true;
            LRU_Algorithm();
        }
    }

    // clear_stimulation
    public void clear_stimulation() {
        waitingList.lists[0].setBackground(new Color(169, 193, 202));// 清除等待队列的高亮
        GUIWindow.startButton.setEnabled(true);// 恢复开始按钮
        this.memory.clear();// 清除内存
        this.moniter.clear();// 清除显示器
        this.waitingList.clear();// 清空等待队列
        this.lossnumLabel.setText("<html>缺页数：<br>0<html>");
        this.lossrateLabel.setText("缺页率：");
        // 清除标识位
        for (int i = 0; i < 320; i++) {
            this.ins[i] = false;
        }
    }

    // FIFO
    public void FIFO_Algorithm() {
        new Thread(new Runnable() {
            // 要实时更新JLabel，所以需要单独开一个线程来刷新线程
            public void run() {
                int cnt_miss = 0;// 记录缺页次数
                int turn = 0;// 用来记录轮转到哪一页
                int num = -1;// 当前要执行的指令的编号
                // 先生成前四个，加到等待队列
                for (int i = 0; i < 4; i++) {
                    if (!isRunning) break;
                    num = next(i, num);
                    waitingList.turn(num);
                }

                // 逐个执行320条指令
                for (int i = 0; i < 320; i++) {
                    if (!isRunning) break;
                    num = waitingList.insNum[0];
                    // 判断在不在里面
                    if (memory.check(num) != -1) {
                        // 在内存中，显示信息
                        show(memory.check(num), num, true, -1);
                    } else {
                        // 不在内存中，调度
                        show(memory.check(num), num, false, turn);
                        memory.dispatchPage(turn, num / 10);

                        // 高亮显示目标指令所在位置
                        Memory.pages[turn].blocks[num % 10].setBackground(new Color(209, 182, 225));
                        try {
                            Thread.sleep(speed);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Memory.pages[turn].blocks[num % 10].setBackground(new Color(169, 193, 202));

                        // 修改相应计数器
                        turn++;
                        cnt_miss++;
                        if (turn > 3) {
                            turn = turn % 4;
                        }
                    }
                    if (isRunning) {
                        //显示缺页数和缺页率
                        lossnumLabel.setText("<html>缺页数：<br>" + cnt_miss + "</html>");
                        double missRate = (double) cnt_miss / (double) (i + 1) * 100;
                        DecimalFormat decimalFormat = new DecimalFormat("#.##");
                        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);  // 设置四舍五入
                        String formattedRate = decimalFormat.format(missRate);
                        lossrateLabel.setText("<html>缺页率：<br>" + formattedRate + "%</html>");


                        // 产生下一个待执行指令
                        if (i < 316) {
                            num = next(i, waitingList.insNum[3]);
                            // 修改等待队列
                            waitingList.turn(num);
                        } else {
                            waitingList.turn(-1);
                        }

                        // 休眠一段时间以方便观察
                        try {
                            Thread.sleep(speed);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    // LRU
    public void LRU_Algorithm() {
        new Thread(new Runnable() {
            // 要实时更新JLabel，所以需要单独开一个线程来刷新线程
            public void run() {
                int cnt_miss = 0;// 记录缺页次数
                int[] free = new int[4];// 记录每个物理页的空闲次数
                int num = -1;// 当前要执行的指令的编号
                // 先生成前四个，加入等待队列
                for (int i = 0; i < 4; i++) {
                    if (!isRunning) break;
                    num = next(i, num);
                    waitingList.turn(num);
                }

                // 逐个执行320条指令
                for (int i = 0; i < 320; i++) {
                    if (!isRunning) break;
                    num = waitingList.insNum[0];
                    // 判断在不在里面
                    if (memory.check(num) != -1) {
                        // 在内存中
                        // 现将各页闲置次数加一
                        for (int j = 0; j < 4; j++) {
                            free[j]++;
                        }
                        // 再将当前执行的页限制次数置为零
                        free[memory.check(num)] = 0;
                        // 显示信息
                        show(memory.check(num), num, true, -1);
                    } else {
                        // 不在内存中，调度
                        int turn = 0;// 要调度的页的物理页号
                        int longest = 0;// 最长闲置时间
                        // 判断调度哪一页
                        for (int j = 0; j < 4; j++) {
                            if (free[j] > longest) {
                                turn = j;
                                longest = free[j];
                            }
                        }
                        // 现将各页闲置次数加一
                        for (int j = 0; j < 4; j++) {
                            free[j]++;
                        }
                        // 再将当前执行的页限制次数置为零
                        free[turn] = 0;
                        // 修改相应计数器
                        cnt_miss++;
                        // 显示信息
                        show(memory.check(num), num, false, turn);
                        // 调度
                        memory.dispatchPage(turn, num / 10);
                        // 高亮显示目标指令所在位置
                        Memory.pages[turn].blocks[num % 10].setBackground(new Color(209, 182, 225));
                        try {
                            Thread.sleep(speed);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Memory.pages[turn].blocks[num % 10].setBackground(new Color(169, 193, 202));


                    }
                    if (isRunning) {
                        //显示缺页数和缺页率
                        lossnumLabel.setText("<html>缺页数：<br>" + cnt_miss + "</html>");
                        double missRate = (double) cnt_miss / (double) (i + 1) * 100;
                        DecimalFormat decimalFormat = new DecimalFormat("#.##");
                        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);  // 设置四舍五入
                        String formattedRate = decimalFormat.format(missRate);
                        lossrateLabel.setText("<html>缺页率：<br>" + formattedRate + "%</html>");
                        // 产生下一个待执行指令
                        if (i < 316) {
                            num = next(i, waitingList.insNum[3]);
                            // 修改等待队列
                            waitingList.turn(num);
                        } else {
                            waitingList.turn(-1);
                        }

                        // 休眠一段时间以方便观察
                        try {
                            Thread.sleep(speed);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    // 展示在内存中的信息
    public void show(int pageNum, int num, boolean tag, int remove) {
        /*
         * pageNum内存块号 num指令编码 tag是否命中 remove调出页的页号
         */
        if (tag) {
            // 如果命中
            this.moniter.showInf(num, true, -1);// 展示信息
            // 高亮显示指令所在位置
            this.memory.pages[pageNum].blocks[num % 10].setBackground(new Color(209, 182, 225));
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.memory.pages[pageNum].blocks[num % 10].setBackground(new Color(169, 193, 202));
        } else {
            // 缺页
            int rem = this.memory.pages[remove].lPage;// 计算所调的逻辑页郝
            this.moniter.showInf(num / 10, false, rem);// 显示调页信息
        }

    }

    // 指令访问次序，生成下一个指令
    public int next(int cnt, int last) {
        // cnt产生的第几个随机数[0,319]
        // last上一条执行的指令序号
        // 返回下一条执行的指令序号next
        int next = -1;

        // 上一条是-1表示当前是第一条指令
        // 则从0~319中随机产生一条
        if (last == -1) {
            next = (int) (Math.random() * 320);
            return next;
        }

        // 奇数条指令随机产生
        if (cnt % 2 == 0) {
            // 跳转到前地址部分
            if (cnt % 4 == 2) {
                next = (int) (Math.random() * last);
                int times = 0;
                while (true == this.ins[next]) {
                    times++;
                    next = (int) (Math.random() * last);
                    // 如果last前面全部执行过了
                    // 就从所有指令中随机产生下一条
                    if (times > last - 1) {
                        while (true == this.ins[next]) {
                            next = (int) (Math.random() * 320);
                        }
                    }
                }
                // 修改记录的标签位表示此指令已执行过
                this.ins[next] = true;
            }
            // 跳转到后地址部分
            else {
                next = (int) (last + Math.random() * (320 - last));
                int times = 0;
                while (true == this.ins[next]) {
                    times++;
                    next = (int) (last + Math.random() * (320 - last));
                    // 如果last后面全部执行过了
                    // 就从所有指令中随机产生下一条
                    if (times > (319 - last)) {
                        while (true == this.ins[next]) {
                            next = (int) (Math.random() * 320);
                        }
                    }
                }
                // 修改记录的标签位表示此指令已执行过
                this.ins[next] = true;
            }
        }
        // 第偶数条指令顺序产生——last之后第一条未执行的指令
        else {
            next = last + 1;
            // 如果超过319则从头开始
            if (next > 319) {
                next = next % 320;
            }
            while (true == this.ins[next]) {
                next++;
                if (next > 319) {
                    next = next % 320;
                }
            }
            // 修改记录的标签位表示此指令已执行过
            this.ins[next] = true;
        }
        return next;
    }

    // 设置全局字体
    private static void InitGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }
}
