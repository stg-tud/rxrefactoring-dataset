package pipe.models.petrinet.name;

public interface PetriNetName {
    public String getName();
    public void visit(NameVisitor nameVisitor);
}
