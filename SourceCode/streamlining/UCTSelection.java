import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;

public class UCTSelection implements SelectionPolicy, Serializable{


    //We arent doing anything with this?
    private double explorationConstant = 0.01;
    final static Logger logger = Logger.getLogger(UCTSelection.class);

    @Override
    public int selectNode(TreeNode node) {
        logger.info("UCT Selection--------");
         if (node.getChildren() == null){
             throw new RuntimeException("UCT Selection: Children HashSet not initialized");
         }
         double maxValue = Double.NEGATIVE_INFINITY;
         int bestChild = -1;
         logger.info("    Selection node: " + node);
         logger.info("\n");


         for (int streamliner: node.getChildren().keySet()){
             TreeNode child = node.getChild(streamliner);
             double uctValue = (child.rewardValue / child.numberOfVisits)  + 2 * explorationConstant * Math.sqrt((2*Math.log(node.numberOfVisits))/ child.numberOfVisits);
             logger.info("       UCT value is " + uctValue);
             logger.info("       " + child);
             if(uctValue > maxValue){
                 maxValue = uctValue;
                 bestChild = streamliner;
             }
         }

        logger.info("------------------");
         if (bestChild < 0){
             return -1;
         }else{
             return bestChild;
         }
    }
}
