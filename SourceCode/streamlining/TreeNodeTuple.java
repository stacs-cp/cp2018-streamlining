import java.util.Set;

public class TreeNodeTuple {
    public Set<Integer> streamlinersApplied;
    public TreeNode node;

    public TreeNodeTuple(TreeNode node, Set<Integer> streamlinersApplied) {
        this.node = node;
        this.streamlinersApplied = streamlinersApplied;
    }

}
