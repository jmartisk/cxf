package demo.wseventing;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import demo.wseventing.eventapi.EarthquakeEvent;

@WebServlet(urlPatterns = "/EarthquakeEvent")
public class EarthquakeEventServlet  extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        float strength = Float.parseFloat(req.getParameter("strength"));
        EarthquakeEvent event = new EarthquakeEvent(strength, req.getParameter("location"));
        NotificatorServiceHolder.getInstance().dispatchEvent(event);
        resp.getWriter().append("<html><body>");
        resp.getWriter().append("Event ").append(event.toString()).append(" emitted successfully!");
        resp.getWriter().append("<br/><a href=\"index.jsp\">Back to main page</a>");
        resp.getWriter().append("</body></html>");
    }
}
