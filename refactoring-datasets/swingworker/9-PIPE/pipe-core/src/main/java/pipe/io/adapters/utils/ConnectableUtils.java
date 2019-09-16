package pipe.io.adapters.utils;

import pipe.io.adapters.model.*;
import pipe.models.component.Connectable;

public final class ConnectableUtils {

    /**
     * Hidden constructor for utility class since this class
     * is not designed to be instantiated
     */
    private ConnectableUtils() {}

    /**
     * Sets adaptedConnectable name based on the connectable name
     *
     * @param connectable
     * @param adaptedConnectable
     */
    public static void setAdaptedName(Connectable connectable, AdaptedConnectable adaptedConnectable) {
        NameDetails details = new NameDetails();
        details.setName(connectable.getName());

        OffsetGraphics graphics = new OffsetGraphics();
        graphics.point = new Point();
        graphics.point.setX(connectable.getNameXOffset());
        graphics.point.setY(connectable.getNameYOffset());
        details.setGraphics(graphics);

        adaptedConnectable.setNameDetails(details);
    }

    public static void setPosition(Connectable connectable, AdaptedConnectable adaptedConnectable) {
        PositionGraphics positionGraphics = new PositionGraphics();
        positionGraphics.point = new Point();
        positionGraphics.point.setX(connectable.getX());
        positionGraphics.point.setY(connectable.getY());

        adaptedConnectable.setGraphics(positionGraphics);
    }

    /**
     * Sets the connectables name offset based on the adapted connectable
     *
     * @param connectable
     * @param adaptedConnectable
     */
    public static void setConntactableNameOffset(Connectable connectable, AdaptedConnectable adaptedConnectable) {
        NameDetails nameDetails = adaptedConnectable.getName();
        OffsetGraphics offsetGraphics = nameDetails.getGraphics();
        if (offsetGraphics.point != null) {
            connectable.setNameXOffset(offsetGraphics.point.getX());
            connectable.setNameYOffset(offsetGraphics.point.getY());
        }
    }

    /**
     * Sets the connectables position based on the adapted connectable
     *
     * @param connectable
     * @param adaptedConnectable
     */
    public static void setConnectablePosition(Connectable connectable, AdaptedConnectable adaptedConnectable) {
        connectable.setX(adaptedConnectable.getGraphics().point.getX());
        connectable.setY(adaptedConnectable.getGraphics().point.getY());
    }
}
