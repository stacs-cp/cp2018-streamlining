import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SingleModelSimulation extends BasicSimulation{


    public SingleModelSimulation(int numberOfCores) {
        super(numberOfCores);
    }

    @Override
    public String generateConjureModellingCommand(State state, String streamliners) {
        return "conjure modelling -ac " + state.essenceFile + " -o " + state.conjureOutputDir + " --generate-streamliners "+ streamliners;
    }

}
