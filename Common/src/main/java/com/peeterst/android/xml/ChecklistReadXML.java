package com.peeterst.android.xml;

import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 31/12/10
 * Time: 14:03
 *
 */
public class ChecklistReadXML {

    public static String TITLE = "Title";
    public static String CREDAT = "CreationDate";
    public static String TARDAT = "TargetDate";
    public static String ITEMS = "Items";
    public static String ITEM = "Item";
    public static String TEXT = "Text";
    public static String CHECKED = "Checked";
    public static String ITEMCREDAT = "ItemCreationDate";

    private Document doc;
    private Checklist checklist;

    public ChecklistReadXML(InputStream in) throws ParserConfigurationException, IOException, SAXException {
        doc = parse(in);
        if(doc != null) {
            checklist = createChecklist(doc);
        }
        in.close();
    }

    public ChecklistReadXML(String data) throws ParserConfigurationException, IOException, SAXException {
        if(data != null){
            checklist = createChecklist(parse(data));
        }
    }

    private Document parse(InputStream in) throws ParserConfigurationException, IOException, SAXException {
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            return fact.newDocumentBuilder().parse(in,"UTF-8");
        }catch (SAXParseException e){
            e.printStackTrace();        //todo: fix attempt?
            throw e;
//            return null;
        }
    }

    private Document parse(String data) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();


        Document document = documentBuilderFactory.newDocumentBuilder().newDocument();

        Node checklistNode = builder.parse(new InputSource(new StringReader(data))).getDocumentElement();
        checklistNode = document.adoptNode(checklistNode);
        document.appendChild(checklistNode);
        return document;
    }

    public Checklist createChecklist(Document doc){

        NodeList checkList = doc.getElementsByTagName("Checklist");

        for(int i = 0; i < checkList.getLength(); i++ ){

            Node elementNode = checkList.item(i);
                if(elementNode.getNodeType() == Node.ELEMENT_NODE){
                    String title = getNodeValue((Element) elementNode,TITLE);
                    String creationDat = getNodeValue((Element) elementNode,CREDAT);
                    String tarDat = getNodeValue((Element) elementNode,TARDAT);

                    Checklist checklist = new Checklist(title,Long.parseLong(tarDat));
                    checklist.setCreationDatestamp(Long.parseLong(creationDat));

                    NodeList itemsNodeList = ((Element)elementNode).getElementsByTagName(ITEMS);



                    for(int j=0; j < itemsNodeList.getLength(); j++){
                        Node itemsNode = itemsNodeList.item(j);

                        if(itemsNode.getNodeType() == Node.ELEMENT_NODE){

                            NodeList deeperNodeList = itemsNode.getChildNodes();

                            if(deeperNodeList.getLength() > 0) {

                                List<ChecklistItem> itemList = new ArrayList<ChecklistItem>();

                                for(int k = 0; k < deeperNodeList.getLength(); k++){
                                    Node itemNode = deeperNodeList.item(k);
                                    if(itemNode.getNodeType() == Node.ELEMENT_NODE){
                                        Element element = (Element) itemNode;
                                        String txt = getNodeValue(element,TEXT);
                                        if(txt == null){
                                            txt = "";
                                        }
                                        String checked = getNodeValue(element,CHECKED);
                                        String itemCreDat = getNodeValue(element,ITEMCREDAT);

                                        long creDat;

                                        //todo: serves to give earlier items a creationdate, remove when obsolete
                                        if(itemCreDat == null){
                                            creDat = new java.util.Date().getTime();
                                            try { //we wait a little so we've got unique credats
                                                Thread.sleep(5);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                            }
                                        }else {
                                            creDat = Long.parseLong(itemCreDat);
                                        }

                                        ChecklistItem item = new ChecklistItem(txt);
                                        item.setTaken(Boolean.parseBoolean(checked));
                                        item.setCreationDatestamp(creDat);
                                        itemList.add(item);
                                    }

                                }
                                checklist.setItems(itemList);

                            }
                        }
                    }

                    return checklist;


                }

        }

        return null;
    }

    private String getNodeValue(Element node, String tagName){
        NodeList tagList = node.getElementsByTagName(tagName);
        Element tagElement = (Element)tagList.item(0);

        if(tagElement == null) return null;

        NodeList tagChildList = tagElement.getChildNodes();



//        System.out.println("First Name : " +
//                ((Node)tagChildList.item(0)).getNodeValue().trim());
        if(tagChildList != null && tagChildList.item(0) != null) {
            return tagChildList.item(0).getNodeValue().trim();
        }else return null;
    }

    public Checklist getChecklist(){
        return this.checklist;
    }

    public Document getDocument() {
        return doc;
    }

    public enum Voorbeeld {
        VOORBEELD1("Voorbeeld 1"),VOORBEELD2("Voorbeeld2"); //...

        //enum constructor
        Voorbeeld(String naam){
            this.naam = naam;
        }

        private String naam;

        public String getNaam() {
            return naam;
        }
    }

    public class GebruiksVoorbeeld{

        private String gebruiksNaam;

        public GebruiksVoorbeeld(Voorbeeld voorbeeld){
            this.gebruiksNaam = voorbeeld.getNaam();
        }
    }

}
