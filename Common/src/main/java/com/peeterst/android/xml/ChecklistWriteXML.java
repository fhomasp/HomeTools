package com.peeterst.android.xml;

import com.peeterst.android.model.Checklist;
import com.peeterst.android.model.ChecklistItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 31/12/10
 * Time: 0:48
 * Responsible for writing the checklist POJO to XML
 */
public class ChecklistWriteXML {

    private Document checklistXmlDocument;

    public ChecklistWriteXML(Checklist checklist){
        checklistXmlDocument = makeDoc(checklist);

    }

    private Document makeDoc(Checklist checklist) {
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = fact.newDocumentBuilder();
            Document doc = parser.newDocument();

            Node root = doc.createElement("Checklist");
            doc.appendChild(root);

            Node titleNode = doc.createElement("Title");
            if(checklist.getTitle() == null) {
                titleNode.appendChild(doc.createTextNode(""));
            }else {
                titleNode.appendChild(doc.createTextNode(checklist.getTitle()));
            }
            root.appendChild(titleNode);

            Node creationDateNote = doc.createElement("CreationDate");
            creationDateNote.appendChild(doc.createTextNode(""+checklist.getCreationDatestamp()));
            root.appendChild(creationDateNote);

            Node targetDateNote = doc.createElement("TargetDate");
            targetDateNote.appendChild(doc.createTextNode(""+checklist.getTargetDatestamp()));
            root.appendChild(targetDateNote);

            Node itemsNode = doc.createElement("Items");
            for(ChecklistItem item:checklist.getItems()){
                Node itemNode = doc.createElement("Item");

                Node itemText = doc.createElement("Text");
                if(item.getBulletName() == null){
                    itemText.appendChild(doc.createTextNode(""));
                }else {
                    itemText.appendChild(doc.createTextNode(item.getBulletName()));
                }
                itemNode.appendChild(itemText);

                Node checked = doc.createElement("Checked");
                checked.appendChild(doc.createTextNode(
                        ""+item.isTaken()));
                itemNode.appendChild(checked);

                Node creDat = doc.createElement("ItemCreationDate");
                creDat.appendChild(doc.createTextNode(""+item.getCreationDatestamp()));
                itemNode.appendChild(creDat);

                itemsNode.appendChild(itemNode);
            }

            root.appendChild(itemsNode);


            return doc;

        } catch (Exception ex) {
            //TODO throw up ;-)
            System.err.println("+============================+");
            System.err.println("|        XML Error           |");
            System.err.println("+============================+");
            System.err.println(ex.getClass());
            ex.printStackTrace();
            System.err.println("+============================+");
            return null;
        }


    }

    public Document getChecklistXmlDocument() {
        return checklistXmlDocument;
    }

    public String createStringVersion(){
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(checklistXmlDocument), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            return output;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
