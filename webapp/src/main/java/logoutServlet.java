import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "logoutServlet", urlPatterns = "/api/logout")
public class logoutServlet extends HttpServlet {
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.getSession().removeAttribute("user");
            request.getSession().invalidate();
            response.sendRedirect(request.getContextPath() + "/login.html");
            response.setStatus(200);
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}
