import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "loginFilter", urlPatterns = "/*")
public class loginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        allowedURIs.add("/login.html");
        allowedURIs.add("/login.js");
        allowedURIs.add("/api/login");
        allowedURIs.add("/login.css");
        allowedURIs.add("/usericon.png");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String URI = httpRequest.getRequestURI();
        System.out.println("LoginFilter: " + URI);

        if (this.isURLAllowedWithoutLogin(httpRequest.getContextPath(), URI)) {
            chain.doFilter(request, response);
            return;
        }

        if (httpRequest.getSession().getAttribute("user") == null) {
            System.out.println("filtered!!!!!!!!!!!!!!!!!!!!!!");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isURLAllowedWithoutLogin(String path, String URI) {
        for (String i : allowedURIs) {
            if ((path + i).equals(URI))
                return true;
        }
        return false;
    }

    @Override
    public void destroy() {

    }
}
