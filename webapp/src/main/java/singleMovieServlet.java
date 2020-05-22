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
import java.util.ArrayList;
import java.util.Collections;


@WebServlet(name = "singleMovieServlet", urlPatterns = "/api/single-movie")
public class singleMovieServlet extends HttpServlet {
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

            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();

            String query = "select * from movies left join ratings on (id = movieId) where movies.id = ?";
            PreparedStatement statement = dbcon.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();
            rs.first();

            jsonObject.addProperty("movieTitle", rs.getString("title"));
            jsonObject.addProperty("movieYear", rs.getString("year"));
            jsonObject.addProperty("movieRating", rs.getString("rating"));
            jsonObject.addProperty("movieDirector", rs.getString("director"));
            jsonObject.addProperty("movieId", id);


            JsonArray movieGenres = new JsonArray();
            String genreQuery = "select genres.name as name, genres.id as id from genres_in_movies, genres " +
                                "where id = genreId and movieId = ? order by name";
            PreparedStatement genreSt = dbcon.prepareStatement(genreQuery);
            genreSt.setString(1, id);
            ResultSet genreRe = genreSt.executeQuery();
            genreRe.beforeFirst();
            while (genreRe.next()) {
                JsonObject gjs = new JsonObject();
                gjs.addProperty("id", genreRe.getString("id"));
                gjs.addProperty("name", genreRe.getString("name"));
                movieGenres.add(gjs);
            }
            jsonObject.add("movieGenres", movieGenres);



            String starQuery ="select name, id from stars_in_movies, stars where id = starId and movieId = ? ";
            PreparedStatement starSt = dbcon.prepareStatement(starQuery);
            starSt.setString(1, id);
            ResultSet starRe = starSt.executeQuery();
            JsonArray movieStars = new JsonArray();
            starRe.beforeFirst();
            ArrayList<star> stars = new ArrayList<>();

            PreparedStatement sortSt = dbcon.prepareStatement("select count(*) as num from stars_in_movies where starId = ?");

            while (starRe.next()) {
                sortSt.setString(1, starRe.getString("id"));
                ResultSet sortRs = sortSt.executeQuery();
                sortRs.next();
                int num_movies = Integer.parseInt(sortRs.getString("num"));
                stars.add(new star(starRe.getString("name"), starRe.getString("id"), num_movies));
            }

            Collections.sort(stars);

            for (star t : stars) {
                JsonObject tjo = new JsonObject();
                tjo.addProperty("name", t.getName());
                tjo.addProperty("id", t.getId());
                movieStars.add(tjo);
            }

            jsonObject.add("movieStars", movieStars);

            jsonArray.add(jsonObject);
            out.write(jsonArray.toString());
            response.setStatus(200);

            statement.close();
            dbcon.close();
            rs.close();
            genreRe.close();
            starRe.close();
        }
        catch (Exception e) {
        // write error message JSON object to output
            e.printStackTrace();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("errorMessage", e.getMessage());
        out.write(jsonObject.toString());

        // set reponse status to 500 (Internal Server Error)
        response.setStatus(500);
        }
		out.close();

    }
}
