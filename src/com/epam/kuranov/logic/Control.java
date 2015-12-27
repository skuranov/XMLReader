package com.epam.kuranov.logic;

import com.epam.kuranov.data.Data;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import com.epam.kuranov.ui.Window;
import java.util.ArrayList;
import java.util.List;

public class Control {

    private Data data = null;
        private static Window window = null;

    public Data getData() {
        return data;
    }

    private Control(){
        this.data = new Data();
    }

    public static void main(String[] args) throws Exception {
        Control control = new Control();
        window = new Window(control);
        }

        public ArrayList<Node> getElementsFromNList(NodeList nodeList) {
            ArrayList<Node> nodes = new ArrayList<>();
            for (int k = 0; k < nodeList.getLength(); k++) {
                Node node = nodeList.item(k);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    nodes.add(node);
                }
            }
            return nodes;
        }

        public void showTask(Object treeObj){
            NodeList nList = data.getDocument().getElementsByTagName("tns:courses");
            NodeList coursesList = nList.item(0).getChildNodes();
            final String[] path = window.getXmlTree().getSelectionPath().toString().split(", ");
            StringBuilder stringBuilder = new StringBuilder();
            String taskId = "";
            for (int i = 0; i < coursesList.getLength(); i++) {
                Node nNode =  coursesList.item(i);
                List<Node> taskList = getElementsFromNList(nNode.getChildNodes());
                for (Node taskNode : taskList) {
                    Element eElement = (Element) taskNode;
                    if (treeObj.toString().equals(eElement.getAttribute("taskName"))) {
                        stringBuilder.append("Title: ").
                                append(eElement.getAttribute("taskName")).
                                append("\r\n").append("Duration (hrs): ").
                                append(eElement.getAttribute("planTime")).
                                append("\r\n");
                        taskId = eElement.getAttribute("taskId");
                    }
                }
            }
            DocumentTraversal traversable = (DocumentTraversal) data.getDocument();
            NodeIterator xmlIterator = traversable.createNodeIterator(
                    data.getDocument(), NodeFilter.SHOW_ELEMENT, new NodeFilter() {
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
                    stringBuilder.append("Status: ").
                            append(rootElement.getAttribute("status")).append("\r\n");
                    stringBuilder.append("Mark: ").
                            append(rootElement.getAttribute("mark")).append("\r\n");
                    stringBuilder.append("Type: ").
                            append(rootElement.getTagName()).
                            append("\r\n");
                    window.getInfoArea().setText(stringBuilder.toString());
                }
            }
        }

        public void showCourse(Object treeObj){
            NodeList nList = data.getDocument().getElementsByTagName("tns:courses");
            List<Node> coursesList = getElementsFromNList(nList.item(0).getChildNodes());
            for (Node nNode : coursesList) {
                Element eElement = (Element) nNode;
                StringBuilder stringBuilder = new StringBuilder("\r\n" + "Tasks: " + "\r\n");
                List<Node> taskList = getElementsFromNList(nNode.getChildNodes());
                for (Node courseNode : taskList) {
                    stringBuilder.append(((Element) courseNode).getAttribute("taskName")).
                            append("\r\n");
                }
                if (treeObj.toString().equals(eElement.getAttribute("courseName"))) {
                    window.getInfoArea().setText("Title: " + eElement.getAttribute("courseName") + "\r\n" +
                            "Author: " + eElement.getAttribute("author") + "\r\n" +
                            "Last modified: " + eElement.getAttribute("birthDate") + "\r\n" + stringBuilder);
                }
            }
        }

        public void showProgram(Object treeObj){
            NodeList nList = data.getDocument().getElementsByTagName("tns:programs");
            List<Node> programList = getElementsFromNList(nList.item(0).getChildNodes());
            for (Node nNode : programList) {
                Element eElement = (Element) nNode;
                StringBuilder stringBuilder = new StringBuilder("\r\n" + "Courses: " + "\r\n");
                List<Node> courseList = getElementsFromNList(nNode.getChildNodes());
                for (Node courseNode : courseList) {
                    stringBuilder.append(((Element) courseNode).getAttribute("courseName")).
                            append("\r\n");
                }
                if (treeObj.toString().equals(eElement.getAttribute("programName"))) {
                    window.getInfoArea().setText("Title: " + eElement.getAttribute("programName") + "\r\n" +
                            "Author: " + eElement.getAttribute("author") + "\r\n" +
                            "Last modified: " + eElement.getAttribute("birthDate") + "\r\n" + stringBuilder);
                }
            }
        }

        public void showStudent(Object treeObj){
            NodeList nList = data.getDocument().getElementsByTagName("tns:student");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                Element eElement = (Element) nNode;
                if(treeObj.toString().equals(eElement.getAttribute("fullName"))){
                    String tmpStrSignedContr;
                    if (eElement.getAttribute("signedContract").equals("true")){tmpStrSignedContr = "yes";}
                    else {tmpStrSignedContr = "no";}
                    window.getInfoArea().setText("Full Name: " + eElement.getAttribute("fullName") + "\r\n" +
                            "E-mail: " + eElement.getAttribute("email") + "\r\n" +
                            "Region: " + eElement.getAttribute("city") + "\r\n" +
                            "Contract Signed: " + tmpStrSignedContr + "\r\n" +
                            "Start Date: " + eElement.getAttribute("startDate"));
                }
            }
        }


}
