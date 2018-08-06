import java.util.Set;

public interface ExpansionPolicy {

    int expand(State state, Set<Integer> allowedChildren, Set<Integer> appliedStreamliners);

}
