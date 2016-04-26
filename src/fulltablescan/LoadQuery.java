/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fulltablescan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author CDIAZ
 */
public class LoadQuery {

    private ArrayList<File> files;
    private ArrayList<String> querys;
    
    public LoadQuery(String dir) {
        files = new ArrayList<>();
        querys = new ArrayList<>();
        getFiles(dir);
        readXmlFiles();
    }

    public ArrayList<String> getQuerys() {
        return querys;
    }
    
    private void getFiles(String dir){
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            //Se ignora el archivo que tiene la conxion a la basse de datos
            if (listOfFiles[i].isFile() && !listOfFiles[i].getName().equals("Connection.xml")) {
                files.add(listOfFiles[i]);
//                System.out.println("File " + listOfFiles[i].getName());
            }
        }
    }
    
    private void readXmlFiles(){

        try {
            for (int i = 0; i < files.size(); i++) {
                File fXmlFile = files.get(i);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                //optional, but recommended
                //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
                doc.getDocumentElement().normalize();
//                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                
                NodeList nList = doc.getElementsByTagName("select");
                
                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);
                    
//                    System.out.println("\nCurrent Element :" + nNode.getNodeName());
                    
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;
//                            System.out.println("Staff id : " + eElement.getAttribute("id"));
//                            System.out.println("Cuerpo : " + eElement.getTextContent());
                        String aux = eElement.getTextContent();
                        if (aux.startsWith("<![CDATA[")) {
                            aux = aux.replace("<![CDATA[", "");
                            aux = aux.replace("]]>", "");
                        }
                        //Por el momento no se analizan consultas dinamicas
                        if (eElement.getElementsByTagName("if").getLength() == 0 && !aux.contains("like")) {
                            querys.add(aux);
                        }
//                            System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
//                            System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
//                            System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());
                    }
                }
            }
        } 
        catch (ParserConfigurationException | SAXException | IOException | DOMException e) {
            System.out.println("Mensaje: " + e.getMessage() + "\nError: " + e);
            System.exit(-1);
        }
    }
}
