import com.google.gson.JsonArray;
import com.google.gson.JsonObject;



import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;


@WebServlet(name = "moviesServlet", urlPatterns = "/api/movies")
public class movieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    private Connection dbcon;


    // returned json array pattern: [0]=username, [1]=allGenres, [2-inf]=movies
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            dbcon = dataSource.getConnection();

            // if the request type is auto-complete
            if (request.getParameter("autocp") != null) {
                String title = request.getParameter("autocp");
                JsonArray jsa = new JsonArray();
                findMatches(title, jsa);
                out.write(jsa.toString());
                response.setStatus(200);
                out.close();
                return;
            }
            // get keyword if any
            Keyword prevKey = (Keyword) request.getSession().getAttribute("keyword");
            Keyword keyword = getKeyword(request);


            if (prevKey == null || (! keyword.toString().equals(prevKey.toString()))) {
                String totalNum = getTotalNum(keyword);
                request.getSession().setAttribute("totalNum", totalNum);
            }


            JsonArray jsonArray = new JsonArray();

            // username
            getFullUserName(request, jsonArray);

            // allGenres
            jsonArray.add(getAllGenres(request));

            // totalNum
            JsonObject totalNumJs = new JsonObject();
            totalNumJs.addProperty("totalNum", (String) request.getSession().getAttribute("totalNum"));
            jsonArray.add(totalNumJs);

            // start searching movies
            PreparedStatement statement = getPreStatement(keyword);

            // execute the query and store all info in to the jsonArray
            extractAll(statement, jsonArray);

            // write JSON string to output
            out.write(jsonArray.toString());

            // set response status to 200 (OK)
            response.setStatus(200);

            statement.close();
            dbcon.close();
        } catch (Exception e) {
            e.printStackTrace();
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }


    private JsonArray getAllGenres(HttpServletRequest request) throws SQLException {
        JsonArray ja = (JsonArray) request.getSession().getAttribute("allGenres");
        if (ja != null) {
            return ja;
        }
        ja = new JsonArray();
        Statement st = dbcon.createStatement();
        ResultSet rs = st.executeQuery("select * from genres");
        while(rs.next()) {
            JsonObject jo = new JsonObject();
            jo.addProperty("genreId", rs.getString("id"));
            jo.addProperty("genreName", rs.getString("name"));
            ja.add(jo);
        }
        request.getSession().setAttribute("allGenres", ja);
        return ja;
    }



    private Keyword getKeyword(HttpServletRequest request) {
        Keyword keyword;
        String orderBy = request.getParameter("orderBy");
        String limit = request.getParameter("limit");
        String ascOptR = request.getParameter("ascOptR");
        String ascOptT = request.getParameter("ascOptT");
        String offset = request.getParameter("offset");

        if (request.getParameter("browseBy") != null) {
            String browseBy = request.getParameter("browseBy");
            String browseKey = request.getParameter("browseKey");
            keyword = new Keyword(browseBy, browseKey, orderBy, limit, ascOptR, ascOptT, offset);
        } else {
            String title = (request.getParameter("title") != null) ? request.getParameter("title") : "";
            String actor = (request.getParameter("actor") != null) ? request.getParameter("actor") : "";
            String director = (request.getParameter("director") != null) ? request.getParameter("director") : "";
            String year = ((request.getParameter("year") != null) ? request.getParameter("year") : "");
            keyword = new Keyword(title, actor, director, year, orderBy, limit, ascOptR, ascOptT, offset);
        }

        System.out.println("this is the key: " + keyword + "\nempty: " + keyword.isKwEmpty());

        if (keyword.isKwEmpty()) {
            Object prevKw = request.getSession().getAttribute("keyword");
            if (prevKw != null) {
                keyword = (Keyword) prevKw;
                keyword.setLimit(limit);
                keyword.setOrderBy(orderBy);
                keyword.setAscOptR(ascOptR);
                keyword.setAscOptT(ascOptT);
                keyword.setOffset(offset);
            }
        }
        request.getSession().setAttribute("keyword", keyword);

        return keyword;
    }


    private PreparedStatement getPreStatement(Keyword keyword) throws SQLException {
        String query;
        PreparedStatement st;

        String orderBy = keyword.getOrderBy();
        int limit = Integer.parseInt(keyword.getLimit());
        int offset = (Integer.parseInt(keyword.getOffset()) - 1) * limit;
        String secondaryOder;

        if (orderBy.equals("rating")) {
            orderBy += " " + keyword.getAscOptR();
            secondaryOder = "title " + keyword.getAscOptT();
        } else {
            orderBy += " " + keyword.getAscOptT();
            secondaryOder = "rating " + keyword.getAscOptR();
        }

        if (keyword.getkeyType().equals("search")) {
            int year = keyword.getYear();
            String title = keyword.getTitle();
            if (!title.isEmpty()) {
                String[] titles = title.split("\\s+");
                title = "and match(title) against('";
                for (String t : titles) {
                    title += "+" + t + "* ";
                }
                title += "' in boolean mode) ";
            }

            String director = "%" + keyword.getDirector()  + "%";
            String star = "%" + keyword.getActor() + "%";

            query = String.format(
                    "select distinct m.id as mid, rating, director, year, title " +
                    "from movies as m left join ratings as r on (r.movieId = m.id) cross join stars as s cross join stars_in_movies as sm " +
                    "where m.id = sm.movieId and s.id = sm.starId " +
                    "%s and director like ? and s.name like ? ",
                    title);

            if (year >= 0) {
                query += "and year = ? ";
            }

            query += String.format("order by %s, %s limit ? offset ? ", orderBy, secondaryOder);

            st = dbcon.prepareStatement(query);
            st.setString(1, director);
            st.setString(2, star);
            int n = 3;
            if (year >= 0) {
                st.setInt(3, year);
                n += 1;
            }

            st.setInt(n, limit);
            st.setInt(n+1, offset);

        } else {
            String browseBy = keyword.getBrowseBy();
            String browseKey = keyword.getBrowseKey();
            if (browseBy.equals("genre")) {
                query = String.format("select distinct m.id as mid, rating, director, year, title " +
                        "from movies as m left join ratings as r on (m.id = r.movieId) cross join genres_in_movies as gm " +
                        "where m.id = gm.movieId " +
                        "and gm.genreId = ? order by %s, %s limit ? offset ?", orderBy, secondaryOder);

                st = dbcon.prepareStatement(query);
                st.setInt(1, Integer.parseInt(browseKey));
                st.setInt(2, limit);
                st.setInt(3, offset);

            } else {
                query = "select distinct m.id as mid, rating, director, year, title " +
                        "from movies as m left join ratings as r on (r.movieId = m.id) where ";
                if (browseKey.equals("*")) {
                    query += "title REGEXP '^[^a-z0-9]' ";
                } else {
                    query += "title like ? ";
                }
                query += String.format("order by %s, %s limit ? offset ?", orderBy, secondaryOder);

                st = dbcon.prepareStatement(query);
                int n = 1;
                if (!browseKey.equals("*")) {
                    st.setString(1, browseKey + "%");
                    n += 1;
                }
                st.setInt(n, limit);
                st.setInt(n+1, offset);
            }
        }
        return st;
    }


    public String getTotalNum(Keyword keyword) throws SQLException {
        String query;
        PreparedStatement st;
        if (keyword.getkeyType().equals("search")) {
            int year = keyword.getYear();
            String title = keyword.getTitle();
            if (!title.isEmpty()) {
                String[] titles = title.split("\\s+");
                title = "and match(title) against('";
                for (String t : titles) {
                    title += "+" + t + "* ";
                }
                title += "' in boolean mode) ";
            }
            String director = "%" + keyword.getDirector()  + "%";
            String star = "%" + keyword.getActor() + "%";

            query = String.format(
                    "select count(distinct m.id) as num " +
                    "from movies as m, stars as s, stars_in_movies as sm " +
                    "where m.id = sm.movieId and s.id = sm.starId " +
                    "%s and director like ? and s.name like ? ",
                    title);
            if (year >= 0) {
                query += "and year= ? ";
            }

            st = dbcon.prepareStatement(query);
            st.setString(1, director);
            st.setString(2, star);

            if (year >= 0) {
                st.setInt(3, year);
            }

        } else {
            String browseBy = keyword.getBrowseBy();
            String browseKey = keyword.getBrowseKey();
            if (browseBy.equals("genre")) {
                query = "select count(distinct movieId) as num " +
                        "from genres_in_movies where genreId = ? ";
                st = dbcon.prepareStatement(query);
                st.setString(1, browseKey);

            } else {
                query = "select count(distinct id) as num " +
                        "from movies where ";
                if (browseKey.equals("*")) {
                    query += "title REGEXP '^[^a-z0-9]' ";
                } else {
                    query += "title like ?";
                }

                st = dbcon.prepareStatement(query);
                if (!browseKey.equals("*")) {
                    st.setString(1, browseKey + "%");
                }
            }
        }

        ResultSet rs = st.executeQuery();
        String num = "0";
        if (rs.next())
            num = rs.getString("num");
        rs.close();
        st.close();
        System.out.println(num);
        return num;
    }



    private void extractAll(PreparedStatement statement, JsonArray jsonArray) throws SQLException {
        PreparedStatement preStarSt, preGenreSt;

        String starQuery = "select name, id from stars_in_movies, stars " +
                           "where id = starId and movieId = ? order by name limit 3";
        String genreQuery = "select name, id from genres_in_movies, genres " +
                            "where id = genreId and movieId = ? order by name limit 3";

        preStarSt = dbcon.prepareStatement(starQuery);
        preGenreSt = dbcon.prepareStatement(genreQuery);

        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            String mid = rs.getString("mid");
            if (mid != null) {
                String movieTitle = rs.getString("title");
                int movieYear = rs.getInt("year");
                String movieDirector = rs.getString("director");
                float movieRating = rs.getFloat("rating");

                // stars filter
                preStarSt.setString(1, mid);
                ResultSet stars = preStarSt.executeQuery();
                JsonArray movieStars = new JsonArray();
                while (stars.next()) {
                    JsonObject star_name_id = new JsonObject();
                    star_name_id.addProperty("name", stars.getString("name"));
                    star_name_id.addProperty("id", stars.getString("id"));
                    movieStars.add(star_name_id);
                }

                // genre
                preGenreSt.setString(1, mid);
                ResultSet genresRs = preGenreSt.executeQuery();
                JsonArray genres = new JsonArray();
                while (genresRs.next()) {
                    JsonObject genre_movie_id = new JsonObject();
                    genre_movie_id.addProperty("genreName", genresRs.getString("name"));
                    genre_movie_id.addProperty("genreId", genresRs.getString("id"));
                    genres.add(genre_movie_id);
                }

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieTitle", movieTitle);
                jsonObject.addProperty("movieYear", movieYear);
                jsonObject.addProperty("movieDirector", movieDirector);
                jsonObject.addProperty("movieRating", movieRating);
                jsonObject.addProperty("movieId", mid);
                jsonObject.add("movieStars", movieStars);
                jsonObject.add("movieGenres", genres);

                jsonArray.add(jsonObject);

                genresRs.close();
                stars.close();
            }
        }
        preGenreSt.close();
        preStarSt.close();
        rs.close();
    }


    private void getFullUserName(HttpServletRequest request, JsonArray ja) {
        JsonObject userObject = new JsonObject();
        User u = (User) request.getSession().getAttribute("user");
        String fullName = u.getFirstName() + " " + u.getLastName();
        userObject.addProperty("fullName", fullName);
        ja.add(userObject);
    }

    private void findMatches(String title, JsonArray ja) throws SQLException {
        String[] titles = title.split("\\s+");
        title = "against('";
        for (String t : titles) {
            title += "+" + t + "* ";
        }
        title += "' in boolean mode) ";

        Statement st = dbcon.createStatement();
        ResultSet rs = st.executeQuery(String.format(
                "select title, id from movies where match(title) %s limit 10",
                title));
        while(rs.next()) {
            JsonObject jo = new JsonObject();
            JsonObject innerJo = new JsonObject();
            jo.addProperty("value", rs.getString("title"));
            innerJo.addProperty("movieId", rs.getString("id"));
            jo.add("data", innerJo);
            ja.add(jo);
        }
        rs.close();
        st.close();
    }
}

