import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import javafx.collections.transformation.SortedList;
import org.apache.log4j.Logger;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class State implements Serializable{

    //Contains the training instances
    private StreamlinerStats trainingInstanceStats;
//    private HashMap<String, StreamlinerParamStats> trainingInstanceStats;
    final static Logger logger = Logger.getLogger(MCTS.class);
    //The possible streamliners produced by conjure
    private Set<Integer> possibleStreamliners = new HashSet<>();
    //This contains the results of all streamliners
    private HashMap<Set<Integer>, StreamlinerStats> streamlinerResults;

    public String essenceFile;
    public String conjureOutputDir;
    public String paramDirectory;

    private final String CONJURE_MODEL_PREFIX = "model000001";
    private final String streamlinerResultsDir;

    //Writer for writing out the streamliner results to file
    private transient BufferedWriter outputWriter;
    private final String OUTPUT_FILE_NAME;


    public State(String inputEssenceFile, String inputConjureOutputDir, String testInstanceDirectory, String paramDirectory, String minNumberSearchNodes){
        essenceFile = inputEssenceFile;
        this.OUTPUT_FILE_NAME = essenceFile.split(".essence")[0] + "Results.csv";
        conjureOutputDir = inputConjureOutputDir;
        this.paramDirectory = paramDirectory;
        streamlinerResults = new HashMap<>();
        streamlinerResultsDir = conjureOutputDir + "/StreamlinerResults";
        initializeDir(streamlinerResultsDir);
        generateStreamliners(inputEssenceFile);
        readInTestInstances(testInstanceDirectory, paramDirectory, minNumberSearchNodes);
        intializeDisallowedCombinations();

    }

    private void initializeDir(String dir){
        File folder = new File(dir);
        if (!folder.exists()){
            boolean created = folder.mkdirs();
            if(!created)
                throw new RuntimeException("StreamlinerResults Dir could not be created");
        }
    }



    private void intializeDisallowedCombinations(){
        disallowedStreamlinerCombinations = new HashMap<Integer, Set<Set<Integer>>>();
        for(Integer streamliner: possibleStreamliners){
            disallowedStreamlinerCombinations.put(streamliner, new HashSet<Set<Integer>>());
        }

    }

    public File[] getInfoFiles(String testInstanceDirectory, final String param, final Pattern pattern){
        File dir = new File(testInstanceDirectory);
        File [] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                Matcher m = pattern.matcher(name);
//                System.out.println("Name is " + name);
//                System.out.println(m.matches());
                return name.contains(param) && m.matches();
            }
        });

        if(files == null){
            throw new RuntimeException("Parsing streamliner stats, cant find info files");
        }

        return files;

    }


    public StreamlinerParamStats parseInfoFile(String directory, String param){
            try {
                //Get all of the info files for this param file (all of the models)
                File [] paramInfoFiles = getInfoFiles(directory, param, Pattern.compile(".*-info"));

                if (paramInfoFiles.length == 0){
                    return null;
                }
                System.out.println("param file size " + paramInfoFiles.length);
//                ArrayList<InstanceStats> instanceStats = new ArrayList<>();
                HashMap<String, InstanceStats> instanceStatsHashMap = new HashMap<>();
                for (File file : paramInfoFiles){
//                    System.out.println("File: " + file.getName());
                    System.out.println("File is " + file);
                    HashMap<String, String> statMapping = parseStatMapping(file);
                    if(!statMapping.get("SavileRowTimeOut").equals("1")){
                        instanceStatsHashMap.put(file.getName().split("-")[0], new InstanceStats(parseStatMapping(file)));
                    }
//                    instanceStats.add(new InstanceStats(parseStatMapping(file)));
                }
                assert(instanceStatsHashMap.size() != 0);
                return new StreamlinerParamStats(param, instanceStatsHashMap);


            }catch(IOException e){
                System.out.println("IO Exception for param file: " + param);
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            throw new RuntimeException("Not supposeed to get here");

    }


    /**
     * Read in all .info files and keep the training set of instances which is roughly 70%
      * @param testInstanceDirectory
     */
    private void readInTestInstances(String testInstanceDirectory, String paramDirectory, String minNumberSearchNodes){
        double minSearchNodes = Double.valueOf(minNumberSearchNodes);
//        this.trainingInstanceStats = new HashMap<>();
        System.out.println("REading in test instances");

        HashMap<String, StreamlinerParamStats> trainingInstances = new HashMap<>();
        SortedMap<StreamlinerParamStats, String> reverse = new TreeMap<>(new Comparator<StreamlinerParamStats>() {
            @Override
            public int compare(StreamlinerParamStats o1, StreamlinerParamStats o2) {
                return Double.compare(o1.minSearchNodes(), o2.minSearchNodes());
            }
        });
        //This will stay the same. This is getting us the list of param files
        HashMap<String, File> paramFiles = getParamFiles(paramDirectory);

        for(String paramFileName : paramFiles.keySet()){
            StreamlinerParamStats streamlinerParamStats = parseInfoFile(testInstanceDirectory, paramFileName);
            if (streamlinerParamStats == null){
                System.out.println("Param " + paramFileName  + " cant be found");
                continue;
            }
            if (!streamlinerParamStats.unsat()){
                System.out.println("Param name is " + paramFileName);
                System.out.println("average search nodes is " + streamlinerParamStats.averageSearchNodes());
                if (streamlinerParamStats.minSearchNodes() > minSearchNodes) {
                    System.out.println("Param name is " + paramFileName);
                    System.out.println("average search nodes is " + streamlinerParamStats.minSearchNodes());
                    trainingInstances.put(paramFileName, streamlinerParamStats);
                    reverse.put(streamlinerParamStats, paramFileName);
                }
            }else{

            }

        }

        assert(trainingInstances.size() != 0);

        int numberOfTrainingInstances = (int) (trainingInstances.size() * 0.7) > 10 ? 10 : (int) (trainingInstances.size() * 0.7);
        System.out.println("Size before: " + trainingInstances.size());
        //Remove test instances
        while(trainingInstances.size() > numberOfTrainingInstances){
            StreamlinerParamStats trainingInstance = reverse.lastKey();
            trainingInstances.remove(reverse.get(trainingInstance));
            reverse.remove(trainingInstance);
        }

        System.out.println("Size after: " + trainingInstances.size());
        System.out.println("There are this many training instances: " + numberOfTrainingInstances);

        for(StreamlinerParamStats trainingInstance: trainingInstances.values()){
            System.out.println("-----------");
            System.out.println("    Param File" + trainingInstance.param);
            System.out.println("    Number of search nodes: " + (int) trainingInstance.minSearchNodes());
        }

        trainingInstanceStats = new StreamlinerStats(trainingInstances);
        streamlinerResults.put(new HashSet<Integer>(), new StreamlinerStats(trainingInstances));
    }

    public HashMap<String, String> parseStatMapping(File statFile) throws IOException {
//        File statFile = new File(testInstanceDirectory + "/" + CONJURE_MODEL_PREFIX + "-" + paramFile + ".eprime-info");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(statFile));
        String input = null;
        HashMap<String, String> stats = new HashMap<>();
        while ((input = bufferedReader.readLine()) != null){
            String [] inputSplit = input.split(":");
            stats.put(inputSplit[0], inputSplit[1]);
        }
        bufferedReader.close();
        return stats;
    }


//    private InstanceStats parseInstanceStat(String paramName, File paramFile) throws IOException {
//        return new InstanceStats(paramName, parseStatMapping(paramFile));
//    }


    /**
     * Add an InstanceStat to the StreamlinerStats object that it belongs to.
     * @param streamlinersApplied
     * @param streamlinerStats
     */
    public void registerStreamlinerStats(Set<Integer> streamlinersApplied, StreamlinerStats streamlinerStats){
            streamlinerResults.put(streamlinersApplied, streamlinerStats);
    }


    /**
     * Save out the Streamlined Eprime Model and the solutions for the param files if they exist
     * @param streamlinersApplied
     */
    public void saveStreamlinerGeneratedFiles(Set<Integer> streamlinersApplied) {
        StreamlinerStats streamlinerStats = streamlinerResults.get(streamlinersApplied);
        //Base dir for the streamliner
        String baseDir = streamlinerResultsDir + "/" + getStreamlinerCombiniationAsSortedString(streamlinersApplied, "-");

        System.out.println("Saving streamliner generated files");
        initializeDir(baseDir);

        for(String param : streamlinerStats.getParams()){

            try {
                //Saving the eprime models
                File[] eprimeModels = getInfoFiles(conjureOutputDir, param, Pattern.compile("\\.eprime$"));
                for (File file : eprimeModels) {
                    Files.move(file.toPath(), Paths.get(baseDir));

                }

                File[] modelSolutionFiles = getInfoFiles(conjureOutputDir, param, Pattern.compile("\\.eprime-solution"));
                for (File solutionFile : modelSolutionFiles) {
                    Files.move(solutionFile.toPath(), Paths.get(baseDir));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private BufferedWriter getWriter(String textFile) throws IOException {
        if (outputWriter == null){
            outputWriter = new BufferedWriter(new FileWriter(textFile));
            initializeOutputFile(outputWriter);
        }
        return outputWriter;
    }

    private void initializeOutputFile(BufferedWriter bufferedWriter) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("StreamlinerCombination,");
        for(String param: trainingInstanceStats.getParams()){
            sb.append(param);
            sb.append(",");
        }
        sb.append("PercentageInstancesUNSAT,");
        sb.append("AverageReduction,");
        sb.append("BestModel");
        sb.append("\n");
        bufferedWriter.write(sb.toString());
        bufferedWriter.flush();
    }

    /**
     * Write out a streamliner result to file in CSV
     * @param streamlinersApplied
     */
    public void outputResult(Set<Integer> streamlinersApplied){
        StreamlinerStats currentStreamlinerStats = streamlinerResults.get(streamlinersApplied);
        assert(currentStreamlinerStats != null);

        StringBuilder stringBuffer = new StringBuilder();
        try {
            BufferedWriter outputFile = getWriter(OUTPUT_FILE_NAME);
            stringBuffer.append(getStreamlinerCombinationAsString(streamlinersApplied, "-"));
            stringBuffer.append(",");

            for(String trainingInstance: trainingInstanceStats.getParams()){
                if(currentStreamlinerStats.getParams().contains(trainingInstance)){
//                    StreamlinerParamStats instanceStats = currentStreamlinerStats.getParamStats(trainingInstance);
//                    assert(instanceStats != null);
                    if(!currentStreamlinerStats.unsat(trainingInstance)){
                        stringBuffer.append(currentStreamlinerStats.searchSpaceReduction(trainingInstance, trainingInstanceStats.getInstanceStats(trainingInstance)));
                    }else{
                        stringBuffer.append("-1");
                    }
                    stringBuffer.append(",");
                }else{
                    stringBuffer.append("-1,");
                }
            }

            stringBuffer.append(getPercentageUNSAT(currentStreamlinerStats.getNumberUNSAT()));
            stringBuffer.append(",");
            stringBuffer.append(currentStreamlinerStats.averageSearchNodeReduction(this));
            stringBuffer.append(",");
            stringBuffer.append(currentStreamlinerStats.getBestModel());
            stringBuffer.append("\n");

            outputFile.write(stringBuffer.toString());
            outputFile.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    public double getPercentageUNSAT(int numberUNSAT){
        return numberUNSAT/(double) trainingInstanceStats.numberOfParams();
    }


//    public InstanceStats getStatsForOriginalModel(String paramFile){
//        return trainingInstanceStats.get(paramFile);
//    }


    public String getStreamlinerCombiniationAsSortedString(Set<Integer> appliedStreamliners, String separationCharacter){
        ArrayList<Integer> streamliners = new ArrayList<>(appliedStreamliners);
        Collections.sort(streamliners);
        return getStreamlinerCombinationAsString(streamliners, separationCharacter);
    }


    public String getStreamlinerCombinationAsString(Collection<Integer> appliedStreamliners, String separationCharacter){
        String streamliner = "";
        int i = 0;
        for(Integer currentStreamliner : appliedStreamliners){
            if (++i == appliedStreamliners.size()){
               streamliner += currentStreamliner;
            }else{
                streamliner += currentStreamliner + separationCharacter;
            }
        }
        return streamliner;
    }

    /**
     * Helper method for printing out the path of Streamliner combination
     * @param appliedStreamliners
     */
    public void printStreamlinerPath(Set<Integer> appliedStreamliners){
        System.out.println("      streamliner Path: " + getStreamlinerCombinationAsString(appliedStreamliners, "-"));

    }


    /**
     * Get the list of file names for all possible test instance params.
     * @param testInstanceDirectory
     * @return
     */
    private HashMap<String, File> getParamFiles(String testInstanceDirectory) {
        File folder = new File(testInstanceDirectory);
        HashMap<String, File> paramFiles = new HashMap<>();
        for (File fileEntry : folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().contains("param");
            }
        })) {
            paramFiles.put(fileEntry.getName().split("\\.p")[0], fileEntry);
        }
        return paramFiles;
    }


    /**
     * Call conjure to generate the set of streamliners for the input Essence model
     * @param essenceFile
     */
    private void generateStreamliners(String essenceFile){
        try {
            Process p = Runtime.getRuntime().exec("conjure streamlining " + essenceFile );
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            StringBuffer stringBuffer = new StringBuffer();
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                stringBuffer.append(s);
            }
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(stringBuffer.toString());

            for (Object key : object.keySet()){
                possibleStreamliners.add(Integer.valueOf((String) key));
            }

            stdInput.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public Set<Integer> getInitialStreamlinerSet(){
        return possibleStreamliners;
    }



    public void addInvalidStreamlinerCombination(Set<Integer> streamlinerCombination){
        logger.info("   Adding invalid streamliner combination: " + getStreamlinerCombinationAsString(streamlinerCombination, "-"));
        //If the disallowed combination is just a single streamliner remove it from being considered again
        if(streamlinerCombination.size() == 1){
            logger.info("   Removing a single streamliner");
            possibleStreamliners.remove(streamlinerCombination.iterator().next());
        }else{
            Set<Integer> currentStreamlinerCombo = new HashSet<>(streamlinerCombination);
            for(Integer streamliner : streamlinerCombination){
                currentStreamlinerCombo.remove(streamliner);
                disallowedStreamlinerCombinations.get(streamliner).add(new HashSet<Integer>(currentStreamlinerCombo));
                currentStreamlinerCombo.add(streamliner);
            }
        }
    }



    private HashMap<Integer, Set<Set<Integer>>> disallowedStreamlinerCombinations;




    private boolean isSubset(Set<Integer> setA, Set<Integer> setB){
        return setB.containsAll(setA);
    }


    /**
     * Work out the Set of possible children that can be applied from a given point in the lattice. Return a set of the individual
     * streamliners that can be applied.
     * @param globalMapping
     * @param currentStreamlinerPath
     * @return
     */
    public Set<Integer> getAllowedChildren(HashMap<Set<Integer>, TreeNode> globalMapping, Set<Integer> currentStreamlinerPath){
        Set<Integer> allowedChildren = new HashSet<>();
        //Clone the current streamliner path
        HashSet<Integer> clonedStreamlinerPath = new HashSet<>(currentStreamlinerPath);
        //Iterate through all possible child streamliners
        for(Integer possibleStreamliner: getInitialStreamlinerSet()){

            if (!currentStreamlinerPath.contains(possibleStreamliner)){
                clonedStreamlinerPath.add(possibleStreamliner);

                if(!globalMapping.containsKey(clonedStreamlinerPath)){
                        boolean addChild = true;
                        //Add the current child to the path
                        //Iterate through all streamliners in the current path and check if adding the possible child
                        //would cause a disallowed combinatino
                        for(Integer streamliner: currentStreamlinerPath){

                            for(Set<Integer> disallowedCombination: disallowedStreamlinerCombinations.get(streamliner)){
                                if(isSubset(disallowedCombination, clonedStreamlinerPath)){
                                    addChild = false;
                                    break;
                                }
                            }

                            if(!addChild){
                                break;
                            }

                        }

                if(addChild)
                    allowedChildren.add(possibleStreamliner);

                }

                clonedStreamlinerPath.remove(possibleStreamliner);
            }


        }

        return allowedChildren;
    }


    public StreamlinerStats getTrainingInstanceStats() {
        return trainingInstanceStats;
    }

    public StreamlinerStats getStreamlinerResults(Set<Integer> streamlinersApplied){
        return streamlinerResults.get(streamlinersApplied);
    }



    /*
    Checking whether a potential child retains a solutions to the training instances for which the parent is SAT
     */
    public HashMap<String, Boolean> generateRetainSolutionStats(Set<Integer> appliedStreamliners, Set<Integer> potentialChildCombination) throws IOException, InterruptedException {

        //Grab the streamliner stats for the streamliner path
        StreamlinerStats streamlinerStats = streamlinerResults.get(appliedStreamliners);
        HashMap<String, Boolean> retainSolutionMapping = new HashMap<>();

        //Model the problem based upon the potential child combination
        conjureModel(getStreamlinerCombinationAsString(potentialChildCombination, ","));

        String baseDir = streamlinerResultsDir + "/" + getStreamlinerCombiniationAsSortedString(appliedStreamliners, "-");

        Path eprimeModelPath = Paths.get(conjureOutputDir + "/model000001.eprime");

        for (String param: trainingInstanceStats.getParams()){
//            StreamlinerParamStats currentTrainingInstance
//            String trainingInstance = currentTrainingInstance.param;
            if (!streamlinerStats.unsat(param)){
                File solutionFile = getInfoFiles(baseDir, param, Pattern.compile(".eprime-solution"))[0];
                File paramFile = getInfoFiles(paramDirectory, param, Pattern.compile("\\.param"))[0];

//                Path solutionPath = Paths.get(baseDir + "/" + trainingInstance + ".eprime-solution");
//                Path paramPath = Paths.get(   paramDirectory + "/" + trainingInstance + ".param");
                retainSolutionMapping.put(param, retainSolution(eprimeModelPath, solutionFile.toPath(), paramFile.toPath()));
            }
        }
        return retainSolutionMapping;
    }

    /**
     * Check if a set of streamliners
     * @param eprimeModelPath
     * @param paramFile
     * @param solutionPath
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean retainSolution(Path eprimeModelPath, Path paramFile, Path solutionPath) throws IOException, InterruptedException {
        Formatter fmt = new Formatter();
        fmt.format("conjure validate-solution --essence=%s --param=%s --solution=%s",eprimeModelPath.toString(),
                paramFile.toString(), solutionPath.toString());

        Process conjure = Runtime.getRuntime().exec(fmt.toString());
        conjure.waitFor();

        return conjure.exitValue() == 0;
    }




    /**
     * Model the Essence file with the applied streamliners
     * @param streamliners
     * @throws IOException
     * @throws InterruptedException
     * @return Conjure Modelling Time
     */
    public double conjureModel(String streamliners) throws IOException, InterruptedException {
        double timeBefore = System.currentTimeMillis();
        Process conjure = Runtime.getRuntime().exec("conjure modelling -ac " + essenceFile + " -o " + conjureOutputDir +
                " --generate-streamliners "+ streamliners);
        conjure.waitFor();
        return System.currentTimeMillis() - timeBefore;
    }


}
