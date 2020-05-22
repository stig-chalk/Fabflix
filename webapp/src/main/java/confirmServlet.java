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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@WebServlet(name = "confirmServlet", urlPatterns = "/api/confirm")
public class confirmServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (request.getParameter("type") == null) {
            response.setContentType("application/json");
            ArrayList<cartItem> previousItems = (ArrayList<cartItem>) session.getAttribute("previousItems");
            JsonArray ja = new JsonArray();
            float totalPrice = 0;
            User u = (User) session.getAttribute("user");
            int lastId = Integer.parseInt((String) session.getAttribute("lastId"));
            for (cartItem ci : previousItems) {
               for (int i = 0; i < ci.getQuantity(); i++) {
                   lastId += 1;
                   JsonObject jo = new JsonObject();
                   jo.addProperty("saleId", lastId);
                   jo.addProperty("title", ci.getName());
                   jo.addProperty("quantity", 1);
                   ja.add(jo);
               }

                totalPrice += ci.getPrice() * ci.getQuantity();


            }
            ja.add(totalPrice);
            session.removeAttribute("lastId");
            PrintWriter out = response.getWriter();
            out.write(ja.toString());

        }
        else {
            session.removeAttribute("previousItems");
        }
        response.setStatus(200);
    }
}