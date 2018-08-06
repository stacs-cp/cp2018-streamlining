import java.util.HashMap;
import java.util.Set;

public interface BackPropPolicy {


    public void backPropagate(HashMap<Set<Integer>, TreeNode> globalMapping, double reward, TreeNode child, Set<Integer> streamlinersApplied);


}
