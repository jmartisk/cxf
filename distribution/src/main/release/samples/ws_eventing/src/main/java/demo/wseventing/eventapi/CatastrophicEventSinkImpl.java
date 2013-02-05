package demo.wseventing.eventapi;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

public class CatastrophicEventSinkImpl implements CatastrophicEventSink {

    private String url;

    private List<Object> receivedEvents = new ArrayList<Object>();

    public CatastrophicEventSinkImpl(String url) {
        JaxWsServerFactoryBean bean = new JaxWsServerFactoryBean();
        bean.setServiceBean(this);
        bean.setAddress(url);
        this.url = url;
        server = bean.create();
    }

    @Override
    public void earthquake(EarthquakeEvent ev) {
        System.out.println("Event sink received an earthquake notification: " + ev.toString());
        receivedEvents.add(ev);
    }

    @Override
    public void fire(FireEvent ev) {
        System.out.println("Event sink received an fire notification: " + ev.toString());
        receivedEvents.add(ev);
    }

    private Server server;

    public void stop() {
        server.stop();
    }

    public boolean isRunning() {
        return server.isStarted();
    }

    public String getFullURL() {
        return "/services/"+url;
    }

    public String getShortURL() {
        return url;
    }

    public List<Object> getReceivedEvents() {
        return receivedEvents;
    }

}
