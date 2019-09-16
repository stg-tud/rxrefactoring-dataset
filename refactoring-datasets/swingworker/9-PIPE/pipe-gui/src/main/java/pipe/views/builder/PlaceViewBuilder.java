package pipe.views.builder;

import pipe.controllers.PetriNetController;
import pipe.models.component.place.Place;
import pipe.views.MarkingView;
import pipe.views.PlaceView;

import java.util.LinkedList;

public class PlaceViewBuilder {
    private final Place place;
    private final PetriNetController controller;

    public PlaceViewBuilder(Place place, PetriNetController controller) {
        this.place = place;
        this.controller = controller;
    }

    public PlaceView build() {
        return new PlaceView(place, controller);
    }

}
