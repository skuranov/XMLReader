package com.epam.kuranov.data;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;


public class Data {
    private Document document = null;

    public Data() {
    }

    public Document getDocument() {
        return document;
    }


    private Schema getSchema(SchemaFactory schemaFactory, Source schemaFile){
        try {
            return schemaFactory.newSchema(schemaFile);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String validate(String schemaFileName, String xmlFileName) {
        Source schemaFile = new StreamSource(schemaFileName);
        Source xmlFile = new StreamSource(xmlFileName);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = getSchema(schemaFactory,schemaFile);
        Validator validator = schema.newValidator();
        try {
            validator.validate(xmlFile);
            return xmlFile.getSystemId() + " is valid";
        } catch (Exception e) {
            return xmlFile.getSystemId() + " Error: " + e.getLocalizedMessage();
        }
    }


    public Document getDocument(String fileName) {
        document = null;
        File file = new File(fileName);
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            document = dBuilder != null ? dBuilder.parse(file) : null;
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        assert document != null;
        document.getDocumentElement().normalize();
        return document;
    }

}
