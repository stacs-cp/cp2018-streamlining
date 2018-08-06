import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class RandomExpansion implements ExpansionPolicy, Serializable{

    Random random = new Random();

    @Override
    public int expand(State state, Set<Integer> allowedChildren, Set<Integer> appliedStreamliners) {
        ArrayList<Integer> possibleChildren = new ArrayList<>(allowedChildren);
        return possibleChildren.get(random.nextInt(possibleChildren.size()));
    }
}
