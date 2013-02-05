package demo.wseventing.eventapi;

import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;
import javax.xml.ws.soap.Addressing;

@WebService
@Addressing(enabled = true, required = true)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface CatastrophicEventSink {

    @Action(input = "http://www.earthquake.com")
    @Oneway
    void earthquake(@WebParam(name = "earthquake") EarthquakeEvent ev);

    @Action(input = "http://www.fire.com")
    @Oneway
    void fire(@WebParam(name = "fire") FireEvent ev);

}
