
import java.awt.Container;
import static java.awt.Frame.DEFAULT_CURSOR;
import static java.awt.Frame.WAIT_CURSOR;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hdm
 */
public class dxfConvJFrame extends javax.swing.JFrame {
DefaultListModel ModelFiles;
DefaultListModel ModelSrc;
DefaultListModel ModelDest;
DefaultListModel ModelConv;
JViewport viewSrc;
JViewport viewDest;
JViewport viewConv;

    /**
     * Creates new form dxfConvJFrame
     */
    public dxfConvJFrame() {
        initComponents();
        jComboBoxReadEnc.removeAllItems();
        jComboBoxReadEnc.addItem("UTF-8");
        jComboBoxReadEnc.addItem("Shift-JIS");
        
        jComboBoxWriteEnc.removeAllItems();
        jComboBoxWriteEnc.addItem("UTF-8");
        jComboBoxWriteEnc.addItem("Shift-JIS");
        
        jComboBoxConvType.removeAllItems();
        jComboBoxConvType.addItem("可読文字列");
        jComboBoxConvType.addItem("Unicodeエスケープ");
        jComboBoxConvType.setSelectedIndex(1);
        
        /* ラジオボタングループ化 */
        ButtonGroup group0 = new ButtonGroup();
        group0.add(jRadioButtonMenuItemTarget1);
        group0.add(jRadioButtonMenuItemTarget2);
        group0.add(jRadioButtonMenuItemTarget3);
        group0.add(jRadioButtonMenuItemTarget4);
        
        jRadioButtonMenuItemTarget2.setSelected(true);
        jTextFieldFileName.setText("");
        jTextFieldFileNameDest.setText("");
        
        jList1.setSelectedIndex(0);
        initList_ListFiles();
        DropTarget target = new DropTarget(jListFiles, new MyDropTargetAdapter());
        
        initListSrc();
        initListDest();
        initListConv();
        editFileNameDest();
        jTabbedPane1.setSelectedIndex(1);
        
        viewSrc = jScrollPaneSrc.getViewport();
        viewDest = jScrollPaneDest.getViewport();
        viewConv = jScrollPaneConv.getViewport();
        initView();
    }
    
