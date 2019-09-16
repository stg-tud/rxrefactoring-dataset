package pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;


@XmlAccessorType(XmlAccessType.FIELD)
public class AdaptedConnectable {

    @XmlElement
    private PositionGraphics graphics;

    @XmlAttribute
    private String id;

    @XmlElement(name = "name")
    private NameDetails name = new NameDetails();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PositionGraphics getGraphics() {
        return graphics;
    }

    public void setGraphics(PositionGraphics graphics) {
        this.graphics = graphics;
    }

    public NameDetails getName() {
        return name;
    }

    public void setNameDetails(NameDetails name) {
        this.name = name;
    }


}
