package pipe.models;


import pipe.models.petrinet.PetriNet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class just holds a petri net and forms the base level of PNML
 */
@XmlRootElement(name = "pnml")
public class PetriNetHolder {
    @XmlElement(name = "net")
    private final List<PetriNet> nets = new ArrayList<PetriNet>();

    public void addNet(PetriNet net) {
        nets.add(net);
    }

    public PetriNet getNet(int index) {
        return nets.get(index);
    }
}
