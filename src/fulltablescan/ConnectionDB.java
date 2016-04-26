/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fulltablescan;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author CDIAZ
 */
public class ConnectionDB {

    private Connection con;
    
    public Statement stmt;
    
    private String user = "";
    private String pass = "";
    private String host = "";
    private String port = "";
    private String db = "";
    private final String jdbc = "jdbc:oracle:thin:";
    
    
    public ConnectionDB(String dir) {
        try {
            readConnectionData(dir);
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection(jdbc + "@" + host + ":" + port + "/" + db , user, pass);
            con.setAutoCommit(false);
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Mensaje: " + ex.getMessage() + "\nError: " + ex);
            System.out.println("Chequear conexion VPN");
            System.exit(-1);
        }
    }
    
    private void readConnectionData(String dir){
        try {
            File connectionData = new File(dir + "\\Connection.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(connectionData);
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            
            NodeList nList = doc.getElementsByTagName("connection");
            
            Node nNode = nList.item(0);//Es solo para una conexion
            
            Element eElement = (Element) nNode;
            user = eElement.getAttribute("user");
            pass = eElement.getAttribute("pass");
            host = eElement.getAttribute("host");
            port = eElement.getAttribute("port");
            db = eElement.getAttribute("db");
            
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            System.out.println("Mensaje: " + ex.getMessage() + "\nError: " + ex);
            System.exit(-1);
        }
    }
    
    public void close(){
        try {
            con.close();
        } catch (SQLException ex) {
            System.out.println("Mensaje: " + ex.getMessage() + "\nError: " + ex);
        }
    }
}
