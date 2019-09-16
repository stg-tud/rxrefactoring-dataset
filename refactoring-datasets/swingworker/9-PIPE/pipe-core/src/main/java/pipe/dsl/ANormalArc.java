package pipe.dsl;

import pipe.models.component.Connectable;
import pipe.models.component.arc.Arc;
import pipe.models.component.arc.ArcType;
import pipe.models.component.rate.RateParameter;
import pipe.models.component.token.Token;

import java.util.HashMap;
import java.util.Map;

public class ANormalArc implements DSLCreator<Arc<? extends Connectable, ? extends Connectable>> {
    private String source;

    private String target;

    private Map<String, String> weights = new HashMap<>();


    private ANormalArc() {
    }

    public static ANormalArc withSource(String source) {
        ANormalArc aNormalArc = new ANormalArc();
        aNormalArc.source = source;
        return aNormalArc;
    }

    public ANormalArc andTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Method for creating tokens
     * E.g. with("5", "Red").tokens()
     * @param tokenWeight
     * @param tokenName
     * @return
     */
    public ANormalArc with(String tokenWeight, String tokenName) {
        weights.put(tokenName, tokenWeight);
        return this;
    }

    /**
     * Added for readbility, same as with method
     * @param tokenWeight
     * @param tokenName
     * @return
     */
    public ANormalArc and(String tokenWeight, String tokenName) {
        return with(tokenWeight, tokenName);
    }

    @Override
    public Arc<? extends Connectable, ? extends Connectable> create(Map<String, Token> tokens,
                                                                    Map<String, Connectable> connectables,
                                                                    Map<String, RateParameter> rateParameters) {
        Map<Token, String> arcWeights = new HashMap<>();
        for (Map.Entry<String, String> entry : weights.entrySet()) {
            arcWeights.put(tokens.get(entry.getKey()), entry.getValue());
        }

        return new Arc<>(connectables.get(source), connectables.get(target), arcWeights, ArcType.NORMAL);
    }

    public ANormalArc tokens() {
        return this;
    }

    public ANormalArc token() {
        return this;
    }
}
