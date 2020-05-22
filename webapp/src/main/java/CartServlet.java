import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet{
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        System.out.println("posttttttttttt");
        String id =request.getParameter("id");
        String  name = request.getParameter("title");

        HttpSession session = request.getSession();

        Random rand = new Random();
        ArrayList<cartItem> previousItems = (ArrayList<cartItem>) session.getAttribute("previousItems");
        if (previousItems == null) {

            previousItems = new ArrayList<cartItem>();
            cartItem item = new cartItem(id, name, 1,
                    Float.parseFloat(String.format("%.2f", rand.nextFloat() * 10)));
            previousItems.add(item);
            session.setAttribute("previousItems", previousItems);
        }

        else {

            synchronized (previousItems) {
                boolean exist = false;
                for(cartItem ci: previousItems){
                    if(ci.getId().equals(id))
                    {
                        ci.setQuantity(ci.getQuantity()+1);
                        exist = true;
                        break;
                    }
                }

                if(!exist)
                {
                    cartItem item = new cartItem(id, name, 1, Float.parseFloat(String.format("%.2f", rand.nextFloat() * 10)));
                    previousItems.add(item);
                }

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
        PrintWriter out = response.getWriter();
        out.write(items.toString());


    }
}
