import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "dashboardServlet", urlPatterns = "/api/dashbord")
public class dashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    private Connection dbcon;

    // returned json array pattern: [0]=username, [1-inf]=tables
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String insertType = request.getParameter("insertType");
        if (insertType != null) {
            if (insertType.equals("star")) {
                String starName = request.getParameter("name");
                String birthYear = request.getParameter("birth-year");
                System.out.println(starName + birthYear);

                try{
                    dbcon = dataSource.getConnection();
                    //set @max_movie_id = (select concat('tt',lpad(convert(substring(max(id),3),  unsigned) + 1, 7, 0)) as num from moviedb.movies);
                    //	INSERT INTO moviedb.movies VALUES (@max_movie_id, movie_title, movie_year, movie_director);
                    Statement st1 = dbcon.createStatement();
                    Statement st2 = dbcon.createStatement();
                    //
                    st1.executeQuery("set @max_star_id = (select concat('nm',lpad(convert(substring(max(id),3),  unsigned) + 1, 7, 0)) as num from moviedb.stars)");
                    ResultSet rs = st2.executeQuery("select @max_star_id");
                    rs.next();
                    String star_id = rs.getString(1);


                    String sql = String.format("insert into stars values(?, ? , %s)", ((!birthYear.isEmpty()) ? birthYear : "null"));
                    PreparedStatement st = dbcon.prepareStatement(sql);
                    st.setString(1, star_id);
                    st.setString(2, starName);
                    System.out.println(star_id + starName  + sql);
                    st.executeUpdate();

                    rs.close();
                    st.close();
                    st1.close();
                    st2.close();

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("star_id", star_id );
                    out.write(jsonObject.toString());

                    response.setStatus(200);
                    return;

                }
                catch (SQLException e) {
                e.printStackTrace();
                }


            } else {
                String title = request.getParameter("title");
                String year = request.getParameter("year");
                String director = request.getParameter("director");
                String singleStar = request.getParameter("star");
                String singleGenre = request.getParameter("genre");

                System.out.println(title + year + director + singleStar + singleGenre);



                try {
                    //connection,
                    dbcon = dataSource.getConnection();
                    String sql = "select * from movies where title = ? and year = ? and director = ?";
                    PreparedStatement st = dbcon.prepareStatement(sql);
                    st.setString(1, title);
                    st.setInt(2, Integer.parseInt(year));
                    st.setString(3, director);
                    ResultSet rs = st.executeQuery();
                    if(rs.next()){
                        response.setStatus(500);
                        return;
                    }
                    st.close();
                    rs.close();

                    sql = "{call add_movie(?,?,?,?,?)}";

                    CallableStatement stmt=dbcon.prepareCall(sql);

                    stmt.setString(1, title);

                    stmt.setInt(2, Integer.parseInt(year));

                    stmt.setString(3, director);

                    stmt.setString(4, singleStar);

                    stmt.setString(5, singleGenre);


                    //Execute stored procedure
                    stmt.execute();

                    JsonObject jsonObject = new JsonObject();

                    //movie id
                    Statement st1 = dbcon.createStatement();
                    ResultSet rs1 = st1.executeQuery("select @movie_id");
                    rs1.next();
                    jsonObject.addProperty("movie_id", rs1.getString(1) );
                    //star id
                    Statement st2 = dbcon.createStatement();
                    ResultSet rs2 = st2.executeQuery("select @star_id");
                    rs2.next();
                    jsonObject.addProperty("star_id", rs2.getString(1) );
                    //genre id
                    Statement st3 = dbcon.createStatement();
                    ResultSet rs3 = st3.executeQuery("select @genre_id");
                    rs3.next();
                    jsonObject.addProperty("genre_id", rs3.getString(1) );

                    rs1.close();
                    rs2.close();
                    rs3.close();
                    st1.close();
                    st2.close();
                    st3.close();
                    out.write(jsonObject.toString());

                    response.setStatus(200);
                    return;

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }


        JsonArray ja = new JsonArray();

        try {
            // get user name
            getFullUserName(request, ja);

            // get table info
            getTableInfo(ja);

            out.write(ja.toString());
            response.setStatus(200);

        } catch (SQLException e) {
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

    private void getFullUserName(HttpServletRequest request, JsonArray ja) {
        JsonObject userObject = new JsonObject();
        User u = (User) request.getSession().getAttribute("user");
        String fullName = u.getFirstName() + " " + u.getLastName();
        userObject.addProperty("fullName", fullName);
        ja.add(userObject);
    }

    private void getTableInfo(JsonArray ja) throws SQLException {
        dbcon = dataSource.getConnection();
        Statement st = dbcon.createStatement();
        ResultSet rs = st.executeQuery("show tables");
        String singleTableQuery = "show columns from ";
        while (rs.next()) {
            JsonObject jo = new JsonObject();
            Statement singleTableSt = dbcon.createStatement();
            String tableName = rs.getString(1);

            ResultSet singleTableRs = singleTableSt.executeQuery(singleTableQuery + tableName);
            JsonArray attrsja = new JsonArray();
            while (singleTableRs.next()) {
                JsonObject coljo = new JsonObject();
                coljo.addProperty("field", singleTableRs.getString(1));
                coljo.addProperty("type", singleTableRs.getString(2));
                attrsja.add(coljo);
            }

            jo.addProperty("tableName", tableName);
            jo.add("columns", attrsja);

            ja.add(jo);

            singleTableSt.close();
            singleTableRs.close();
        }
        st.close();
    }
}