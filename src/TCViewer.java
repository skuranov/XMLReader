import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TCViewer extends JFrame {
        private static JTextArea messageArea;
        private static TCViewer trviewer;
        private static JTree xmlTree;
        private static DefaultMutableTreeNode top;
        private static JTextArea infoArea;
        private static Document readedXML;

        public static void main(String[] args) throws IOException, SAXException {
            trviewer = new TCViewer();
            trviewer.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            trviewer.setResizable(false);
            trviewer.setLocationRelativeTo(null);
            trviewer.getContentPane().setLayout(new BorderLayout());
            trviewer.setJMenuBar(trviewer.makeMenuBar());
            trviewer.add(trviewer.makeMessagebox(), BorderLayout.NORTH);
            trviewer.add(trviewer.makeTreeAndInfoField(), BorderLayout.CENTER);
            trviewer.setVisible(true);
        }

        public String parseXMLFile (String schemaFileName, String xmlFileName)
        {
            Source schemaFile = new StreamSource(schemaFileName);
            Source xmlFile = new StreamSource(xmlFileName);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = null;
            try {
                schema = schemaFactory.newSchema(schemaFile);
            } catch (SAXException e) {
                e.printStackTrace();
            }
            Validator validator = schema.newValidator();
            try {validator.validate(xmlFile);
                try {
                    File file = new File("Report.xml");
                    DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    readedXML = dBuilder.parse(file);
                    readedXML.getDocumentElement().normalize();
                    showInfoFromXML();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return xmlFile.getSystemId() + " is valid";
            } catch (SAXException e) {
                DefaultTreeModel model = (DefaultTreeModel)xmlTree.getModel();
                top.removeAllChildren();
                model.reload();
                return xmlFile.getSystemId() + " is NOT valid Reason: " + e.getLocalizedMessage();
            } catch (IOException e) {
                DefaultTreeModel model = (DefaultTreeModel)xmlTree.getModel();
                top.removeAllChildren();
                model.reload();
                return xmlFile.getSystemId() + " IO Error: " + e.getLocalizedMessage();
            }
        }
        public JPanel makeTreeAndInfoField()throws HeadlessException{
            top = new DefaultMutableTreeNode("ROOT");
            xmlTree = new JTree(top);
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

        public JMenuBar makeMenuBar(){
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
                    messageArea.setText(trviewer.parseXMLFile("StudentsReport.xsd","Report.xml"));
                }
            });
            menuBar.add(fileMenu);
            return menuBar;
        }

        public JPanel makeMessagebox(){
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

        public static ArrayList<Node> getElementsFromNList(NodeList nodeList) {
            ArrayList<Node> nodes = new ArrayList<>();
            for (int k = 0; k < nodeList.getLength(); k++) {
                Node node = nodeList.item(k);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    nodes.add(node);
                }
            }
            return nodes;
        }

        public void showInfoFromXML(){
            List<Node> nList = getElementsFromNList(readedXML.getElementsByTagName("tns:student"));
            DocumentTraversal traversable = (DocumentTraversal) readedXML;
            NodeIterator xmlIterator = traversable.createNodeIterator(
                    readedXML, NodeFilter.SHOW_ELEMENT, null, true);
            for (int i = 0; i < nList.size(); i++) {
                Node nNode = nList.get(i);
                Element eElement = (Element) nNode;
                DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(eElement.getAttribute("fullName"));
                DefaultTreeModel model = (DefaultTreeModel)xmlTree.getModel();
                Object obj = model.getRoot();
                DefaultMutableTreeNode sel = (DefaultMutableTreeNode)obj;
                model.insertNodeInto(tmp, sel, sel.getChildCount());
                List<Node> programNodeList = getElementsFromNList(nNode.getChildNodes());
                DefaultMutableTreeNode selProgram = sel.getLastLeaf();
                for(int j = 0; j < programNodeList.size(); j++){
                    Node programNode = programNodeList.get(j);
                    Element programElement = (Element) programNode;
                    DefaultMutableTreeNode tmpProgram =
                            new DefaultMutableTreeNode(programElement.getAttribute("programName"));
                    model.insertNodeInto(tmpProgram, selProgram, selProgram.getChildCount());
                    List<Node> coursesNodeList = getElementsFromNList(programNode.getChildNodes());
                    DefaultMutableTreeNode selCourse = selProgram.getLastLeaf();
                    for (int k = 0; k < coursesNodeList.size(); k++) {
                        Node courseNode = coursesNodeList.get(k);
                        Element courseElement = (Element) courseNode;
                        DefaultMutableTreeNode tmpCourse =
                                new DefaultMutableTreeNode(courseElement.getAttribute("courseName"));
                        model.insertNodeInto(tmpCourse, selCourse, selCourse.getChildCount());
                        List<Node> tasksNodeList = getElementsFromNList(courseNode.getChildNodes());
                        DefaultMutableTreeNode selTask = selCourse.getLastLeaf();
                        for(int l = 0; l < tasksNodeList.size(); l++){
                            Node taskNode = tasksNodeList.get(l);
                            Element taskElement = (Element) taskNode;
                            DefaultMutableTreeNode tmpTask = new DefaultMutableTreeNode
                                    (taskElement.getAttribute("taskName"));
                            model.insertNodeInto(tmpTask, selTask, selTask.getChildCount());
                        }
                    }
                }
            }
        }

        public void changeInfo() {
            Object obj = xmlTree.getLastSelectedPathComponent();
            if (obj != null) {
                DefaultMutableTreeNode sel = (DefaultMutableTreeNode) obj;
                if (sel.getLevel() == 4){showTask(obj);}
                if (sel.getLevel() == 3){showCourse(obj);}
                if (sel.getLevel() == 2){showProgram(obj);}
                if (sel.getLevel() == 1) {showStudent(obj);}
            }
        }

        public void showTask(Object treeObj){
            NodeList nList = readedXML.getElementsByTagName("tns:courses");
            NodeList coursesList = nList.item(0).getChildNodes();
            final String[] path = xmlTree.getSelectionPath().toString().split(", ");
            StringBuilder stringBuilder = new StringBuilder();
            String taskId = "";
            for (int i = 0; i < coursesList.getLength(); i++) {
                Node nNode =  coursesList.item(i);
                List<Node> taskList = getElementsFromNList(nNode.getChildNodes());
                for(int j = 0; j < taskList.size(); j++){
                    Node taskNode =  taskList.get(j);
                    Element eElement = (Element) taskNode;
                    if(treeObj.toString().equals(eElement.getAttribute("taskName"))){
                        stringBuilder.append("Title: " + eElement.getAttribute("taskName") + "\r\n" +
                                        "Duration (hrs): " + eElement.getAttribute("planTime")+ "\r\n");
                        taskId = eElement.getAttribute("taskId");
                    }
                }
            }
            DocumentTraversal traversable = (DocumentTraversal) readedXML;
            NodeIterator xmlIterator = traversable.createNodeIterator(
                    readedXML, NodeFilter.SHOW_ELEMENT, new NodeFilter() {
                        @Override
                        public short acceptNode(Node node) {
                            if ((node.getNodeName().contains("theoryTask")||
                                    node.getNodeName().contains("practicalTask"))&&
                            ((Element)node.
                                    getParentNode()).
                                    getAttribute("courseName").equals(path[3])&&
                            ((Element)node.
                                    getParentNode().
                                    getParentNode()).
                                    getAttribute("programName").equals(path[2])&&
                            ((Element)node.getParentNode().
                                    getParentNode().
                                    getParentNode()).
                                    getAttribute("fullName").equals(path[1])){
                                return FILTER_ACCEPT;
                            }
                            else return FILTER_REJECT;
                        }
                    }, true);

            Element rootElement;
            while ((rootElement = (Element) xmlIterator.nextNode()) != null){
                if (rootElement.getAttribute("taskId").equals(taskId)) {
                    stringBuilder.append("Status: " + rootElement.
                            getAttribute("status") + "\r\n");
                    stringBuilder.append("Mark: " + rootElement.
                            getAttribute("mark") + "\r\n");
                    stringBuilder.append("Type: " +
                            rootElement.getTagName() + "\r\n");
                    infoArea.setText(stringBuilder.toString());
                }
            }
        }

        public void showCourse(Object treeObj){
            NodeList nList = readedXML.getElementsByTagName("tns:courses");
            List<Node> coursesList = getElementsFromNList(nList.item(0).getChildNodes());
            for (int temp = 0; temp < coursesList.size(); temp++) {
                Node nNode = coursesList.get(temp);
                Element eElement = (Element) nNode;
                StringBuilder stringBuilder = new StringBuilder("\r\n" + "Tasks: " + "\r\n");
                List<Node> taskList = getElementsFromNList(nNode.getChildNodes());
                for(int j = 0; j< taskList.size(); j++){
                    Node courseNode = taskList.get(j);
                    stringBuilder.append(((Element) courseNode).getAttribute("taskName") + "\r\n");
                }
                if(treeObj.toString().equals(eElement.getAttribute("courseName"))){
                    infoArea.setText("Title: " + eElement.getAttribute("courseName") + "\r\n" +
                            "Author: " + eElement.getAttribute("author") + "\r\n" +
                            "Last modified: " + eElement.getAttribute("birthDate") + "\r\n" + stringBuilder);
                }
            }
        }

        public void showProgram(Object treeObj){
            NodeList nList = readedXML.getElementsByTagName("tns:programs");
            List<Node> programList = getElementsFromNList(nList.item(0).getChildNodes());
            for (int temp = 0; temp < programList.size(); temp++) {
                Node nNode = programList.get(temp);
                Element eElement = (Element) nNode;
                StringBuilder stringBuilder = new StringBuilder("\r\n" + "Courses: " + "\r\n");
                List <Node> courseList = getElementsFromNList(nNode.getChildNodes());
                for(int j = 0; j< courseList.size(); j++){
                    Node courseNode = courseList.get(j);
                    stringBuilder.append(((Element) courseNode).getAttribute("courseName") + "\r\n");
                }
                if(treeObj.toString().equals(eElement.getAttribute("programName"))){
                    infoArea.setText("Title: " + eElement.getAttribute("programName") + "\r\n" +
                            "Author: " + eElement.getAttribute("author") + "\r\n" +
                            "Last modified: " + eElement.getAttribute("birthDate") + "\r\n" + stringBuilder);
                }
            }
        }

        public void showStudent(Object treeObj){
            NodeList nList = readedXML.getElementsByTagName("tns:student");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                Element eElement = (Element) nNode;
                if(treeObj.toString().equals(eElement.getAttribute("fullName"))){
                    String tmpStrSignedContr;
                    if (eElement.getAttribute("signedContract").equals("true")){tmpStrSignedContr = "yes";}
                    else {tmpStrSignedContr = "no";}
                    infoArea.setText("Full Name: " + eElement.getAttribute("fullName") + "\r\n" +
                            "E-mail: " + eElement.getAttribute("email") + "\r\n" +
                            "Region: " + eElement.getAttribute("city") + "\r\n" +
                            "Contract Signed: " + tmpStrSignedContr + "\r\n" +
                            "Start Date: " + eElement.getAttribute("startDate"));
                }
            }
        }







}
