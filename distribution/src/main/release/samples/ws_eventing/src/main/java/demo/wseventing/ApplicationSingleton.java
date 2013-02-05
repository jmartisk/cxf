package demo.wseventing;

import java.util.ArrayList;
import java.util.List;

import demo.wseventing.eventapi.CatastrophicEventSinkImpl;

public class ApplicationSingleton {

    private static ApplicationSingleton instance;
    private List<CatastrophicEventSinkImpl> eventSinks = new ArrayList<CatastrophicEventSinkImpl>();

    public static ApplicationSingleton getInstance() {
        if (instance == null)
            instance = new ApplicationSingleton();
        return instance;
    }

    private ApplicationSingleton() {
    }

    public void createEventSink(String url) {
        eventSinks.add(new CatastrophicEventSinkImpl(url));
    }

    public List<CatastrophicEventSinkImpl> getEventSinks() {
        return this.eventSinks;
    }

    public CatastrophicEventSinkImpl getEventSinkByURL(String url) {
        for (CatastrophicEventSinkImpl eventSink : eventSinks) {
            if (eventSink.getShortURL().equals(url))
                return eventSink;
        }
        return null;
    }



}
