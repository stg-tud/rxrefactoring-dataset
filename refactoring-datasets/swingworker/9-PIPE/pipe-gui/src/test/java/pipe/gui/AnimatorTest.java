package pipe.gui;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import pipe.historyActions.AnimationHistory;
import pipe.models.component.place.Place;
import pipe.models.component.token.Token;
import pipe.models.component.transition.Transition;
import pipe.models.petrinet.PetriNet;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.mockito.Mockito.*;

public class AnimatorTest {

    private Animator animator;

    private AnimationHistory mockHistory;

    private PetriNet mockPetriNet;

    @Before
    public void setUp() {
        mockHistory = mock(AnimationHistory.class);
        mockPetriNet = mock(PetriNet.class);
        animator = new Animator(mockPetriNet, mockHistory);
    }

    @Test
    public void firingAddsToHistoryAndFires() {
        Transition transition = mock(Transition.class);
        animator.fireTransition(transition);

        InOrder inOrder = inOrder(mockHistory);
        inOrder.verify(mockHistory, times(1)).clearStepsForward();
        inOrder.verify(mockHistory, times(1)).addHistoryItem(transition);
        verify(mockPetriNet).fireTransition(transition);
    }

    @Test
    public void ifStepForwardAnimatesTransition() {
        when(mockHistory.isStepForwardAllowed()).thenReturn(true);
        when(mockHistory.getCurrentPosition()).thenReturn(1);
        Transition transition = mock(Transition.class);
        when(mockHistory.getTransition(2)).thenReturn(transition);

        animator.stepForward();
        verify(mockPetriNet).fireTransition(transition);
        verify(mockHistory).stepForward();
    }

    @Test
    public void ifCannotStepForwardDoesNotAnimateTransition() {
        when(mockHistory.isStepForwardAllowed()).thenReturn(false);
        when(mockHistory.getCurrentPosition()).thenReturn(1);
        Transition transition = mock(Transition.class);
        when(mockHistory.getTransition(2)).thenReturn(transition);

        animator.stepForward();
        verify(mockPetriNet, never()).fireTransition(transition);
        verify(mockHistory, never()).stepForward();
    }

    @Test
    public void ifStepBackwardAnimatesTransition() {
        when(mockHistory.isStepBackAllowed()).thenReturn(true);
        Transition transition = mock(Transition.class);
        when(mockHistory.getCurrentTransition()).thenReturn(transition);

        animator.stepBack();
        verify(mockPetriNet).fireTransitionBackwards(transition);
        verify(mockHistory).stepBackwards();
    }

    @Test
    public void ifCannotStepBackwardDoesNotAnimateTransition() {
        when(mockHistory.isStepBackAllowed()).thenReturn(true);
        Transition transition = mock(Transition.class);
        when(mockHistory.getCurrentTransition()).thenReturn(transition);

        animator.stepForward();
        verify(mockPetriNet, never()).fireTransitionBackwards(transition);
        verify(mockHistory, never()).stepBackwards();
    }

    @Test
    public void doRandomFiringClearsForwardsThenAddsToHistory() {
        Transition transition = mock(Transition.class);
        when(mockPetriNet.getRandomTransition()).thenReturn(transition);
        animator.doRandomFiring();
        InOrder inOrder = inOrder(mockHistory);
        inOrder.verify(mockHistory, times(1)).clearStepsForward();
        inOrder.verify(mockHistory, times(1)).addHistoryItem(transition);
    }

    @Test
    public void doRandomFiringFiresPetriNet() {
        Transition transition = mock(Transition.class);
        when(mockPetriNet.getRandomTransition()).thenReturn(transition);
        animator.doRandomFiring();
        verify(mockPetriNet).fireTransition(transition);
    }

    @Test
    public void restoresOriginalTokensWhenFinished() {
        Place mockPlace = mock(Place.class);
        Collection<Place> places = new LinkedList<Place>();
        places.add(mockPlace);

        Token token = mock(Token.class);
        Map<Token, Integer> tokens = new HashMap<Token, Integer>();
        tokens.put(token, 5);

        when(mockPlace.getTokenCounts()).thenReturn(tokens);
        when(mockPetriNet.getPlaces()).thenReturn(places);

        animator.startAnimation();
        animator.finish();

        verify(mockPlace).setTokenCounts(tokens);
    }
}
