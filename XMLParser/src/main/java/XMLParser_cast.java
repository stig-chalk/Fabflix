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

public class XMLParser_cast extends DefaultHandler {
    private String fileName, tempVal;
    private HashSet<cast> storedCastRecords, newCastRecords;
    private PrintWriter pwC;
    private cast tempCast;

    public XMLParser_cast(String xmlFile) {
        fileName = xmlFile;
    }

    public void startParsing() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        storedCastRecords = new HashSet<>();
        newCastRecords = new HashSet<>();
        try {
            Class.forName(XMLParser.JDBC_DRIVER);
            pwC = new PrintWriter(new File("newCasts.csv"));

            Connection conn = DriverManager.getConnection(XMLParser.DB_URL, "mytestuser", "mypassword");

            // set movie map
            Statement st = conn.createStatement();
            setUpMovieMap(st);
            st.close();

            // set star map
            Statement st1 = conn.createStatement();
            setUpStarMap(st1);
            st1.close();

            // set stored cast set
            Statement st2 = conn.createStatement();
            setUpStoredCastRecords(st2);
            st2.close();

            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(fileName, this);
            pwC.close();

            // insert into db
            Statement st3 = conn.createStatement();
            String load_gms = "LOAD DATA LOCAL INFILE 'newCasts.csv' INTO TABLE stars_in_movies " +
                            "FIELDS TERMINATED BY ';;' " +
                            "LINES TERMINATED BY '\n' " +
                            "(movieId, starId);";
            st3.executeQuery(load_gms);
            st3.close();

        } catch (SQLException | FileNotFoundException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private void setUpMovieMap(Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("select title, id from movies");
        while (rs.next()) {
            cast.movieMap.put(rs.getString("title"), rs.getString("id"));
        }
        System.out.println("movie size: " + cast.movieMap.size());
        rs.close();
    }

    private void setUpStarMap(Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("select name, id from stars");
        while (rs.next()) {
            cast.starMap.put(rs.getString("name"), rs.getString("id"));
        }
        System.out.println("star size: " + cast.starMap.size());
        rs.close();
    }

    private void setUpStoredCastRecords(Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("select * from stars_in_movies");
        while (rs.next()) {
            storedCastRecords.add(new cast(rs.getString("movieId"), rs.getString("starId")));
        }
        System.out.println("sm size: " + storedCastRecords.size());
        rs.close();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("m"))
            tempCast = new cast();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("m")) {
            if (!storedCastRecords.contains(tempCast)) {
                int n = newCastRecords.size();
                newCastRecords.add(tempCast);
                if (n < newCastRecords.size() && tempCast.getMovieId() != null && tempCast.getStarId() != null) {
                    pwC.write(tempCast.toString() + "\n");
                }  else System.out.println(tempCast);
            }
        } else if (qName.equalsIgnoreCase("t"))
            tempCast.setMovieId(tempVal);
        else if (qName.equalsIgnoreCase("a"))
            tempCast.setStarId(tempVal);
    }
}
