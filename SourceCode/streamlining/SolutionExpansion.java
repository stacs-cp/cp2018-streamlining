import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class SolutionExpansion implements ExpansionPolicy, Serializable{


    /**
      * @param state
     * @param allowedChildren
     * @param appliedStreamliners
     * @return
     */
    @Override
    public int expand(State state, Set<Integer> allowedChildren, Set<Integer> appliedStreamliners) {

        HashSet<Integer> potentialStreamlinerCombo = new HashSet<>(appliedStreamliners);
        SortedMap<Integer, Double> childStats = new TreeMap<>();

        for(Integer streamliner: allowedChildren){
            potentialStreamlinerCombo.add(streamliner);

            try {
                HashMap<String, Boolean> trainingInstanceStats = state.generateRetainSolutionStats(appliedStreamliners, potentialStreamlinerCombo);
                double numberRetained = 0;
                for(String trainingInstance : trainingInstanceStats.keySet()){
                    numberRetained += trainingInstanceStats.get(trainingInstance) ? 1 : 0;
                }

                //The child retains the solution to every instance the parent found
                if (numberRetained / trainingInstanceStats.size() == 1){
                    return streamliner;
                }else{
                    childStats.put(streamliner, (numberRetained/trainingInstanceStats.size()));
                }

                if (numberRetained / trainingInstanceStats.size() > 0.5){
                    return streamliner;
                }



            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            potentialStreamlinerCombo.remove(streamliner);
        }

        return childStats.firstKey();

    }
}
