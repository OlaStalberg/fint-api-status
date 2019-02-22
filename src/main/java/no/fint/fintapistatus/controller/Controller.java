package no.fint.fintapistatus.controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api")
public class Controller {
    @Autowired
    private PeopleService peopleService;
    @Autowired
    private HealthService healthService;
    private static final String APLICATION_HEALTHY = "APPLICATION_HEALTHY";
    //Finnes det noen ConcurrentList? Eller er det greit siden add er O(1)?
    static ConcurrentHashMap<String,LinkedList<Event>> healthStatusLogg= new ConcurrentHashMap<>();
    static ConcurrentHashMap<String,Event> lastHealthyStatus = new ConcurrentHashMap<>();
    @GetMapping(value = "/people")
    public List<People> getPeople() {
        return peopleService.getAllPeople();
    }

    @GetMapping(value = "/test/{var}")
    public String getVar(@PathVariable final String var) {
        return var;
    }

    @GetMapping(value = "/checkStatus")
    public String checkStatus(){
        StringBuilder completeStatus = new StringBuilder("Dette er den nåværende statusen fra healthStatusLogg i Controller:\n\n");
        Set<String> mainDomains = healthStatusLogg.keySet();
        for (String mainDomain : mainDomains){
            Event lastLogg = healthStatusLogg.get(mainDomain).getLast();
            completeStatus.append("For server: " + lastLogg.getSource() + " følgende resultat: <br>\n");
            completeStatus.append(lastLogg.getData().toString());
        }
        return completeStatus.toString();
    }

    @GetMapping(value = "/healthcheck/all")
    public String getHealthCheckStatusAll() {
        TreeMap<String, LinkedList<String>> domenekart = new TreeMap<>();
        LinkedList<String> domene1Underdomener = new LinkedList<>();
        LinkedList<String> domene2Underdomener = new LinkedList<>();
        //Flytte innholdet i listene til Config-fil
        final String domene1 = "administrasjon";
        final String domene2 = "utdanning";
        domene1Underdomener.add("personal");
        domene1Underdomener.add("organisasjon");
        domene1Underdomener.add("kodeverk");
        domenekart.put(domene1, domene1Underdomener);
        domene2Underdomener.add("elev");
        domene2Underdomener.add("utdanningsprogram");
        domene2Underdomener.add("vurdering");
        domene2Underdomener.add("timeplan");
        domene2Underdomener.add("kodeverk");
        domenekart.put(domene2, domene2Underdomener);
        healthService.healthCheckAll(domenekart);
        return "suksess!";
    }
    @GetMapping(value = "/healthcheckwithdomene/{domene}/{underdomene}")
    public String getHealthCheckStatusByDomene(
            @PathVariable("domene") final String domene,
            @PathVariable("underdomene") final String underdomene) {
        healthService.healthCheck(domene, underdomene);
        return "yes! Ferdig med getHealthCheckStatusByDomene";
    }

    static boolean containsHealthyStatus(Event event) {
        if (event != null)
            if (event.getData()!=null){
                for (int i = 0; i <event.getData().size() ; i++) {
                    if (event.getData().get(i).toString().contains(APLICATION_HEALTHY))
                        return true;
                }
            }
        return false;

    }
}
