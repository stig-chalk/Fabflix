import com.google.gson.JsonObject;
import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "loginServlet", urlPatterns = "/api/login")
public class loginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String client = request.getParameter("client") != null ? request.getParameter("client") : "web";

        if (client.equals("web")) {
            try {
                String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch (Exception e) {
                response.setStatus(200);
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Failed to pass reCaptcha test");
                out.write(responseJsonObject.toString());
                return;
            }
        }


        String email = request.getParameter("username");
        String password = request.getParameter("password");
        String login_type = request.getParameter("login-type");

        JsonObject responseJsonObject = new JsonObject();

        PreparedStatement preSt = null;
        String query = String.format("select * from %s where email= ?", login_type);

        try {
            Connection dbcon = dataSource.getConnection();
            preSt = dbcon.prepareStatement(query);
            preSt.setString(1, email);

            ResultSet rs = preSt.executeQuery();
            boolean userExist = rs.next();
            // check whether the email/password combination is correct
            if (userExist &&
                login_type.equals("customers") &&
                new StrongPasswordEncryptor().checkPassword(password, rs.getString("password"))) {

                int id = Integer.parseInt(rs.getString("id"));
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                request.getSession().setAttribute("user", new User(email, firstName, lastName, id));
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                responseJsonObject.addProperty("directTo", login_type);
            }
            else if (userExist && login_type.equals("employees")) {
                String fullname = rs.getString("fullname");
                request.getSession().setAttribute("user", new User(email, fullname, "", -1));
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                responseJsonObject.addProperty("directTo", login_type);
            }
            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid email/password combination");
            }

            out.write(responseJsonObject.toString());
            response.setStatus(200);
            rs.close();
            preSt.close();
            dbcon.close();

        } catch (SQLException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }
    }
}