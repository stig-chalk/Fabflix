import java.io.IOException;
import java.sql.*;
import java.util.*;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


import org.xml.sax.helpers.DefaultHandler;

public class XMLParser_stars extends DefaultHandler {

    private String fileName, tempVal;
    private HashSet<star> storedStarRecords, newStarRecords;
    private PrintWriter pwS;
    private star tempStar;

    public XMLParser_stars(String xmlFile) {
        fileName = xmlFile;
    }

    public void startParsing() throws ClassNotFoundException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        storedStarRecords = new HashSet<>();
        newStarRecords = new HashSet<>();
        try {
            Class.forName(XMLParser.JDBC_DRIVER);
            pwS = new PrintWriter(new File("newStars.csv"));

            Connection conn = DriverManager.getConnection(XMLParser.DB_URL, "mytestuser", "mypassword");

            // get largest star id
            Statement st0 = conn.createStatement();
            ResultSet rs = st0.executeQuery("select max(id) as id from stars");
            rs.next();
            star.largestId = rs.getString("id");

            // store all recorded stars
            Statement st = conn.createStatement();
            setUpStoredStarRecords(st);

            rs.close();
            st.close();
            st0.close();

            SAXParser sp = spf.newSAXParser();
            sp.parse(fileName, this);
            pwS.close();

            Statement st1 = conn.createStatement();
            String load_stars = "LOAD DATA LOCAL INFILE 'newStars.csv' INTO TABLE stars " +
                    "FIELDS TERMINATED BY ';;' " +
                    "LINES TERMINATED BY '\n' " +
                    "(id, name, birthYear);";
            st1.executeQuery(load_stars);
            st1.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpStoredStarRecords (Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("select * from stars");
        while (rs.next()) {
            storedStarRecords.add(new star(rs.getString("id"), rs.getString("name"),
                    (rs.getString("birthYear") != null ? Integer.parseInt(rs.getString("birthYear")) : -1)));
        }
        System.out.println(storedStarRecords.size());
        rs.close();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("actor"))
            tempStar = new star();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            if (!storedStarRecords.contains(tempStar)) {
                int n = newStarRecords.size();
                newStarRecords.add(tempStar);
                if (n < newStarRecords.size() && !tempStar.getName().isEmpty()) {
                    pwS.write(tempStar.toString() + "\n");
                } else System.out.println(tempStar);
            }
        } else if (qName.equalsIgnoreCase("stagename"))
            tempStar.setName(tempVal);
        else if (qName.equalsIgnoreCase("dob"))
            tempStar.setBirthYear(tempVal);
    }
}
