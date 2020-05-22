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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


@WebServlet(name = "singleStarServlet", urlPatterns = "/api/single-star")
public class singleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json"); // Response mime type

        String id = request.getParameter("id");
        PrintWriter out = response.getWriter();

        try {
            Connection dbcon = dataSource.getConnection();
            PreparedStatement statement;

            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();

            String query = "select * from stars where stars.id = ?";
            statement = dbcon.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();
            rs.first();

            jsonObject.addProperty("starId", rs.getString("id"));
            jsonObject.addProperty("starName", rs.getString("name"));
            if(rs.getString("birthYear") != null)
                jsonObject.addProperty("starYear", rs.getString("birthYear"));
            else
                jsonObject.addProperty("starYear", "N/A");

            String movieQuery = "select title, id from stars_in_movies, movies where id = movieId and starId = ? " +
                                "order by -year, title";
            PreparedStatement movieSt = dbcon.prepareStatement(movieQuery);
            movieSt.setString(1, id);
            ResultSet movieRe = movieSt.executeQuery();
            JsonArray starMovies = new JsonArray();
            movieRe.beforeFirst();
            while (movieRe.next()) {
                JsonObject name_id = new JsonObject();
                name_id.addProperty("name", movieRe.getString("title"));
                name_id.addProperty("id", movieRe.getString("id"));
                starMovies.add(name_id);
            }

            jsonObject.add("starMovies", starMovies);

            jsonArray.add(jsonObject);
            out.write(jsonArray.toString());
            response.setStatus(200);

            statement.close();
            dbcon.close();
            rs.close();
            movieRe.close();
        }
        catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();

    }
}
