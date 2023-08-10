package Java;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class WYXFileSystem extends JFrame {
    private JTree tree;
    private JScrollPane treePane;
    private JScrollPane tablePane;
    private documentTable model = new documentTable();
    private JTable fileTable;

    private File rootFile;
    private File readMe;


    private Folder folder1;
    private Folder folder2;
    private Folder folder3;
    private Folder folder4;
    private Folder folder5;
    private ArrayList<Folder> folders = new ArrayList<Folder>();

    // 底部信息栏
    private JLabel haveUsed = new JLabel("已使用空间:");
    private JLabel usedField = new JLabel();
    private JLabel freeYet = new JLabel("剩余空间:");
    private JLabel freeField = new JLabel();

    private JTextField searchLine = new JTextField();

    // 删除文件夹
    public static void deleteDirectory(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File myfile : files) {
                deleteDirectory(filePath + File.separator + myfile.getName());
            }
            file.delete();
        }
    }

    // 获取空闲空间
    public double getSpace(File file) {
        double space = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.readLine();
            space = Double.parseDouble(reader.readLine());
            if (space > 16384) {
                space = 0.0;
            }
            reader.close();
        } catch (Exception e) {
        }
        ;
        return space;
    }

    // 更新文件夹信息
    public void upDateBlock(Folder currentBlock) {
//        fileNumField.setText(String.valueOf(currentBlock.getFileNum()));
        usedField.setText(String.valueOf(currentBlock.getSpace()) + " KB");
        freeField.setText(String.valueOf(16384 - currentBlock.getSpace()) + "KB");
    }

    // 查找文件
    public boolean searchFile(String fileName, File parent) {
        File[] files = parent.listFiles();
        for (File myFile : files) {
            if (myFile.getName().equals(fileName)) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(myFile);
                        return true;
                    }
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, myFile.getPath() + " 抱歉，出现了一些错误！", "Fail to open",
                            JOptionPane.ERROR_MESSAGE);
                    return true;
                }
            }
            if (myFile.isDirectory() && myFile.canRead()) {
                if (searchFile(fileName, myFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 用户交互界面
    public WYXFileSystem() throws IOException {
        setTitle("WYXFileSystem");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon(this.getClass().getResource("/image/filesystem.png")).getImage());
        getContentPane().setBackground(Color.WHITE);

        ImageIcon folderImg = new ImageIcon(this.getClass().getResource("/image/folder.png"));
        folderImg.setImage(folderImg.getImage().getScaledInstance(30, 23, Image.SCALE_DEFAULT));

        ImageIcon docImg = new ImageIcon(this.getClass().getResource("/image/document.png"));
        docImg.setImage(docImg.getImage().getScaledInstance(24, 30, Image.SCALE_DEFAULT));

        Icon icon1 = folderImg;
        Icon icon2 = docImg;
        UIManager.put("Tree.openIcon", icon1);
        UIManager.put("Tree.closedIcon", icon1);
        UIManager.put("Tree.leafIcon", icon2);

        // 创建工作区——如果已有直接使用
        rootFile = new File("WYXFileSystem");

        boolean flag = true;

        // 文件树初始化
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(new WYXFile(rootFile, 0, 16384));
        if (!rootFile.exists()) {
            flag = false;
            try {
                rootFile.mkdir();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "空闲空间不足!", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        folder1 = new Folder(1, new File(rootFile.getPath() + File.separator + "Root"), flag);
        folders.add(folder1);

        root.add(new DefaultMutableTreeNode(new WYXFile(folder1.getBlockFile(), 1, 16384.0)));
        model.addRow(new WYXFile(folder1.getBlockFile(), 1, 16384.0));
        ((DefaultMutableTreeNode) root.getChildAt(0)).add(new DefaultMutableTreeNode("temp"));

        // 文件表初始化
        fileTable = new JTable(model);
        fileTable.getTableHeader().setFont(new Font(Font.DIALOG, Font.CENTER_BASELINE, 24));
        fileTable.setSelectionBackground(Color.ORANGE);
        fileTable.setShowHorizontalLines(true);
        fileTable.setShowVerticalLines(false);
        fileTable.getTableHeader().setFont(new Font("黑体", Font.CENTER_BASELINE, 16));
        fileTable.getTableHeader().setForeground(new Color(79, 155, 250));
        fileTable.getTableHeader().setBackground(new Color(220, 220, 220)); // 浅灰色背景
        fileTable.setRowHeight(30);
        fileTable.setBackground(Color.WHITE); // 白色背景
        fileTable.setForeground(Color.GRAY); // 灰色前景
        fileTable.setSelectionBackground(new Color(201,253,225)); // 亮绿色选中背景
        fileTable.setSelectionForeground(Color.DARK_GRAY); // 深灰色选中前景
        fileTable.setFont(new Font(Font.DIALOG, Font.CENTER_BASELINE, 14));
        fileTable.updateUI();


        final DefaultTreeModel treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setEditable(false);
        tree.putClientProperty("Jtree.lineStyle", "Horizontal");
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false); // 设置根节点不显示

        //默认生成两个文件夹
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new WYXFile(folder1.getBlockFile(), 1, 1024.0));
        WYXFile temp = (WYXFile) node.getUserObject();
        int blockName = 1;
        Folder currentBlock = folders.get(blockName - 1);
        String[] folderNames = { "WYX1", "WYX2" }; // 新建文件夹的名称
        for (String folderName : folderNames) {
            String newDirPath = temp.getFilePath() + File.separator + folderName;
            File newDir = new File(newDirPath);
            if (newDir.exists()) {
                continue; // 文件夹已存在，跳过本次循环
            }
            newDir.mkdir();
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new WYXFile(newDir, blockName, 0));
            newNode.add(new DefaultMutableTreeNode("temp"));
            model.addRow(new WYXFile(newDir, blockName, 0));
        }
        fileTable.updateUI();
        upDateBlock(currentBlock);



        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

                DefaultMutableTreeNode parent = null;
                TreePath parentPath = e.getPath();
                if (parentPath == null) {
                    parent = root;
                } else {
                    parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }
                int blockName = ((WYXFile) parent.getUserObject()).getBlockName();

                    Folder currentBlock = folders.get(blockName - 1);
                    upDateBlock(currentBlock);

                    model.removeRows(0, model.getRowCount());
                    File rootFile = new File(((WYXFile) parent.getUserObject()).getFilePath());
                    if (parent.getChildCount() > 0) {
                        File[] childFiles = rootFile.listFiles();

                        for (File file : childFiles) {
                            model.addRow(new WYXFile(file, blockName, getSpace(file)));
                        }
                    } else {
                        model.addRow(new WYXFile(rootFile, blockName, getSpace(rootFile)));
                    }
                    fileTable.updateUI();

                }

        });
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            //文件树展开
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode parent = null;
                TreePath parentPath = event.getPath();
                if (parentPath == null) {
                    parent = root;
                } else {
                    parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }

                int blockName = ((WYXFile) parent.getUserObject()).getBlockName();

                File rootFile = new File(((WYXFile) parent.getUserObject()).getFilePath());
                File[] childFiles = rootFile.listFiles();

                model.removeRows(0, model.getRowCount());
                for (File myFile : childFiles) {
                    DefaultMutableTreeNode node = null;
                    node = new DefaultMutableTreeNode(new WYXFile(myFile, blockName, getSpace(myFile)));
                    if (myFile.isDirectory() && myFile.canRead()) {
                        node.add(new DefaultMutableTreeNode("temp"));
                    }
                    treeModel.insertNodeInto(node, parent, parent.getChildCount());
                    model.addRow(new WYXFile(myFile, blockName, getSpace(myFile)));
                }
                if (parent.getChildAt(0).toString().equals("temp") && parent.getChildCount() != 1)
                    treeModel.removeNodeFromParent((MutableTreeNode) parent.getChildAt(0));
                fileTable.updateUI();
            }

            //文件树收起
            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode parent = null;
                TreePath parentPath = event.getPath();
                if (parentPath == null) {
                    parent = root;
                } else {
                    parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }
                if (parent.getChildCount() > 0) {
                    int count = parent.getChildCount();
                    for (int i = count - 1; i >= 0; i--) {
                        treeModel.removeNodeFromParent((MutableTreeNode) parent.getChildAt(i));
                    }
                    treeModel.insertNodeInto(new DefaultMutableTreeNode("temp"), parent, parent.getChildCount());
                }
                model.removeRows(0, model.getRowCount());
                fileTable.updateUI();
            }
        });
        treePane = new JScrollPane(tree);
        treePane.setPreferredSize(new Dimension(150, 400));
        add(treePane, BorderLayout.WEST);

        tablePane = new JScrollPane(fileTable);
        add(tablePane, BorderLayout.CENTER);

        // 双击打开文件
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    String fileName = ((String) model.getValueAt(fileTable.getSelectedRow(), 0));
                    String filePath = ((String) model.getValueAt(fileTable.getSelectedRow(), 1));
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(new File(filePath));
                        }
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "抱歉，出了一些错误！", "打开失败", JOptionPane.ERROR_MESSAGE);
                    }
                    JOptionPane.showMessageDialog(null, "文件名: " + fileName + "\n 文件路径: " + filePath,
                            "content", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // 菜单初始化
        final JPopupMenu myMenu = new JPopupMenu();
        myMenu.setPreferredSize(new Dimension(300, 200));

        // 新建文件
        JMenuItem createFileItem = new JMenuItem("新建文件");
        createFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                WYXFile temp = (WYXFile) node.getUserObject();
                int blokName = temp.getBlockName();
                Folder currentBlock = folders.get(blokName - 1);

                String inputValue;
                double capacity;

                JOptionPane inputPane = new JOptionPane();
                inputPane.setPreferredSize(new Dimension(600, 600));
                inputPane.setInputValue(JOptionPane.showInputDialog("文件名："));
                if (inputPane.getInputValue() == null) {
                    return;
                }
                inputValue = inputPane.getInputValue().toString();
                inputPane.setInputValue(JOptionPane.showInputDialog("文件大小(KB):"));
                if (inputPane.getInputValue() == null) {
                    return;
                }
                capacity = Double.parseDouble(inputPane.getInputValue().toString());

                File newFile = new File(temp.getFilePath() + File.separator + inputValue + ".txt");
                if (!newFile.exists() && !inputValue.equals(null)) {
                    try {
                        if (currentBlock.createFile(newFile, capacity)) {
                            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
                                    new WYXFile(newFile, blokName, capacity));
                            model.removeRows(0, model.getRowCount());
                            model.addRow(new WYXFile(newFile, blokName, capacity));
                            fileTable.updateUI();
                            upDateBlock(currentBlock);
                            JOptionPane.showMessageDialog(null, "创建成功！请刷新文件夹！", "成功", JOptionPane.DEFAULT_OPTION);
                        }
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "创建失败!!!", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        myMenu.add(createFileItem);

        // 新建文件夹
        JMenuItem createDirItem = new JMenuItem("新建文件夹");
        createDirItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                WYXFile temp = (WYXFile) node.getUserObject();
                int blockName = temp.getBlockName();
                Folder currentBlock = folders.get(blockName - 1);
                String inputValue = JOptionPane.showInputDialog("文件夹名称:");
                if (inputValue == null) {
                    return;
                }
                File newDir = new File(temp.getFilePath() + File.separator + inputValue);
                if (newDir.exists())
                    deleteDirectory(newDir.getPath());
                try {
                    newDir.mkdir();
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new WYXFile(newDir, blockName, 0));
                    newNode.add(new DefaultMutableTreeNode("temp"));
                    model.removeRows(0, model.getRowCount());
                    model.addRow(new WYXFile(newDir, blockName, 0));
                    fileTable.updateUI();
                    upDateBlock(currentBlock);
                    JOptionPane.showMessageDialog(null, "创建成功，请刷新文件夹！", "成功", JOptionPane.DEFAULT_OPTION);
                } catch (Exception E) {
                    JOptionPane.showMessageDialog(null, "创建失败!!!", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        myMenu.add(createDirItem);

        // 删除文件/文件夹
        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                WYXFile temp = (WYXFile) node.getUserObject();
                int blockName = temp.getBlockName();
                Folder currentBlock = folders.get(blockName - 1);
                int choose = JOptionPane.showConfirmDialog(null, "确定删除？", "确认", JOptionPane.YES_NO_OPTION);
                if (choose == 0) {
                    if (currentBlock.deleteFile(temp.getMyFile(), temp.getSpace())) {
                        try {
                            currentBlock.rewriteBitMap();
                            currentBlock.rewriteRecoverWriter();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        upDateBlock(currentBlock);
                        JOptionPane.showMessageDialog(null, "删除成功，请刷新文件夹！", "成功", JOptionPane.DEFAULT_OPTION);
                    } else {
                        JOptionPane.showMessageDialog(null, "删除失败!!!", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        myMenu.add(deleteItem);

        // 格式化
        JMenuItem formatItem = new JMenuItem("格式化");
        formatItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                WYXFile temp = (WYXFile) node.getUserObject();
                int blockName = temp.getBlockName();
                Folder currentBlock = folders.get(blockName - 1);
                int choose = JOptionPane.showConfirmDialog(null, "确定格式化文件夹吗？", "确认", JOptionPane.YES_NO_OPTION);
                if(temp.getFileName()!="1") {
                    if (choose == 0) {
                        try {
                            if (temp.getMyFile().isDirectory()) {
                                for (File myfile : temp.getMyFile().listFiles()) {
                                    currentBlock.deleteFile(myfile, getSpace(myfile));
                                }
                                upDateBlock(currentBlock);
                                JOptionPane.showMessageDialog(null, "格式化成功，请刷新文件夹！", "成功", JOptionPane.DEFAULT_OPTION);
                                currentBlock.rewriteBitMap();
                            }
                        } catch (Exception E1) {
                            JOptionPane.showMessageDialog(null, "格式化失败!!!", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                else{
                    if(choose==0)
                    JOptionPane.showMessageDialog(null, "未取得权限，格式化失败!!!", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        myMenu.add(formatItem);

        // 重命名
        JMenuItem renameItem = new JMenuItem("重命名");
        renameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                WYXFile temp = (WYXFile) node.getUserObject();
                int blockName = temp.getBlockName();
                Folder currentBlock = folders.get(blockName - 1);
                if(temp.getFileName()!="1") {
                    String inputValue = null;
                    JOptionPane inputPane = new JOptionPane();
                    inputPane.setInputValue(JOptionPane.showInputDialog("新的文件名:"));
                    if (inputPane.getInputValue() == null) {
                        return;
                    }
                    inputValue = inputPane.getInputValue().toString();
                    try {
                        currentBlock.renameFile(temp.getMyFile(), inputValue, temp.getSpace());
                        JOptionPane.showMessageDialog(null, "重命名成功，请刷新文件夹", "成功", JOptionPane.DEFAULT_OPTION);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "重命名失败!!!", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null, "未取得权限，重命名失败!!!", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        myMenu.add(renameItem);

        // 文件夹信息
        JPanel panel = new JPanel();
        panel.setBackground(new Color(79, 155, 250));
        panel.setForeground(Color.WHITE);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(new JLabel("  "));
        panel.add(haveUsed);
        usedField.setForeground(Color.WHITE);
        panel.add(usedField);
        panel.add(new JLabel("  "));
        panel.add(freeYet);
        freeField.setForeground(Color.WHITE);
        panel.add(freeField);
        panel.add(new JLabel("  "));
        add(panel, BorderLayout.SOUTH);

        // 搜索条初始化
        JPanel searchPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JLabel searchLabel = new JLabel("搜索: ");
        searchPane.add(searchLabel);

        final JTextField searchLine = new JTextField("输入格式:文件：XXX.txt 文件夹:XXX");
        searchLine.setPreferredSize(new Dimension(500, 30));
        searchLine.setFont(new Font("楷体", Font.BOLD, 16));
        searchLine.setForeground(Color.gray);

        // 添加焦点监听器
        searchLine.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // 当搜索条获得焦点时，如果文本是默认提示文本，则清空文本
                if (searchLine.getText().equals("输入格式:文件：XXX.txt 文件夹:XXX")) {
                    searchLine.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 当搜索条失去焦点时，如果文本为空，则还原默认提示文本
                if (searchLine.getText().isEmpty()) {
                    searchLine.setText("输入格式:文件：XXX.txt 文件夹:XXX");
                }
            }
        });

        searchPane.add(searchLine);

        JButton searchButton = new JButton("确认");
        searchButton.setBackground(new Color(79, 155, 250));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("宋体", Font.BOLD, 16));
        searchButton.setFont(new Font("宋体", Font.BOLD, 16));

        // 添加搜索按钮的事件处理逻辑
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = searchLine.getText();
                if (!searchFile(fileName, rootFile)) {
                    JOptionPane.showMessageDialog(null, "找不到此文件!", "失败!", JOptionPane.WARNING_MESSAGE);
                }
                searchLine.setText("");
            }
        });
        searchPane.add(searchButton);
        add(searchPane, BorderLayout.NORTH);

        // 给文件树加监听
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON3) {
                    myMenu.show(e.getComponent(), e.getX(), e.getY());

                }
            }
        });

        this.setBounds(150, 50, 900, 600);
        setVisible(true);
    }

    public static void main(String args[]) throws IOException {
        new WYXFileSystem();
    }
}
