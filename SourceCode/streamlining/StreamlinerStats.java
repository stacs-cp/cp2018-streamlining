import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StreamlinerStats implements Serializable{

    final static Logger logger = Logger.getLogger(StreamlinerStats.class);
    private HashMap<String, StreamlinerParamStats> instanceStatsMapping;
    private String bestModel;

    public StreamlinerStats(HashMap<String, StreamlinerParamStats> instanceStatsMapping){
        this.instanceStatsMapping = instanceStatsMapping;
        this.bestModel = calculateBestModel();
    }


    /*
    Work out what model performs best across all of the params
     */
    private String calculateBestModel(){

        HashMap<String, Integer> bestModels = new HashMap<>();
        for(String paramFile: instanceStatsMapping.keySet()) {
            String bestModel = instanceStatsMapping.get(paramFile).getBestModel();
            if(bestModels.containsKey(bestModel)){
                bestModels.put(bestModel, bestModels.get(bestModel)+1);
            }else{
                bestModels.put(bestModel, 1);
            }
        }

        int highestNum = -1;
        String bestModel = null;
        for(Map.Entry<String, Integer> models : bestModels.entrySet()){
            if (models.getValue() > highestNum){
                highestNum = models.getValue();
                bestModel = models.getKey();
            }
        }

        assert(bestModel != null);
        return bestModel;


    }


    /*
    Calculate average search node reduction on the Satisfiable instances for the best model. We don't include the UNSAT
    instances in this calculation as it can skew off the percentage reduction.
     */
    public double averageSearchNodeReduction(State state){
        double totalNumberOfSearchNodes = 0;
        double totalOriginalNumberOfSearchNodes = 0;

        //Calculate the average reduction based upon this model
        for(String paramFile: instanceStatsMapping.keySet()){
//            for(InstanceStats trainingInstance : trainingInstances){

                if (!instanceStatsMapping.get(paramFile).unsat()){
                    //This is also taking the search nodes for the best model on average for the original unstreamlined model.
                    totalOriginalNumberOfSearchNodes += state.getTrainingInstanceStats().searchNodes(paramFile);
                    //This is taking the search nodes for the best model on average
                    totalNumberOfSearchNodes += searchNodes(paramFile);

                }
//            }
        }
        System.out.println("    Total number of search nodes " + totalOriginalNumberOfSearchNodes);
        System.out.println("    Total number of streamlined search nodes " + totalNumberOfSearchNodes);
        if (totalOriginalNumberOfSearchNodes == 0){
            return -1;
        }else
            return totalNumberOfSearchNodes/totalOriginalNumberOfSearchNodes;
    }

    public int getNumberUNSAT(){
        int numberUNSAT = 0;
        for(StreamlinerParamStats stats: instanceStatsMapping.values()){
            numberUNSAT += stats.unsat() ? 1 : 0;
        }
        return numberUNSAT;
    }


    public String getBestModel(){
        return bestModel;
    }


    public Set<String> getParams(){
        return instanceStatsMapping.keySet();
    }


    public int numberOfParams(){
        return instanceStatsMapping.size();
    }

    public double searchNodes(String paramFile){
        return instanceStatsMapping.get(paramFile).get(bestModel).searchNodes;
    }

    public boolean unsat(String paramFile){
        return instanceStatsMapping.get(paramFile).unsat();
    }

    public double searchSpaceReduction(String paramFile, InstanceStats originalModel){
        return instanceStatsMapping.get(paramFile).get(bestModel).searchSpaceReduction(originalModel);
    }

    public StreamlinerParamStats getParamStats(String param){
        assert(instanceStatsMapping.containsKey(param));
        return instanceStatsMapping.get(param);
    }

    public InstanceStats getInstanceStats(String param){
        return instanceStatsMapping.get(param).get(bestModel);
    }
}
