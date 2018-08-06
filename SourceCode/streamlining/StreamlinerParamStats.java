import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


/*
This represents a param file which has been run under one or more models.
 */
public class StreamlinerParamStats implements Serializable{


    /*
    Now we need to make sure the ordering is the same across all StreamlinerParamStats
     */
    private HashMap<String, InstanceStats> singleModelInstanceStats;
    public String param;
    private boolean unsat;

    public StreamlinerParamStats(String param, HashMap<String, InstanceStats> singleModelInstanceStats) {
        this.singleModelInstanceStats = singleModelInstanceStats;
        this.param = param;

        for (InstanceStats instanceStats : singleModelInstanceStats.values()){
            if (instanceStats.satisfiable() == InstanceStats.SATISFIABLE.UNSAT) {
                System.out.println(param + " is UNSAT!");
                unsat = true;
                return;
            }
        }
    }


    public String getBestModel(){
        double minSearchNodes = Double.POSITIVE_INFINITY;
        String bestModel = null;

        for(String model: singleModelInstanceStats.keySet()){
            if(singleModelInstanceStats.get(model).searchNodes < minSearchNodes){
                minSearchNodes = singleModelInstanceStats.get(model).searchNodes;
                bestModel = model;
            }
        }

        assert(bestModel != null);
        return bestModel;

    }

//
    public double minSearchNodes(){
        return Collections.min(singleModelInstanceStats.values(), new Comparator<InstanceStats>() {
            @Override
            public int compare(InstanceStats o1, InstanceStats o2) {
                return Double.compare(o1.searchNodes, o2.searchNodes);
            }
        }).searchNodes;
    }
////
//    public double maxSearchNodes(){
//        return Collections.max(singleModelInstanceStats.values(), new Comparator<InstanceStats>() {
//            @Override
//            public int compare(InstanceStats o1, InstanceStats o2) {
//                return Double.compare(o1.searchNodes, o2.searchNodes);
//            }
//        }).searchNodes;
//    }
//
    public double averageSearchNodes(){
        double searchNodes = 0;
       for (InstanceStats instanceStats: singleModelInstanceStats.values()){
           searchNodes += instanceStats.searchNodes;
       }
       return searchNodes/singleModelInstanceStats.size();
    }


    public boolean unsat(){
        return unsat;
    }

    public InstanceStats get(String model){
        return singleModelInstanceStats.get(model);
    }

}
