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
public class XMLParser extends DefaultHandler  {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb?useSSL=false";

    private String fileName, tempVal, tempDir;
    private movie tempMovie;
    private HashSet<movie> storedMovieRecords, newMovieRecords;
    private HashSet<Integer> newGenres;
    private boolean isDir = false;
    private PrintWriter pw, pwGm, pwG;
    private int largestGenreId;
    private HashMap<String, String> genreFullNameMapping = new HashMap<String, String>() {{
        put("susp", "Thriller");
        put("cnr", "Crime");
        put("cnrb", "Crime");
        put("surr", "Drama");
        put("camp", "Comedy");
        put("dram", "Drama");
        put("west", "Western");
        put("myst", "Mystery");
        put("s.f.", "Sci-Fi");
        put("scfi", "Sci-Fi");
        put("scif", "Sci-Fi");
        put("advt", "Adventure");
        put("horr", "Horror");
        put("romt", "Romance");
        put("comd", "Comedy");
        put("musc", "Musical");
        put("docu", "Documentary");
        put("musical", "Musical");
        put("ducu", "Documentary");
        put("porn", "Adult");
        put("noir", "Black");
        put("biop", "Biography");
        put("tv", "Reality-TV");
        put("tvs", "TV Series");
        put("tvm", "TV Miniseries");
        put("tvmini", "TV Miniseries");
        put("actn", "Action");
        put("fant", "Fantasy");
        put("cart", "Carton");
        put("hist", "History");
        put("crim", "Crime");
        put("cond", "Comedy");
        put("biog", "Biography");
        put("bio", "Biography");
        put("biopp", "Biography");
        put("west1", "Western");
        put("disa", "Disaster");
        put("adctx", "Adventure");
        put("sxfi", "Sci-Fi");
        put("surreal", "Surrealism");
        put("surl", "Surrealism");
        put("txx", "ctxxx");
        put("dist", "Disaster");
        put("ctcxx", "ctxxx");
        put("ctxx", "ctxxx");
        put("stage", "Stage");
        put("muusc", "Musical");
        put("avant-garde", "Experimental");
        put("cnrbb", "Crime");
        put("draam", "Drama");
        put("ram", "Drama");
        put("dramn", "Drama");
        put("psych", "Psychology");
        put("psyc", "Psychology");
        put("comdx", "Comedy");
        put("homo", "Comedy");
        put("muscl", "Musical");
        put("cmr", "Crime");
        put("duco", "Documentary");
        put("fanth*", "Fantasy");
        put("rfp;", "Drama");
        put("natu", "Drama");
        put("scat", "Comedy");
        put("mystp", "Mystery");
        put("avga", "Drama");
        put("dramd", "Drama");
        put("dram>", "Drama");
        put("cult", "Comedy");
        put("ront", "Romance");
        put("romtx", "Romance");
        put("sctn", "Action");
        put("hor", "Horror");
        put("adct", "Adventure");
        put("viol", "Violence");
        put("biopx", "Biography");
        put("axtn", "Action");
        put("biob", "Biography");
        put("kinky", "Adult");
        put("sati", "Music");
        put("dicu", "Documentary");
        put("faml", "Family");
        put("undr", "Comedy");
        put("expm", "Experimental");
        put("art", "Art");
        put("allegory", "Allegory");
        put("romt.", "Romance");
        put("dram.actn", "Drama");
        put("porb", "Action");
        put("verite", "Drama");
        put("anti-dram", "Drama");
        put("act", "Action");
        put("road", "Road");
        put("sports", "Sport");
        put("weird", "Experimental");
        put("drama", "Drama");
        put("epic", "History");
        put("video", "Video");
    }};

    public XMLParser(String xmlFile) {
        fileName = xmlFile;
    }

