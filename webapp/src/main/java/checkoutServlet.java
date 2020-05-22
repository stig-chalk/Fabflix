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
import java.util.Date;
import java.util.Random;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
@WebServlet(name = "checkoutServlet", urlPatterns = "/api/checkout")
public class checkoutServlet extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        ArrayList<cartItem> previousItems = (ArrayList<cartItem>) session.getAttribute("previousItems");

        if (request.getParameter("quantity") != null) {
            String id = request.getParameter("id");
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            for (int i = 0; i < previousItems.size(); i++) {
                cartItem ci = previousItems.get(i);
                if (ci.getId().equals(id)) {
                    if (quantity == 0) {
                        previousItems.remove(i);
                    } else {
                        ci.setQuantity(quantity);
                        previousItems.set(i, ci);
                    }
                    session.setAttribute("previousItems", previousItems);
                    System.out.println(ci.getId() + " " + ci.getQuantity());
                    break;
                }
            }
        } else {

            Random rand = new Random();

            if (previousItems == null) {
                previousItems = new ArrayList<cartItem>();
                session.setAttribute("previousItems", previousItems);
            }
        }
        JsonArray items = new JsonArray();
        for(cartItem ci: previousItems){
            JsonObject item = new JsonObject();
            item.addProperty("id", ci.getId());
            item.addProperty("name", ci.getName());
            item.addProperty("quantity", ci.getQuantity());
            item.addProperty("price", ci.getPrice());
            items.add(item);}
        System.out.println(items);
        PrintWriter out = response.getWriter();
        out.write(items.toString());
    }


    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
    private Connection dbcon;


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String creditId = request.getParameter("credit-card");
        String exp =  request.getParameter("exp-date");
        System.out.println(fname+lname+creditId+exp);

        HttpSession session = request.getSession();

        //check user info
        try {
            dbcon = dataSource.getConnection();

            String query = "select * from creditcards where id = ? and firstName = ? and lastName = ? " +
                           "and expiration = ?";

            PreparedStatement st = dbcon.prepareStatement(query);
            st.setString(1, creditId);
            st.setString(2, fname);
            st.setString(3, lname);
            st.setString(4, exp);

            ResultSet rs = st.executeQuery();
            if (!rs.next()) {
                response.setStatus(500);
                return;
            }

            Statement tst = dbcon.createStatement();
            ResultSet trs = tst.executeQuery("select * from sales order by -id limit 1");
            trs.next();
            session.setAttribute("lastId", trs.getString("id"));

            ArrayList<cartItem> previousItems = (ArrayList<cartItem>) session.getAttribute("previousItems");
            User u = (User) session.getAttribute("user");
            int uid = u.getId();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();

            String tquery = "insert into sales (customerId, movieId, saleDate) values (?, ?, ?)";
            PreparedStatement insertSt = dbcon.prepareStatement(tquery);

            for (cartItem ci : previousItems) {
                for (int t =0; t<ci.getQuantity(); t++) {
                    insertSt.setInt(1, uid);
                    insertSt.setString(2, ci.getId());
                    insertSt.setString(3, dtf.format(now));
                    insertSt.executeUpdate();
                }
            }

//            PrintWriter out = response.getWriter();

            response.setStatus(200);
        }
        catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
        }


    }
}
