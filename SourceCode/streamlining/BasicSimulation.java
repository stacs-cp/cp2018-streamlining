import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public abstract class BasicSimulation implements SimulationPolicy, Serializable{
    private Random random = new Random();
    final static Logger logger = Logger.getLogger(MCTS.class);
    private int numberOfCores;
    private String OUTPUT_FILE = "output_commands.txt";

    public BasicSimulation(int numberOfCores){
        this.numberOfCores = numberOfCores;
    }



    public StreamlinerStats parseStreamlinerStats(State state, ArrayList<String> trainingInstancesUsed) throws IOException {
        assert(state.getTrainingInstanceStats() != null);
        HashMap<String, StreamlinerParamStats> instanceStatsHashMap = new HashMap<>();
        for (String instanceStats: state.getTrainingInstanceStats().getParams()){
            if(trainingInstancesUsed.contains(instanceStats)){
                StreamlinerParamStats streamlinerParamStats = state.parseInfoFile(state.conjureOutputDir, instanceStats);
                instanceStatsHashMap.put(instanceStats, streamlinerParamStats);
            }

        }
        return new StreamlinerStats(instanceStatsHashMap);
    }




    @Override
    public StreamlinerStats simulate(State state, TreeNodeTuple treeNodeTuple, StreamlinerStats parentStats) {
        System.out.println("SIMULATION---------");

        Set<Integer> appliedStreamliners = treeNodeTuple.streamlinersApplied;
        String streamliners = state.getStreamlinerCombinationAsString(appliedStreamliners, ",");

        try {
            //Model the Essence file with the applied streamliners
            double conjureModellingTime = conjureModel(state, streamliners);

            //Generate the output file to run the Training Instances
            ArrayList<String> trainingInstancesUsed = generateParallelTrainingFile(OUTPUT_FILE, state, streamliners, parentStats);


            //Run the training instances in parallel
            runTrainingInstances();

            //Parse the streamliner stats
            StreamlinerStats stats = parseStreamlinerStats(state, trainingInstancesUsed);
            state.registerStreamlinerStats(appliedStreamliners, stats);
            state.saveStreamlinerGeneratedFiles(appliedStreamliners);
            state.outputResult(appliedStreamliners);
            logger.info("   PERCENTAGE UNSAT: " + state.getPercentageUNSAT(stats.getNumberUNSAT()));
            logger.info("   PERCENTAGE REDUCTION: " + stats.averageSearchNodeReduction(state));
            logger.info("   BEST MODEL: " + stats.getBestModel());
            logger.info("---------");
//            throw new RuntimeException();
            return stats;
        }catch(IOException e){
                e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Exception during Simulation");
    }


    /**
     * Generate the GNU parallel file which will run all of the training instances with the given streamliners
     * @param outputFile
     * @param state
     * @param streamliners
     * @return The instances that the streamliner combo was run on
     */
    private ArrayList<String> generateParallelTrainingFile(String outputFile, State state, String streamliners, StreamlinerStats parentStats){

        ArrayList<String> trainingInstancesUsed = new ArrayList<>();
        try{
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

            //Iterate through all of the Instances run by the parent
            for(String instanceStat : parentStats.getParams()){
                StreamlinerParamStats instanceStats = parentStats.getParamStats(instanceStat);
                //Only use the instances which were satisfiable for the parent for training
                if (!instanceStats.unsat()) {
//                    System.out.println("Training file " + instanceStats.param);
                    for(String trainingCommand : generateTrainingInstanceCommand(state,parentStats, instanceStat, streamliners)){
                        bufferedWriter.write(trainingCommand);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }

                    trainingInstancesUsed.add(instanceStats.param);
                }
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return trainingInstancesUsed;
    }

    private void runTrainingInstances() throws IOException, InterruptedException {

        Process p = Runtime.getRuntime().exec("parallel -j " + numberOfCores + " -a " + OUTPUT_FILE);
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));


        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));

        StringBuffer stdOut= new StringBuffer();
        String s;
        while ((s = stdInput.readLine()) != null) {
            stdOut.append(s);
        }
        stdInput.close();

        StringBuffer stdErr = new StringBuffer();
        while ((s = stdError.readLine()) != null) {
            stdErr.append(s);
        }
        stdError.close();

        p.waitFor();
        if(p.exitValue() != 0){
            logger.debug("PARALLEL ABNORMALLY for:");
            logger.debug("STDOUT: " + stdOut.toString());
            logger.debug("STDERR: " + stdErr.toString());
        }
    }

    /**
     * Model the Essence file with the applied streamliners
     * @param streamliners
     * @throws IOException
     * @throws InterruptedException
     * @return Conjure Modelling Time
//     */
    public double conjureModel(State state, String streamliners) throws IOException, InterruptedException {
        double timeBefore = System.currentTimeMillis();
        String conjureModellingCommand = generateConjureModellingCommand(state, streamliners);
        System.out.println("Conjure modelling command is " + conjureModellingCommand);
        Process conjure = Runtime.getRuntime().exec(generateConjureModellingCommand(state, streamliners));
        conjure.waitFor();
        return System.currentTimeMillis() - timeBefore;
    }


    public abstract String generateConjureModellingCommand(State state, String streamliners);

    public ArrayList<String> generateTrainingInstanceCommand(State state, StreamlinerStats streamlinerStats, String paramFile, String streamliners) {

        File dir = new File(state.conjureOutputDir);
        File [] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".eprime");
            }
        });


        if(files == null || files.length == 0){
            throw new RuntimeException("No modelling files in conjure output directory");
        }

        ArrayList<String> commands = new ArrayList<>();

        /*
        Setting the node limit in respect of the best model in the unstreamlined essence
         */
        int nodeLimit = (int) state.getTrainingInstanceStats().searchNodes(paramFile);

        for(File eprimeModel : files){
            String[] commandAr = {"conjure", "solve", state.essenceFile, state.paramDirectory + "/" + paramFile + ".param", "--generate-streamliners",
                    streamliners, "--savilerow-options", "\"-preprocess None\"", "--solver-options", String.format("\"-nodelimit %d \"", nodeLimit + 1), "-o", state.conjureOutputDir,
                    "--copy-solutions=no", "--use-existing-models", eprimeModel.getName()};

            String command = "";
            for (String cm : commandAr) {
                command += cm + " ";
            }
            commands.add(command);

        }

        return commands;
    }



}
