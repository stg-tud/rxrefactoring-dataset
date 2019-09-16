package pipe.controllers.arcCreator;

import pipe.models.component.arc.ArcPoint;
import pipe.models.component.Connectable;

import java.util.List;

public interface ArcActionCreator {
    public <S extends Connectable, T extends Connectable> void create(S source, T target);

    public <S extends Connectable, T extends Connectable> void create(S source, T target, List<ArcPoint> arcPoints);

    public <S extends Connectable, T extends Connectable>  boolean canCreate(S source, T target);
}
