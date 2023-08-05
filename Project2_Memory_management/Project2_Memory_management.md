# 操作系统内存管理项目文档

## 一、项目简介

本项目旨在实现一个请求调页存储管理方式的模拟系统。通过模拟调页过程加深对FIFO（先进先出算法）以及LRU（最近最少使用页面淘汰算法）。

按照每个页面可存放10条指令的规则，给一个作业分配4个内存块，模拟了一个作业有320条指令的执行过程。开始时，所有的页面都未调入内存。随着作业的指令逐步执行，按需将页面调入内存中。

在模拟过程中，如果所访问指令在内存中，则显示其物理地址，并转到下一条指令；如果没有在内存中，则发生缺页，此时需要记录缺页次数，并将其调入内存。如果4个内存块中已装入作业，则需进行页面置换。所有320条指令执行完成后，计算并显示作业执行过程中发生的缺页率。

置换算法选用FIFO或者LRU算法。作业中指令访问次序可以按照下面原则形成：50%的指令是顺序执行的，25%是均匀分布在前地址部分，25％是均匀分布在后地址部分。

## 二、技术栈

* 编程语言：Java

* 开发工具：IntelliJ IDEA Community Edition 2023.1.2

* GUI库：javax.swing.

* AWT库：java.awt.

## 三、界面设计

![1](/imgs/1.png)

本操作系统内存管理项目的UI界面如图，分为左右侧两个部分：控制及信息显示区、内存页面模拟区。

控制及信息显示区包括：

* 顶部页面置换算法选择栏，可以通过点击对应的算法进行选择。

* 开始执行和清零重置按钮，分别用于执行请求调页的模拟和将其清零重置。

* 模拟过程标签处展示了当前指令执行的页调度信息。

* 左下方标签会显示当前等待执行的指令队列中的前四个。

* 右下方显示调度过程中的缺页数和缺页率。

* 最下方为执行速度控制的输入框和确认按钮，输入对应数字并点击确认即可设置对应速度，单位为ms（即执行一条指令需要时间）。

内存页面区包括：

* 四个内存块，每个内存块，每个页面可存放10条指令。

* 最下方表示当前被调入该内存块的页面。

## 四、算法设计

该项目主要通过两个页面置换算法实现，即FIFO（先进先出算法）和LRU（最近最少使用页面淘汰算法）。

1. FIFO（先进先出算法）的主要原理是根据页面进入内存的顺序进行置换，先进入内存的页面将先被替换出去。具体工作方式为：

* 当一个页面需要调入内存时，操作系统选择最早进入内存的页面进行替换。

* 每个页面在进入内存时都会有一个时间戳记录其进入时间。

* 当内存空间不足时，操作系统会查找最早进入内存的页面，并将其替换出，然后将新页面调入内存。

代码实现如下：

```java
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
            double missRate = (double) cnt_miss / (double) (i + 1) *   
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
```

2. LRU（最近最少使用页面淘汰算法）是基于页面使用频率的置换算法，它认为最近最少使用的页面很可能在未来也不会被使用，因此选择该页面进行置换。具体工作方式为：

* 当一个页面需要调入内存时，操作系统会根据页面的使用情况来选择被置换的页面。

* 每个页面有一个计数器来记录其空闲的次数。

* 当内存空间不足时，操作系统会查找最久未被访问的页面，并将其替换出去，然后将新页面调入内存。

代码实现如下：

```java
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
```

## 五、类设计

本项目中共涉及八个类，分别是AlgoChoose类、Block类、Memory类、Moniter类、Page类、SpeedChoose类、WaitingList类以及GUIWindow类。

1. AlgoChoose类：继承自JPanel，这个类的主要功能是创建一个用于页面置换算法选择的UI界面。它包括一个选项列表，其中包含了FIFO（先进先出算法）和LRU（最近最少使用页面淘汰算法）两种选择。用户可以通过界面中的选择列表来选择他们希望使用的页面置换算法。该类还负责设置面板的布局、位置和背景色，并添加标题和选项列表，并进行相应的样式设置。
2. Block类：继承自JLabel，用于显示和表示内存中块的状态。
3. Memory类：继承自JPanel，主要功能包括设置内存图示的大小和背景色。

创建内存页面的图示元素Page，并将其添加到面板中。提供dispatchPage方法，用于将逻辑页分派给物理页，更新页面图示的状态。提供check方法，用于检查指定逻辑页是否在内存中，并返回对应的物理页索引。提供clear方法，用于清空内存中的所有页面。

4. Moniter类：继承自JPanel，通过Moniter类，可以创建一个用于展示模拟过程信息的显示器界面，根据传入的参数显示相应的模拟过程信息，包括指令是否在内存中、调度情况等。同时，该类还提供了清空显示器内容的方法。
5. Page类：继承自JPanel，通过Page类，可以创建表示内存中页面的UI元素，用于显示和更新页面的状态。每个页面包含10个内存块，根据传入的逻辑页号，更新块元素的文本内容和逻辑页标签的显示。同时，该类还提供了清空页面内容和获取逻辑页号的功能。
6. SpeedChoose类：继承自JPanel，用于设置选择指令执行速度的输入框和确认按钮。
7. WaitingList类：继承自JPanel，通过WaitingList类，可以创建用于展示等待执行的指令队列的UI元素。每个列表元素显示一个指令编号，其中最后一位是最新的待执行指令。通过turn方法，可以更新队列的状态，将指令队列顺时针移动一位，并插入新的指令到队列的最后一位。同时，该类还提供了清空等待列表的功能。

8. GUIWindow类：用于设置系统的UI界面以及实现FIFO算法和LRU算法、指令的执行、页面调度过程等。

## 六、运行截图

![2](/imgs/2.png)

![3](/imgs/3.png)

![4](/imgs/4.png)

## 七、项目总结

该操作系统模拟项目实现了请求调页存储管理方式。它通过模拟内存的分页和页面置换算法，使得程序可以动态地请求和释放内存页面。该项目为用户提供了一个可视化界面，用于测试和评估不同的调页算法，以改善内存管理和系统性能。通过设计这个项目，我更加深入地理解了请求调页存储管理的原理和实际应用，对操作系统的内存管理有了更加深入的思考。