    public void startParsing() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        storedMovieRecords = new HashSet<>();
        newMovieRecords = new HashSet<>();
        newGenres = new HashSet<>();
        try {
            Class.forName(JDBC_DRIVER);
            pw = new PrintWriter(new File("newMovies.csv"));
            pwG = new PrintWriter(new File("newGenres.csv"));
            pwGm = new PrintWriter(new File("newGenres_in_movies.csv"));

            Connection conn = DriverManager.getConnection(DB_URL,"mytestuser","mypassword");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select max(id) as id from movies");
            rs.next();
            movie.largestId = rs.getString("id");

            // store all existed movie records
            Statement st2 = conn.createStatement();
            setUpStoredRecords(st2);
            System.out.println(storedMovieRecords.size());


            // store all existed genres
            Statement st3 = conn.createStatement();
            setUpStoredGenres(st3);

            st.close();
            st2.close();
            st3.close();
//            get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(fileName, this);
            pw.close();
            pwG.close();
            pwGm.close();

            Statement st4 = conn.createStatement();
            String load_movies = "LOAD DATA LOCAL INFILE 'newMovies.csv' INTO TABLE movies " +
                    "FIELDS TERMINATED BY ';;' " +
                    "LINES TERMINATED BY '\n' " +
                    "(id, title, year, director);";
            st4.executeQuery(load_movies);
            st4.close();

            Statement st5 = conn.createStatement();
            String load_genres = "LOAD DATA LOCAL INFILE 'newGenres.csv' INTO TABLE genres " +
                    "FIELDS TERMINATED BY ';;' " +
                    "LINES TERMINATED BY '\n' " +
                    "(id, name);";
            st5.executeQuery(load_genres);
            st5.close();

            Statement st6 = conn.createStatement();
            String load_gm = "LOAD DATA LOCAL INFILE 'newGenres_in_movies.csv' INTO TABLE genres_in_movies " +
                    "FIELDS TERMINATED BY ';;' " +
                    "LINES TERMINATED BY '\n' " +
                    "(movieId, genreId);";
            st6.executeQuery(load_gm);
            st6.close();

            conn.close();

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void setUpStoredRecords(Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("select * from movies");
        while (rs.next()) {
            storedMovieRecords.add(
                    new movie(
                            rs.getString("title"),
                            rs.getString("director"),
                            Integer.parseInt(rs.getString("year")),
                            rs.getString("id")));
        }
        rs.close();
    }

    private void setUpStoredGenres(Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("select * from genres");
        while (rs.next()) {
            movie.addGenreMap(rs.getString("name"), Integer.parseInt(rs.getString("id")));
        }
        largestGenreId = movie.largestGenreId;
        System.out.println(largestGenreId);
    }


    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        isDir = false;
        if (qName.equalsIgnoreCase("dirname"))
            isDir = true;
        else if (qName.equalsIgnoreCase("film")) {
            tempMovie = new movie();
            tempMovie.setDirector(tempDir);
        }

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isDir)
            tempDir = new String(ch, start, length);
        else
            tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("film")) {
            //add it to the list
            if (! storedMovieRecords.contains(tempMovie)) {
                int originalLength = newMovieRecords.size();
                newMovieRecords.add(tempMovie);
                // no duplicate
                if (newMovieRecords.size() > originalLength) {
                    // is a valid movie
                    if (tempMovie.getYear() > 0 && !tempMovie.getTitle().isEmpty() && !tempMovie.getDirector().isEmpty() &&
                        tempMovie.getGenres().size() > 0) {
                        pw.write(tempMovie.toString() + "\n");
                        String movieId = tempMovie.getId();
                        for (String gen : tempMovie.getGenres()) {
                            int genreId = movie.genremap.get(gen);
                            pwGm.write(movieId + ";;" + genreId + "\n");
                            // is a new genre
                            if (genreId > largestGenreId && !newGenres.contains(genreId)) {
                                pwG.write(genreId + ";;" + gen + "\n");
                                newGenres.add(genreId);
                            }
                        }
                    } else System.out.println(tempMovie.toString().substring(11) +
                            ((tempMovie.getYear() <= 0) ? "Invalid year, " : "") +
                            ((tempMovie.getGenres().size() <= 0) ? "Invalid categories, " : "") +
                            ((tempMovie.getTitle().isEmpty()) ? "Invalid title, " : "") +
                            ((tempMovie.getDirector().isEmpty()) ? "Invalid director" : ""));
                }

            }

        } else if (qName.equalsIgnoreCase("t"))
            tempMovie.setTitle(tempVal);
        else if (qName.equalsIgnoreCase("year"))
            tempMovie.setYear(tempVal);
        else if (qName.equalsIgnoreCase("dirname"))
            isDir = false;
        else if (qName.equalsIgnoreCase("cat")) {
            for (String t : tempVal.split("\\s+")) {
                t = t.toLowerCase();
                tempMovie.addGenre(genreFullNameMapping.get(t));
            }
        }


    }
}
