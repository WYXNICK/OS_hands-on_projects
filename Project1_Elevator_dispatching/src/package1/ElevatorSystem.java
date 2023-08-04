package package1;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class ElevatorSystem extends Thread
{
    private int name;   //电梯编号
    private int currentState; //该电梯当前运动状态 停止：0 上升：1 下降：-1
    private int emerState; //紧急状态
    private int currentFloor; //该电梯当前所在层数
    private int maxUp; //该电梯要去的最高楼层
    private int minDown; //该电梯要去的最低楼层

    private Comparator<Integer> cmpUp = Comparator.naturalOrder();//升序选择器
    private Comparator<Integer> cmpDown = Comparator.reverseOrder();//降序选择器
    private Queue<Integer> upStopList = new PriorityQueue<Integer>(15, cmpUp); //下降停止队列
    private Queue<Integer> downStopList = new PriorityQueue<Integer>(15, cmpDown); //上升停止队列
    private JButton[] buttonList; //按钮队列（ui）
    private JLabel showfloor; //显示楼层的数码显像管

    ElevatorSystem(int name, int dir, JButton[] buttonList,JLabel showfloor)
    {
        this.name = name;
        maxUp = 0;
        minDown = 19;
        currentState = dir;
        currentFloor = 0;
        emerState = -1;
        this.buttonList = buttonList;
        this.showfloor =showfloor;
    }

    public int getCurrentState() //获取该电梯现在的状态
    {
        return currentState;
    }

    public void setCurrentState(int currentState) //修改该电梯现在的状态
    {
        if(currentState == -2) emerState = this.currentState;
        if(currentState == 2)
        {
            currentState = emerState;
            emerState = -1;
        }
        this.currentState = currentState;
    }

    public int getCurrentFloor() //获取该电梯当前所在楼层
    {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor)//修改电梯当前楼层
    {
        this.currentFloor = currentFloor;
    }

    public void popUp()
    {
        upStopList.poll();
    }

    public void addUp(Integer pos)
    {
        upStopList.add(pos);
    }

    public void popDown(Integer pos)
    {
        downStopList.poll();
    }

    public void addDown(Integer pos)
    {
        downStopList.add(pos);
    }

    public int upMax()
    {
        return maxUp;
    }

    public void setMaxUp(int maxUp)
    {
        this.maxUp = maxUp;
    }

    public int downMin()
    {
        return minDown;
    }

    public void setMinDown(int minDown)
    {
        this.minDown = minDown;
    }

    public void run()
    {
    	ImageIcon originIcon = new ImageIcon(ElevatorSystem.class.getResource("/images/elevator.png"));
    	ImageIcon originIcon1 = new ImageIcon(ElevatorSystem.class.getResource("/images/ele_open.png"));
    	int height = buttonList[currentFloor].getHeight();
    	int width = (int) (originIcon.getIconWidth() * ((double) height / originIcon.getIconHeight()));
        ImageIcon icon = new ImageIcon(originIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        ImageIcon ele_open= new ImageIcon(originIcon1.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        
        // 创建一个长度为20的ImageIcon数组，用于存储引入的图片
        ImageIcon[] floor_img = new ImageIcon[20];
        // 循环引入20张图片
        for (int i = 1; i <= 20; i++) {
            String filename = "/images/数字" + i + ".png";  // 图片文件名
            ImageIcon num_image = new ImageIcon(ElevatorSystem.class.getResource(filename)); // 创建ImageIcon对象
            floor_img[i-1] = new ImageIcon(num_image.getImage().getScaledInstance(20, 25, Image.SCALE_SMOOTH));                        // 将ImageIcon对象存入数组中
        }

        while(true)
        {
            // 上升状态
            while (currentState == 1)
            {
                boolean peopleFlag = false;
                // 上下客
                if (!upStopList.isEmpty() && currentFloor  == upStopList.peek())
                {
                    while (currentFloor  == upStopList.peek())
                    {
                        Integer a = upStopList.poll();
                        GUIwindow.logs.append("电梯" + name + "在第" + (currentFloor + 1) + "楼" + "停下\n");
                        GUIwindow.logs.append("电梯" + name + "开门\n");
                        buttonList[1].setBackground(Color.YELLOW);
                        buttonList[1].setText("开门");
                        if(upStopList.isEmpty()) break;
                    }
                    buttonList[currentFloor].setIcon(ele_open);
                    peopleFlag = true;
                }

                // 载上当前上升的人
                while (!GUIwindow.upqueLock[currentFloor]);
                GUIwindow.upqueLock[currentFloor] = false;
                if (!GUIwindow.upqueue[currentFloor].isEmpty()) //该楼层上升请求队列不为空
                {
                    for (int i = 0; i < GUIwindow.upqueue[currentFloor].size(); i++)
                    {
                        if ((int) GUIwindow.upqueue[currentFloor].get(i) - 1 > maxUp) maxUp = (int) GUIwindow.upqueue[currentFloor].get(i) - 1; //更改该电梯要去的最高楼层
                        addUp((Integer) GUIwindow.upqueue[currentFloor].get(i) - 1); //将请求加入该电梯上升停止队列
                        if(currentFloor+1!=(Integer)GUIwindow.upqueue[currentFloor].get(i))
                           GUIwindow.logs.append("电梯" + name + "在第" + (currentFloor + 1) + "楼载上去" + GUIwindow.upqueue[currentFloor].get(i) + "楼的乘客\n");
                        else 
                           GUIwindow.logs.append("电梯"+name+"在第" + (currentFloor + 1) + "楼载上乘客\n");
                    }
                    peopleFlag = true;
                }
                if (!upStopList.isEmpty() && currentFloor == upStopList.peek()) //添加额外的判断条件
                {
                    // 上升队列中已包含当前楼层，电梯就停在当前楼层
                    upStopList.poll();
                    GUIwindow.logs.append("电梯" + name + "在第" + (currentFloor + 1) + "楼" + "停下\n");
                    GUIwindow.logs.append("电梯" + name + "开门\n");
                    buttonList[1].setBackground(Color.YELLOW);
                    buttonList[1].setText("开门");
                    peopleFlag = true;
                }
                GUIwindow.upqueue[currentFloor].clear(); //清空该楼层上升请求队列
                GUIwindow.upqueLock[currentFloor] = true;

                // 电梯走空 载上向下的人
                while (!GUIwindow.downqueLock[currentFloor]);
                GUIwindow.downqueLock[currentFloor] = false;
                if (upStopList.isEmpty() && !GUIwindow.downqueue[currentFloor].isEmpty()) //该楼层上升停止队列为空且下降请求队列不为空
                {
                    for (int i = 0; i < GUIwindow.downqueue[currentFloor].size();i++)
                    {
                        if ((int)GUIwindow.downqueue[currentFloor].get(i) - 1 < minDown) minDown = (int)GUIwindow.downqueue[currentFloor].get(i) - 1; //更改该电梯要去的最低楼层
                        addDown((Integer) GUIwindow.downqueue[currentFloor].get(i) - 1); //将请求加入该电梯下降停止队列
                        if(currentFloor+1!=(Integer)GUIwindow.downqueue[currentFloor].get(i))
                            GUIwindow.logs.append("电梯" + name + "在第" + (currentFloor + 1) + "楼载上去" + GUIwindow.downqueue[currentFloor].get(i) + "楼的乘客\n");
                        else
                        	GUIwindow.logs.append("电梯"+name+"在第" + (currentFloor + 1) + "楼载上乘客\n");
                    }

                    if (!downStopList.isEmpty()) //该电梯下降停止队列不为空 则输出下降信息
                    {
                        GUIwindow.downqueue[currentFloor].clear(); //清空该楼层下降请求队列
                        setCurrentState(-1); //更改该电梯当前状态为下降
                        peopleFlag = true;
                        GUIwindow.downqueLock[currentFloor] = true;

                        try
                        {
                            Thread.sleep(2000);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }

                        buttonList[1].setText("关门");
                        GUIwindow.logs.append("电梯" + name + "关门\n");
                        GUIwindow.logs.append("电梯" + name + "开始下降\n");
                        break;
                    }
                }
                GUIwindow.downqueLock[currentFloor] = true;
                
                //电梯停留
                if (peopleFlag) 
                {
                    try
                    {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    buttonList[currentFloor].setBackground(Color.WHITE);
                    buttonList[1].setText("关门");
                    GUIwindow.logs.append("电梯" + name + "关门\n");
                    buttonList[1].setBackground(Color.GRAY);
                }

                // 电梯人走空或到顶
                if (upStopList.isEmpty() || currentFloor == 19)
                {
                    setCurrentState(0); //修改该电梯状态为停止
                    maxUp = 0;
                    minDown = 19;
                    buttonList[currentFloor].setBackground(Color.WHITE);
                    buttonList[currentFloor].setIcon(icon);
                    GUIwindow.logs.append("电梯" + name + "停止\n");
                    break;
                }

                while(buttonList[1].getText().equals("开门"))
                {
                    try
                    {
                        Thread.sleep(700);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                };
                //设置上升前后电梯以及数码显像管的变化
                if(currentFloor!=0) {
                buttonList[currentFloor].setBackground(Color.gray);    
                }     
                else {
                	buttonList[0].setBackground(Color.GREEN);          	 
                }
                buttonList[currentFloor].setIcon(null);
                currentFloor++; //上面一层楼显示图标
            	showfloor.setIcon(floor_img[currentFloor]);
            	showfloor.setHorizontalAlignment(JLabel.CENTER);
                buttonList[currentFloor].setIcon(icon);
                buttonList[currentFloor].setBackground(Color.WHITE);

                try
                {
                    Thread.sleep(700);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            // 下降状态
            while(currentState == -1)
            {
                boolean peopleFlag = false;

                // 上下客
                if (!downStopList.isEmpty() && currentFloor  == downStopList.peek())
                {
                    System.out.println(downStopList.peek());
                    while (currentFloor  == downStopList.peek())
                    {
                        Integer a = downStopList.poll();
                        GUIwindow.logs.append("电梯" + name + "在第" + (currentFloor + 1) + "楼" + "停下\n");
                        GUIwindow.logs.append("电梯" + name + "开门\n");
                        buttonList[1].setBackground(Color.YELLOW);
                        buttonList[1].setText("开门");
                        if(downStopList.isEmpty()) break;
                    }
                    buttonList[currentFloor].setIcon(ele_open);
                    peopleFlag = true;
                }

                // 载上当前下降的人
                while (!GUIwindow.downqueLock[currentFloor]);
                GUIwindow.downqueLock[currentFloor] = false;
                if (!GUIwindow.downqueue[currentFloor].isEmpty())
                {
                    for (int i = 0; i < GUIwindow.downqueue[currentFloor].size(); i++)
                    {
                        if ((int) GUIwindow.downqueue[currentFloor].get(i) - 1 < minDown) minDown = (int) GUIwindow.downqueue[currentFloor].get(i) - 1;
                        addDown((Integer) GUIwindow.downqueue[currentFloor].get(i) - 1);
                        if(currentFloor+1!=(Integer)GUIwindow.downqueue[currentFloor].get(i))
                            GUIwindow.logs.append("电梯" + name + "在第" + (currentFloor + 1) + "楼载上去" + GUIwindow.downqueue[currentFloor].get(i) + "楼的乘客\n");
                        else
                        	GUIwindow.logs.append("电梯"+name+"在第" + (currentFloor + 1) + "楼载上乘客\n");
                    }
                    if(currentFloor!=0)
                    peopleFlag = true;
                }
                if (!upStopList.isEmpty() && currentFloor == upStopList.peek()) //添加额外的判断条件
                {
                    // 上升队列中已包含当前楼层，电梯就停在当前楼层
                    upStopList.poll();
                    GUIwindow.logs.append("电梯" + name + "在第" + (currentFloor + 1) + "楼" + "停下\n");
                    GUIwindow.logs.append("电梯" + name + "开门\n");
                    buttonList[1].setBackground(Color.YELLOW);
                    buttonList[1].setText("开门");
                    peopleFlag = true;
                }
                GUIwindow.downqueue[currentFloor].clear();
                GUIwindow.downqueLock[currentFloor] = true;

                // 电梯走空 载上向上的人
                while (!GUIwindow.upqueLock[currentFloor]);
                GUIwindow.upqueLock[currentFloor] = false;
                if (downStopList.isEmpty() && !GUIwindow.upqueue[currentFloor].isEmpty())
                {
                    for (int i = 0; i < GUIwindow.upqueue[currentFloor].size();i++)
                    {
                        if ((int)GUIwindow.upqueue[currentFloor].get(i) - 1 > maxUp) maxUp = (int)GUIwindow.upqueue[currentFloor].get(i) - 1;
                        addUp((Integer) GUIwindow.upqueue[currentFloor].get(i) - 1);
                        if(currentFloor+1!=(Integer)GUIwindow.upqueue[currentFloor].get(i))
                            GUIwindow.logs.append("电梯" + name + "在第" + (currentFloor + 1) + "楼载上去" + GUIwindow.upqueue[currentFloor].get(i) + "楼的乘客\n");
                        else
                        	GUIwindow.logs.append("电梯"+name+"在第" + (currentFloor + 1) + "楼载上乘客\n");
                    }

                    if (!upStopList.isEmpty())
                    {
                        GUIwindow.upqueue[currentFloor].clear();
                        setCurrentState(1);
                        peopleFlag = true;
                        GUIwindow.upqueLock[currentFloor] = true;

                        try
                        {
                            Thread.sleep(2000);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }

                        buttonList[1].setText("关门");
                        GUIwindow.logs.append("电梯" + name + "关门\n");
                        buttonList[1].setBackground(Color.GRAY);
                        GUIwindow.logs.append("电梯" + name + "开始上升\n");
                        break;
                    }
                }
                GUIwindow.upqueLock[currentFloor] = true;
                
                //电梯停留
                if (peopleFlag) 
                {
                    try
                    {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    buttonList[currentFloor].setBackground(Color.WHITE);
                    buttonList[1].setText("关门");
                    GUIwindow.logs.append("电梯" + name + "关门\n");
                    buttonList[1].setBackground(Color.GRAY);
                }

                // 电梯走空 到底
                if (downStopList.isEmpty() || currentFloor == 0)
                {   if(currentFloor==0)
                       buttonList[currentFloor].setBackground(Color.GREEN);             
                    else {
                    buttonList[currentFloor].setBackground(Color.WHITE);
                    }
                    buttonList[currentFloor].setIcon(icon);
                    setCurrentState(0);
                    maxUp = 0;
                    minDown = 19;
                    GUIwindow.logs.append("电梯" + name + "停止\n");
                    break;
                }

                while(buttonList[1].getText().equals("开门"))
                {
                    try
                    {
                        Thread.sleep(700);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                };
                //设置下降前后电梯以及数码显像管的变化
                buttonList[currentFloor].setBackground(Color.gray);
                buttonList[currentFloor].setIcon(null);
                currentFloor--;
                showfloor.setIcon(floor_img[currentFloor]);
            	showfloor.setHorizontalAlignment(JLabel.CENTER);
                buttonList[currentFloor].setIcon(icon);
                if(currentFloor!=0) {
                buttonList[currentFloor].setBackground(Color.WHITE);
                }
                buttonList[currentFloor].setIcon(icon);
                try
                {
                    Thread.sleep(700);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

            }

            // 停止
            while(currentState == 0)
            {
                for (int i = 2; i < 20; i++) buttonList[i].setText("");
                try
                {
                    Thread.sleep(1000);
                    
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            // 防止线程阻塞
            try
            {
                Thread.sleep(20);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}