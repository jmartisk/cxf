package demo.wseventing;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import demo.wseventing.eventapi.EarthquakeEvent;
import demo.wseventing.eventapi.FireEvent;

@WebServlet(urlPatterns = "/FireEvent")
public class FireEventServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        FireEvent event = new FireEvent(req.getParameter("location"), Integer.parseInt(
                req.getParameter("strength")));
        NotificatorServiceHolder.getInstance().dispatchEvent(event);
        resp.getWriter().append("<html><body>");
        resp.getWriter().append("Event ").append(event.toString()).append(" emitted successfully!");
        resp.getWriter().append("<br/><a href=\"index.jsp\">Back to main page</a>");
        resp.getWriter().append("</body></html>");
    }
}
