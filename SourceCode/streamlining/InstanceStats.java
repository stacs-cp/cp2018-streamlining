import java.io.Serializable;
import java.util.HashMap;

public class InstanceStats implements Serializable {

    public String paramFile;
    public static final String SOLVER_NODES = "SolverNodes";
    public static final String SOLVER_SATISFIABLE = "SolverSatisfiable";
    public static final String SAVILE_ROW_TIME = "SavileRowTotalTime";
    public static final String SOLVER_TOTAL_TIME  = "SolverTotalTime";
    public static final String SOLVER_SETUP_TIME = "SolverSetupTime";
    public static final String SOLVER_SOLVE_TIME = "SolverSolveTime";
    public static final String PROPORTION_OF_SEARCH = "ProportionOfSearch";
    public static final String INITIAL_SEARCH_SIZE = "InitialSearchSize";
    public static final String NODE_LIMIT_REACHED = "NodeLimitReached";
    public static final String SOLVER_TIME_OUT = "SolverTimeOut";

    public double searchNodes;
    private SATISFIABLE satisfiable;
    public double savileRowTime;
    public double solverTotalTime;
    public double solverSetupTime;
    public double solverSolveTime;
    public double costPerNode;
//    private double searchSpaceReduction;
    public double proportionOfSearch;
    public double initialSearchSize;

    public enum SATISFIABLE{SAT, UNSAT, UNDETERMINED};



    public InstanceStats(HashMap<String, String> statMapping){
        this.searchNodes = Integer.valueOf(statMapping.get(SOLVER_NODES));
        this.solverSetupTime = Double.valueOf(statMapping.get(SOLVER_SETUP_TIME));
        this.solverSolveTime = Double.valueOf((statMapping.get(SOLVER_SOLVE_TIME)));
        this.savileRowTime = Double.valueOf(statMapping.get(SAVILE_ROW_TIME));
        this.solverTotalTime = Double.valueOf(statMapping.get(SOLVER_TOTAL_TIME));

        if (statMapping.get(SOLVER_SATISFIABLE).equals("1"))
            this.satisfiable = SATISFIABLE.SAT;
        else if (statMapping.get(SOLVER_TIME_OUT).equals("1") || statMapping.get(NODE_LIMIT_REACHED).equals("1"))
        {
            this.satisfiable = SATISFIABLE.UNDETERMINED;
        }
        else{
            this.satisfiable = SATISFIABLE.UNSAT;
        }


        if (statMapping.containsKey(PROPORTION_OF_SEARCH))
            this.proportionOfSearch = Double.valueOf(statMapping.get(PROPORTION_OF_SEARCH));

        if(statMapping.containsKey(INITIAL_SEARCH_SIZE))
            this.initialSearchSize = Double.valueOf(statMapping.get(INITIAL_SEARCH_SIZE));

    }


    @Override
    public String toString() {
        return "InstanceStats{" +
                "searchNodes=" + searchNodes +
                ", satisfiable=" + satisfiable +
                '}';
    }

    public double getSearchNodes() {
        return searchNodes;
    }

    public SATISFIABLE satisfiable() {
        return satisfiable;
    }

    public double searchSpaceReduction(InstanceStats originalModel) {
        System.out.println("Original model search nodees : " + originalModel.searchNodes);
        System.out.println("Streamlined model search nodes " + this.searchNodes);
        double searchSpaceReduction;
        if (this.searchNodes == 0){
            searchSpaceReduction = 0;
            this.costPerNode = 0;
        }else{
            this.costPerNode = ((solverSolveTime*1000)/this.searchNodes);
            searchSpaceReduction = (this.searchNodes/originalModel.searchNodes);
        }
        return searchSpaceReduction;
    }




}