    /**
     * ドラッグ＆ドロップイベントを受け取る。
     * @author s.s.k.
     */
    private class MyDropTargetAdapter extends DropTargetAdapter {
        /*
         * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
         */
        public void drop(DropTargetDropEvent e) {
            System.out.println("Drop!");
            try {
                Transferable transfer = e.getTransferable();
                if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    //For Windows
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    java.util.List fileList = (java.util.List) (transfer.getTransferData(DataFlavor.javaFileListFlavor));
                    setFileNames(fileList);
                } else if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    //For Linux (KDE Dolphin)
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    String str = (String)(transfer.getTransferData(DataFlavor.stringFlavor));
                    String fileListStr[] = str.split("\n");
                    java.util.ArrayList fileList = new java.util.ArrayList();
                    for (int i = 0; i < fileListStr.length; i++) {
                        String path = fileListStr[i].replaceAll("file://", "");
                        System.out.println("ドロップされたファイル名" + path);
                        File file = new File(path);
                        fileList.add(file);
                    }
                    setFileNames(fileList);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    public void setFileNames(java.util.List fileList) {
        for (int i = 0; i < fileList.size(); i++) {
            File file = (File) fileList.get(i);
            //buffer.append(file.getAbsolutePath());
            String str = file.getAbsolutePath();
            //image でなければ処理しない　.bmp .gif .jpg .jpeg のみ
            String ext = "";
            int Idx = str.lastIndexOf(".");
            boolean flg = true;
            if (Idx > 0) {
                ext = str.substring(Idx).toLowerCase();
                if (ext.equals(".dxf")) {
                    //OK
                } else {
                    //NG
                    flg = false;
                    JOptionPane.showMessageDialog(this, ext + "形式には対応していません。\n（DXF変換）");
                }
            } else {
                //NG
                flg = false;
            }
            //ディレクトリならNG
            if (file.isDirectory()) {
                flg = false;
            }
            if (flg) {
                //絶対パスを取得
                String filename = file.getAbsolutePath();
                ModelFiles.addElement(filename);
            }
        }
    }
    
    //スクロールの同期
    public void initView() {
        jScrollPaneDest.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                //もう片方のスクロール
                Point point = viewDest.getViewPosition();
                try {
                    viewSrc.setViewPosition(point);
                    viewConv.setViewPosition(point);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void editFileNameDest() {
        String str = getFileName();
        str = str.replaceAll(".dxf", "");
        str = str.replaceAll(".DXF", "");
        str = str.replaceAll("_ForQCAD", "");
        str = str.replaceAll("_ForJwCad", "");
        str = str.replaceAll("_ForOldAutoCAD", "");
        str = str.replaceAll("_ForAutoCAD", "");
        int mode = getTarget();
        switch (mode) {
            case 0 : str = str + "_ForQCAD.dxf"; break;
            case 1 : str = str + "_ForJwCad.dxf"; break;
            case 2 : str = str + "_ForOldAutoCAD.dxf"; break;
            case 3 : str = str + "_ForAutoCAD.dxf"; break;
        }
        jTextFieldFileNameDest.setText(str);
    }
    public String getFileNameDest() {
        return jTextFieldFileNameDest.getText();
    }
    public void setFileName(String str) {
        jTextFieldFileName.setText(str);
    }
    public String getFileName() {
        return jTextFieldFileName.getText();
    }
    
    public int getTarget() {
        int idx = 0;
        if (jRadioButtonMenuItemTarget1.isSelected()) {
            idx = 0;
        }else if (jRadioButtonMenuItemTarget2.isSelected()) {
            idx = 1;
        }else if (jRadioButtonMenuItemTarget3.isSelected()) {
            idx = 2;
        }else if (jRadioButtonMenuItemTarget4.isSelected()) {
            idx = 3;
        }
        return idx;
    }
    
    public final void initList_ListFiles(){
        //リストボックス初期化
        ModelFiles = new DefaultListModel();
        jListFiles.setModel(ModelFiles);
    }
    public final void initListSrc(){
        //リストボックス初期化
        ModelSrc = new DefaultListModel();
        jListSrc.setModel(ModelSrc);
    }
    public final void initListDest(){
        //リストボックス初期化
        ModelDest = new DefaultListModel();
        jListDest.setModel(ModelDest);
    }
    public final void initListConv(){
        //リストボックス初期化
        ModelConv = new DefaultListModel();
        jListConv.setModel(ModelConv);
    }
    public final void setModelDest(){
        //リストボックス初期化
        jListDest.setModel(ModelDest);
    }
    public final void setModelConv(){
        //リストボックス初期化
        jListConv.setModel(ModelConv);
    }
    public int getSrcEnc(){
        return jComboBoxReadEnc.getSelectedIndex();
    }
    public int getDestEnc(){
        return jComboBoxWriteEnc.getSelectedIndex();
    }
    public int getConvType(){
        return jComboBoxConvType.getSelectedIndex();
    }
    public void setListSrc(String str) {
        ModelSrc.addElement(str);
    }
    public void setListDest(String str) {
        ModelDest.addElement(str);
    }
    public void setListConv(String str) {
        ModelConv.addElement(str);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jComboBoxReadEnc = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPaneSrc = new javax.swing.JScrollPane();
        jListSrc = new javax.swing.JList();
        jScrollPaneDest = new javax.swing.JScrollPane();
        jListDest = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jComboBoxConvType = new javax.swing.JComboBox();
        jButton2 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxWriteEnc = new javax.swing.JComboBox();
        jButton3 = new javax.swing.JButton();
        jTextFieldFileName = new javax.swing.JTextField();
        jTextFieldFileNameDest = new javax.swing.JTextField();
        jScrollPaneConv = new javax.swing.JScrollPane();
        jListConv = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabelMode = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabelMode1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListFiles = new javax.swing.JList();
        jLabelMode2 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();
        jRadioButtonMenuItemTarget1 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemTarget2 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemTarget3 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemTarget4 = new javax.swing.JRadioButtonMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DxfConverter");
        setBounds(new java.awt.Rectangle(300, 100, 0, 0));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jComboBoxReadEnc.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("読み込み文字コード");

        jButton1.setText("読み込み");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jListSrc.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPaneSrc.setViewportView(jListSrc);

        jListDest.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListDest.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListDestValueChanged(evt);
            }
        });
        jScrollPaneDest.setViewportView(jListDest);

        jLabel2.setText("→");

        jLabel3.setText("以下のように変換します");

        jLabel4.setText("変換方法");

        jComboBoxConvType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton2.setText("変換");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel5.setText("書き込み文字コード");

        jComboBoxWriteEnc.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton3.setText("書き込み");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jTextFieldFileName.setEditable(false);
        jTextFieldFileName.setText("jTextField1");

        jTextFieldFileNameDest.setText("jTextField1");

        jListConv.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPaneConv.setViewportView(jListConv);

        jLabel6.setText("→");
        jLabel6.setFocusable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jScrollPaneSrc, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPaneDest, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBoxReadEnc, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton1)))
                                .addGap(13, 13, 13)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPaneConv, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jComboBoxWriteEnc, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton3))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jComboBoxConvType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton2)))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTextFieldFileName))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTextFieldFileNameDest)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxReadEnc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPaneDest, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPaneSrc, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPaneConv, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBoxConvType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jButton2))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBoxWriteEnc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(jButton3))
                        .addGap(18, 18, 18)
                        .addComponent(jTextFieldFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldFileNameDest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("詳細", jPanel1);

        jLabelMode.setText("１．モードを選択してください");

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "QCAD→Jw_Cad", "Jw_Cad→QCAD" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jLabelMode1.setText("２．以下のボックスにエクスプローラからファイルをドロップしてください。");

        jListFiles.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jListFiles);

        jLabelMode2.setText("３．変換ボタンを押すと、DXFファイルと同じフォルダに変換後のファイルが作成されます。");

        jButton4.setText("変換");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelMode, javax.swing.GroupLayout.PREFERRED_SIZE, 590, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelMode1, javax.swing.GroupLayout.PREFERRED_SIZE, 590, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelMode2, javax.swing.GroupLayout.PREFERRED_SIZE, 590, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4))
                        .addGap(0, 11, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelMode, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelMode1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelMode2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addContainerGap())
        );

        jTabbedPane1.addTab("簡単", jPanel2);

        jMenu2.setText("おすすめ設定");

        jRadioButtonMenuItemTarget1.setSelected(true);
        jRadioButtonMenuItemTarget1.setText("QCAD用");
        jRadioButtonMenuItemTarget1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemTarget1ActionPerformed(evt);
            }
        });
        jMenu2.add(jRadioButtonMenuItemTarget1);

        jRadioButtonMenuItemTarget2.setSelected(true);
        jRadioButtonMenuItemTarget2.setText("Jw_Cad用");
        jRadioButtonMenuItemTarget2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemTarget2ActionPerformed(evt);
            }
        });
        jMenu2.add(jRadioButtonMenuItemTarget2);

        jRadioButtonMenuItemTarget3.setSelected(true);
        jRadioButtonMenuItemTarget3.setText("AutoCAD用(2006以前)");
        jRadioButtonMenuItemTarget3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemTarget3ActionPerformed(evt);
            }
        });
        jMenu2.add(jRadioButtonMenuItemTarget3);

        jRadioButtonMenuItemTarget4.setSelected(true);
        jRadioButtonMenuItemTarget4.setText("AutoCAD用(2007以降)");
        jRadioButtonMenuItemTarget4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemTarget4ActionPerformed(evt);
            }
        });
        jMenu2.add(jRadioButtonMenuItemTarget4);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("編集");

        jMenuItem2.setText("全角カナ→半角ｶﾅ");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        jMenuBar1.add(jMenu3);

        jMenu1.setText("Help");

        jMenuItem1.setText("Version情報");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if (dxfconv.sbConv == null) {
            JOptionPane.showMessageDialog(this, "変換後に実行してください。\n", "エラー", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        setCursor(WAIT_CURSOR);
        dxfconv.saveDxf();
        setCursor(DEFAULT_CURSOR);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //カレント
        String path = new File(".").getAbsolutePath();
        if (!dxfconv.DirPath.equals("")) {
            File dir = new File(dxfconv.DirPath);
            if (dir.exists()) {
                //prop
                path = dxfconv.DirPath;
            }
        }
        //前回設定したファイル(あれば)
        if (dxfconv.fileSrc != null) {
            if (dxfconv.fileSrc.exists()) {
                path = dxfconv.fileSrc.getAbsolutePath();
            }
        }
        //ファイル選択ダイアログでファイル名・フォルダ名の変更モードを抑止
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        JFileChooser filechooser = new JFileChooser(path);
        //ファイル選択ダイアログでdxfファイルのみ指定
        DxfFilter filter = new DxfFilter();
        filechooser.addChoosableFileFilter(filter);
        filechooser.setFileFilter(filter);
        //ファイルダイアログ呼び出し
        if (filechooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = filechooser.getSelectedFile();
            dxfconv.DirPath = file.getParent();
            setFileName(file.getAbsolutePath());
            editFileNameDest();
            setCursor(WAIT_CURSOR);
            dxfconv.setSrc(file);
            setCursor(DEFAULT_CURSOR);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jRadioButtonMenuItemTarget1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemTarget1ActionPerformed
        // For QCAD
        jComboBoxWriteEnc.setSelectedIndex(0);
        jComboBoxConvType.setSelectedIndex(1);
        editFileNameDest();
    }//GEN-LAST:event_jRadioButtonMenuItemTarget1ActionPerformed

    private void jRadioButtonMenuItemTarget2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemTarget2ActionPerformed
        // For Jw_Cad
        jComboBoxWriteEnc.setSelectedIndex(1);
        jComboBoxConvType.setSelectedIndex(0);
        editFileNameDest();
    }//GEN-LAST:event_jRadioButtonMenuItemTarget2ActionPerformed

    private void jRadioButtonMenuItemTarget3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemTarget3ActionPerformed
        // For Old AutoCAD
        jComboBoxWriteEnc.setSelectedIndex(1);
        jComboBoxConvType.setSelectedIndex(0);
        editFileNameDest();
    }//GEN-LAST:event_jRadioButtonMenuItemTarget3ActionPerformed

    private void jRadioButtonMenuItemTarget4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemTarget4ActionPerformed
        // For New AutoCAD
        jComboBoxWriteEnc.setSelectedIndex(0);
        jComboBoxConvType.setSelectedIndex(0);
        editFileNameDest();
    }//GEN-LAST:event_jRadioButtonMenuItemTarget4ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        JOptionPane.showMessageDialog(this, "バージョン　" + dxfconv.version + "\n\n作者：田中　秀宗\nauthor : Hidemune TANAKA\n", "Version", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (dxfconv.sbDest == null) {
            JOptionPane.showMessageDialog(this, "読み込み後に実行してください。\n", "エラー", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        setCursor(WAIT_CURSOR);
        dxfconv.setConv();
        setCursor(DEFAULT_CURSOR);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        dxfconv.writeProp();
    }//GEN-LAST:event_formWindowClosing

    private void jListDestValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListDestValueChanged
        if (jListDest.getSelectedIndex() >= 0) {
            //System.out.println(evt.getSource().toString());
            try {
                if (viewDest == null) {
                    return;
                }
                Point point = viewDest.getViewPosition();
                //もう片方のスクロール
                //インデックスを同期
                viewSrc.setViewPosition(point);
                jListSrc.setSelectedIndex(jListDest.getSelectedIndex());
                //もう片方のスクロール
                //インデックスを同期
                viewConv.setViewPosition(point);
                jListConv.setSelectedIndex(jListDest.getSelectedIndex());
            }catch (Exception e) {
                //何もしない
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jListDestValueChanged

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        dxfconv.ZenHan();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        // TODO add your handling code here:
        int idx = jList1.getSelectedIndex();
        switch(idx){
        case 0:     //QCAD->JwCad
                jRadioButtonMenuItemTarget2.setSelected(true);
                jRadioButtonMenuItemTarget2ActionPerformed(null);
                jComboBoxReadEnc.setSelectedIndex(0);   //UTF-8
                break;
        case 1:     //JwCad->QCAD
                jRadioButtonMenuItemTarget1.setSelected(true);
                jRadioButtonMenuItemTarget1ActionPerformed(null);
                jComboBoxReadEnc.setSelectedIndex(1);   //SJIS
                break;
        default:
                
        }
    }//GEN-LAST:event_jList1ValueChanged

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Task task = new Task(this);
        
        Thread thread = new Thread(task);
        thread.start();
    }//GEN-LAST:event_jButton4ActionPerformed

    private class Task extends Thread {
        private int progressCount = 0;
        private dxfConvJFrame frame;
        public Task(dxfConvJFrame frame) {
            this.frame = frame;
        }
        @Override
        public void run() {
            dxfconv.blnStopMsg = true;
            JFrame f = dxfconv.dxfconvF;
            Container c = f.getContentPane();
            for (int i = 0; i < ModelFiles.size(); i++) {
                jListFiles.setSelectedIndex(i);
                //再描画
                try {
                    //jListFiles.invalidate();
                    //ui.validate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //ファイル名取得
                String str = (String) ModelFiles.getElementAt(i);
                System.out.println(str);
                //読み込み処理
                File file = new File(str);
                dxfconv.DirPath = file.getParent();
                setFileName(file.getAbsolutePath());
                editFileNameDest();
                setCursor(WAIT_CURSOR);
                dxfconv.setSrc(file);
                setCursor(DEFAULT_CURSOR);
                //変換
                jButton2ActionPerformed(null);
                //書き込み
                jButton3ActionPerformed(null);
            }
            ModelFiles.clear();
            dxfconv.blnStopMsg = false;
            JOptionPane.showMessageDialog(dxfconv.dxfconvF, "DXFファイルを更新しました");
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(dxfConvJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(dxfConvJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(dxfConvJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(dxfConvJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new dxfConvJFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox jComboBoxConvType;
    private javax.swing.JComboBox jComboBoxReadEnc;
    private javax.swing.JComboBox jComboBoxWriteEnc;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelMode;
    private javax.swing.JLabel jLabelMode1;
    private javax.swing.JLabel jLabelMode2;
    private javax.swing.JList jList1;
    private javax.swing.JList jListConv;
    private javax.swing.JList jListDest;
    private javax.swing.JList jListFiles;
    private javax.swing.JList jListSrc;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemTarget1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemTarget2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemTarget3;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemTarget4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPaneConv;
    private javax.swing.JScrollPane jScrollPaneDest;
    private javax.swing.JScrollPane jScrollPaneSrc;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldFileName;
    private javax.swing.JTextField jTextFieldFileNameDest;
    // End of variables declaration//GEN-END:variables
}
