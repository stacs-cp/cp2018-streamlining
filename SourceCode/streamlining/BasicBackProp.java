import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BasicBackProp implements BackPropPolicy, Serializable{

    final static Logger logger = Logger.getLogger(MCTS.class);

    @Override
    public void backPropagate(HashMap<Set<Integer>, TreeNode> globalMapping, double reward, TreeNode child, Set<Integer> streamlinersApplied) {
        logger.info("BACK PROPAGATION ---------");

        Set<TreeNode> visitedNodes = new HashSet<>();
        if (child != null)
            visitedNodes.add(child);

        Set<TreeNode> parents = getParents(globalMapping, streamlinersApplied);
        for(TreeNode parent : parents){
            backPropagate(parent, visitedNodes);
        }

        logger.info("   Nodes visited during back prop: ");
        for(TreeNode visitedNode : visitedNodes){
            visitedNode.addReward(reward);
            visitedNode.incrementVisitCount();
            logger.info(visitedNode + ",");
        }
        logger.info("--------");
    }

    private Set<TreeNode> getParents(HashMap<Set<Integer>, TreeNode> globalMapping, Set<Integer> appliedStreamliners){
       HashSet<TreeNode> parents = new HashSet<>();
       HashSet<Integer> streamliners = new HashSet<>(appliedStreamliners);
       for (Integer streamliner : appliedStreamliners){
            streamliners.remove(streamliner);
            if (globalMapping.containsKey(streamliners)){
                parents.add(globalMapping.get(streamliners));
            }
            streamliners.add(streamliner);
       }
       return parents;
   }

    private void backPropagate(TreeNode treeNode, Set<TreeNode> visitedNodes){
        visitedNodes.add(treeNode);
        for (TreeNode parent : treeNode.getParents()){
            backPropagate(parent, visitedNodes);
        }
    }
}
