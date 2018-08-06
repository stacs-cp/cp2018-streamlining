import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.Serializable;
import java.util.*;

public class TreeNode implements Serializable{

    protected HashMap<Integer, TreeNode> childNodes;
    private Set<TreeNode> parents;
    double rewardValue;
    int numberOfVisits;

    /**
     * Constructor for the root node
     * @param possibleStreamliners
     * @param streamlinersApplied
     * @param globalMapping
     */
    public TreeNode(Set<Integer> possibleStreamliners, Set<Integer> streamlinersApplied,
                    HashMap<Set<Integer>, TreeNode> globalMapping){
        initializeChildren(possibleStreamliners, streamlinersApplied, globalMapping);
        this.parents = new HashSet<>();
    }


    /**
     * This method populates the child nodes and possible node Lists. It does so by first checking if a node already exists
     * in the global mapping for that streamliner combination and if so that TreeNode is added as an immediate child.
     *
     * If the Streamliner combination is not a disallowed combination then the combination is added as a possible child.
     * @param allowedChildren Valid child streamliners based upon current path and disallowed combinations
     * @param streamlinersAppliedOnCurrentPath
     * @param globalMapping
     */
    protected void initializeChildren(Set<Integer> allowedChildren, Set<Integer> streamlinersAppliedOnCurrentPath,
                                      HashMap<Set<Integer>, TreeNode> globalMapping){
        childNodes = new HashMap<>();
        for(Integer possibleStreamliner: allowedChildren){
            streamlinersAppliedOnCurrentPath.add(possibleStreamliner);

            if (globalMapping.containsKey(streamlinersAppliedOnCurrentPath)){
                TreeNode child = globalMapping.get(streamlinersAppliedOnCurrentPath);
                child.addParent(this);
                childNodes.put(possibleStreamliner, child);
                rewardValue += child.rewardValue;
                numberOfVisits += child.numberOfVisits;
            }
            streamlinersAppliedOnCurrentPath.remove(possibleStreamliner);
        }
    }



    public void addReward(double reward){
        this.rewardValue += reward;
    }

    public void incrementVisitCount(){
        this.numberOfVisits++;
    }


    public HashMap<Integer, TreeNode> getChildren(){
        return childNodes;
    }

    public Set<TreeNode> getParents(){
        return parents;
    }



    public void addParent(TreeNode treeNode){
        parents.add(treeNode);
    }

    /**
     * This method is used to notify parents that a child has been created by another node
     */
    public void notifyChildCreation(int streamlinerApplied, Set<Integer> appliedStreamliners, TreeNode child){
        this.childNodes.put(streamlinerApplied, child);
    }

    public void removeChild(int streamliner){
        assert(childNodes.containsKey(streamliner));
        childNodes.remove(streamliner);
    }

    public TreeNode getChild(int streamliner){
        return childNodes.get(streamliner);
    }



    @Override
    public String toString() {
        String s =  "TreeNode{" +
                ", rewardValue=" + rewardValue +
                ", numberOfVisits=" + numberOfVisits +
                ", parent size = " + parents.size() + "\n";
        return s;
    }


    /*
    Need to implement
     */



}
