package com.epam.kuranov.ui;

import com.epam.kuranov.logic.Control;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static java.awt.BorderLayout.*;

public class Window extends JFrame {
    private Control control;
    private JTextArea messageArea;
    private JTextArea infoArea;
    private JTree xmlTree;

    public JTree getXmlTree() {
        return xmlTree;
    }

    public JTextArea getInfoArea() {
        return infoArea;
    }

    public Window(Control control){
        this.control = control;
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(new BorderLayout());
        this.setJMenuBar(this.makeMenuBar());
        this.add(this.makeMessagebox(), NORTH);
        this.add(this.makeTreeAndInfoField(), CENTER);
        this.setVisible(true);
    }

    private JMenuBar makeMenuBar(){
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Import from XML...");
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(exitItem);
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = control.getData().validate("StudentsReport.xsd","Report.xml");
                messageArea.setText(message);
                if(message.contains("is valid")){
                    showInfoFromXML();
                }
            }
        });
        menuBar.add(fileMenu);
        return menuBar;
    }

    private JPanel makeMessagebox(){
        messageArea = new JTextArea();
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setText("Message...");
        JScrollPane scrollingArea = new JScrollPane(messageArea);
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(scrollingArea, BorderLayout.CENTER);
        messageArea.setEditable(false);
        return content;
    }

    private JPanel makeTreeAndInfoField()throws HeadlessException{
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("ROOT");
        this.xmlTree = new JTree(top);
        xmlTree.setMinimumSize(new Dimension(300,400));
        JPanel jPanel = new JPanel();
        infoArea = new JTextArea();
        JScrollPane jsp = new JScrollPane(xmlTree);
        infoArea.setMaximumSize(new Dimension(500,400));
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add("Center", jsp);
        jsp.add(infoArea);
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jPanel.add(jsp);
        jPanel.add(infoArea);
        setBounds(200, 200, 800, 800);
        xmlTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                changeInfo();
            }
        });
        return jPanel;
    }



    private void changeInfo() {
        Object obj = getXmlTree().getLastSelectedPathComponent();
        if (obj != null) {
            DefaultMutableTreeNode sel = (DefaultMutableTreeNode) obj;
            if (sel.getLevel() == 4){control.showTask(obj);}
            if (sel.getLevel() == 3){control.showCourse(obj);}
            if (sel.getLevel() == 2){control.showProgram(obj);}
            if (sel.getLevel() == 1) {control.showStudent(obj);}
        }
    }


    public void showInfoFromXML(){
        control.getData().getDocument("Report.xml").getElementsByTagName("tns:student");
        java.util.List<Node> nList = control.getElementsFromNList(
                control.getData().getDocument().getElementsByTagName("tns:student"));
        for (Node nNode : nList) {
            Element eElement = (Element) nNode;
            DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(eElement.getAttribute("fullName"));
            DefaultTreeModel model = (DefaultTreeModel) getXmlTree().getModel();
            Object obj = model.getRoot();
            DefaultMutableTreeNode sel = (DefaultMutableTreeNode) obj;
            model.insertNodeInto(tmp, sel, sel.getChildCount());
            java.util.List<Node> programNodeList = control.getElementsFromNList(nNode.getChildNodes());
            DefaultMutableTreeNode selProgram = sel.getLastLeaf();
            for (Node programNode : programNodeList) {
                Element programElement = (Element) programNode;
                DefaultMutableTreeNode tmpProgram =
                        new DefaultMutableTreeNode(programElement.getAttribute("programName"));
                model.insertNodeInto(tmpProgram, selProgram, selProgram.getChildCount());
                List<Node> coursesNodeList = control.getElementsFromNList(programNode.getChildNodes());
                DefaultMutableTreeNode selCourse = selProgram.getLastLeaf();
                for (Node courseNode : coursesNodeList) {
                    Element courseElement = (Element) courseNode;
                    DefaultMutableTreeNode tmpCourse =
                            new DefaultMutableTreeNode(courseElement.getAttribute("courseName"));
                    model.insertNodeInto(tmpCourse, selCourse, selCourse.getChildCount());
                    List<Node> tasksNodeList = control.getElementsFromNList(courseNode.getChildNodes());
                    DefaultMutableTreeNode selTask = selCourse.getLastLeaf();
                    for (Node taskNode : tasksNodeList) {
                        Element taskElement = (Element) taskNode;
                        DefaultMutableTreeNode tmpTask = new DefaultMutableTreeNode
                                (taskElement.getAttribute("taskName"));
                        model.insertNodeInto(tmpTask, selTask, selTask.getChildCount());
                    }
                }
            }
        }
    }

}
