package com.paramjot;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XMLParser {
    public static void main(String[] args) {
        String xmlFilePath = "/Users/paramjotsingh/Desktop/XPDMXML.xml";
        Map<String, Map<String, Map<String, String>>> finalResult = new HashMap<>();
        try {
            Document doc = getXMLDoc(xmlFilePath);
            NodeList nodeList = doc.getElementsByTagName("*");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String tagName = element.getTagName();
                    if (tagName.endsWith("Inst") && element.hasAttribute("id")) {
                        String instId = element.getAttribute("id");
                        NodeList childNodes = element.getChildNodes();
                        Map<String, Map<String, String>> ownedInstanceMap = new HashMap<>();
                        for (int j = 0; j < childNodes.getLength(); j++) {
                            Node childNode = childNodes.item(j);
                            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element childElement = (Element) childNode;
                                String childTagName = childElement.getTagName();
                                if ("Owned".equals(childTagName) || "Instancing".equals(childTagName)) {
                                    String ownedInstancingId = childNode.getTextContent();
                                    for (int k = 0; k < nodeList.getLength(); k++) {
                                        Map<String, String> innerMostMap = new HashMap<>();
                                        Node productFastenerNode = nodeList.item(k);
                                        if (productFastenerNode.getNodeType() == Node.ELEMENT_NODE) {
                                            Element productFastenerElement = (Element) productFastenerNode;
                                            String productFastenerTagName = productFastenerElement.getTagName();
                                            if ((productFastenerTagName.equals("Product") || productFastenerTagName.equals("Fastener")) && productFastenerElement.hasAttribute("id") && productFastenerElement.getAttribute("id").equals(ownedInstancingId)) {
                                                String description = getSpecificTagValue(productFastenerElement, "Description");
                                                String id = getSpecificTagValue(productFastenerElement, "ID");
                                                String revision = getSpecificTagValue(productFastenerElement, "RevisionName") + "." + getSpecificTagValue(productFastenerElement, "RevisionIndex");
                                                String name = getSpecificTagValue(productFastenerElement, "Name");
                                                String lifecycle = getSpecificTagValue(productFastenerElement, "Lifecycle");
                                                innerMostMap.put("Description", description);
                                                innerMostMap.put("ID", id);
                                                innerMostMap.put("RevisionName", revision);
                                                innerMostMap.put("Name", name);
                                                innerMostMap.put("Lifecycle", lifecycle);
                                                ownedInstanceMap.put(ownedInstancingId, innerMostMap);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        finalResult.put(instId, ownedInstanceMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String prettyJson = printNestedMapJson(finalResult);
        writeStringToTextFile("/Users/paramjotsingh/Desktop/finalResult", prettyJson);
    }

    private static Document getXMLDoc(String xmlFilePath) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new File(xmlFilePath));
        doc.getDocumentElement().normalize();
        return doc;
    }

    private static String getSpecificTagValue(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Element specificElement = (Element) nodeList.item(0);
            return specificElement.getTextContent();
        }
        return null;
    }

    private static String printNestedMapJson(Map<String, Map<String, Map<String , String>>> finalResult) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(finalResult);
    }

    private static void writeStringToTextFile(String filePath, String text) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
