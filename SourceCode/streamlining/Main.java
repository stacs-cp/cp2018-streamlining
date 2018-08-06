import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Main {


    final static Logger logger = Logger.getLogger(Main.class);

    static MCTS mcts = null;

    public static void main (String [] args){

        if (args.length != 7) {
            System.out.println("Not enough args: USAGE { essenceFile conjureOutputDir TestInstanceDir paramDir minSearchNodes}");
            System.out.println("Conjure Output contains the location where intermediate output files will be stored during search");
            System.out.println("TestInstanceDir should contain the .info files for the original models");
            System.out.println("Param dir should contain the set of params which match the TestInstanceDir .info files");
            System.out.println("The minimum number of search nodes for a instance to be considered");
            System.out.println("Whether or not the state should be persisted");
            System.out.println("Number of cores to be used");
            System.exit(-1);
        }

        for (String arg : args){
            System.out.println(arg);
        }
        String essenceFile = args[0];
        String conjureOutputDir = args[1];
        String testInstanceDir = args[2];
        String paramDir = args[3];
        String minTrainingInstanceSize = args[4];
        final boolean persistState = Boolean.parseBoolean(args[5]);
        int numberOfCores = Integer.parseInt(args[6]);

        if(persistState) {
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    super.run();
                    try{
                        System.out.println("Persisting state to file");
                        File tempFile = persistState();
                        overwriteExistingState(tempFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            if (stateExists()) {
                try {
                    mcts = loadState();
                    mcts.mcts();

                } catch (IOException e) {
                    logger.error(e.getMessage());
                    logger.error(e.getStackTrace());
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage());
                    logger.error(e.getStackTrace());
                }

            } else {
                State state = new State(essenceFile, conjureOutputDir, testInstanceDir, paramDir, minTrainingInstanceSize);
                mcts = new MCTS(state, new UCTSelection(), new SingleModelSimulation(numberOfCores), new BasicBackProp(), new RandomExpansion());
                mcts.mcts();
            }
        }

//               try {
//            if (persistState) {
//                persistState(mcts);
//            }
//        }catch(IOException e){
//            logger.error("Could not persist the objects to file");
//            e.printStackTrace();
//            logger.error(e.getMessage());
//        }


    }

    public static boolean stateExists(){
        File file = new File("persisted_state.ser");
        return file.exists();
    }

    public static MCTS loadState() throws IOException, ClassNotFoundException {

        // Reading the object from a file
        FileInputStream file = new FileInputStream("persisted_state.ser");
        ObjectInputStream in = new ObjectInputStream(file);

        // Method for deserialization of object
        MCTS mcts = (MCTS) in.readObject();

        in.close();
        file.close();

        System.out.println("MCTS object has been succesfully deserialized");
        return mcts;
    }

    public static File persistState() throws IOException {

        File tempFile = File.createTempFile("persisted-", "-state");
        tempFile.deleteOnExit();
        FileOutputStream file = new FileOutputStream(tempFile);
        ObjectOutputStream out = new ObjectOutputStream(file);

        // Method for serialization of object
        out.writeObject(mcts);

        out.close();
        file.close();

        System.out.println("Object has been serialized");
        return tempFile;
    }

    public static void overwriteExistingState(File file) throws IOException {
        Files.move(Paths.get(file.getPath()), Paths.get("persisted_state.ser"), StandardCopyOption.REPLACE_EXISTING);
    }


}
