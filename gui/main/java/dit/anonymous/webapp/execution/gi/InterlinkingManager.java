package dit.anonymous.webapp.execution.gi;

import batch.AbstractBatchAlgorithm;
import batch.partitionbased.PBSM;
import batch.planesweep.PlaneSweep;
import batch.stripebased.StripeSTRSweep;
import batch.tilebased.GIAnt;
import batch.tilebased.RADON;
import batch.tilebased.StaticGIAnt;
import batch.tilebased.StaticRADON;
import batch.treebased.CRTree;
import batch.treebased.QuadTree;
import batch.treebased.RTree;
import datamodel.PlaneSweepStructure;
import datamodel.RelatedGeometries;
import datareader.*;
import dit.anonymous.webapp.utilities.configurations.HttpPaths;
import dit.anonymous.webapp.utilities.configurations.Options;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@RestController
@RequestMapping(HttpPaths.giReadData + "**")
public class InterlinkingManager {

    static public String algorithmType;
    static public String algorithm;
    static public int budget;
    public static AbstractReader source;
    public static String sourceFilename;
    public static AbstractReader target;
    public static String targetFilename;

    public static List<InterlinkingResults> results = new LinkedList<>();

    public static String setDataset(JSONObject configurations, String inputPath) {
        try{
            String entity_id = configurations.getString("entity_id");
            String filetype = configurations.getString("filetype");
            if (Objects.equals(entity_id, "source")){
                sourceFilename = configurations.getString("filename");
                source = getReader(filetype, inputPath, configurations);
            }
            else{
                targetFilename = configurations.getString("filename");
                target = getReader(filetype, inputPath, configurations);
            }
            return "SUCCESS";
        } catch (Exception e){
            System.out.println(e);
            return "ERROR: " + e.getClass().getSimpleName();
        }
    }

    public static AbstractReader getReader(String filetype, String inputPath, JSONObject configurations) throws Exception {
        if (Objects.equals(filetype, "CSV")) {
            boolean hasHeader = configurations.getBoolean("first_row");
            char separator = configurations.getString("separator").replace("\\t", "\t").charAt(0);
            int geometryIndex = configurations.getInt("geometry_index");
            return new GeometryCSVReader(hasHeader, separator, geometryIndex, new int[0], inputPath);
        } else if (Objects.equals(filetype, "GEOJSON")) {
            return new GeometryGeoJSONReader(inputPath);
        } else if (Objects.equals(filetype, "RDF")) {
            return new GeometryRDFReader(inputPath, new HashSet<String>());
        } else if (Objects.equals(filetype, "RDF/JSON")) {
            String prefix = configurations.getString("prefix");
            return new GeometryJSONRDFReader(inputPath, prefix, new HashSet<>());
        } else{
            return new GeometrySerializationReader(inputPath);
        }
    }

    public static AbstractBatchAlgorithm getProcessor(String algorithm){
        AbstractBatchAlgorithm processor;
        switch (algorithm) {
            case Options.STATIC_GIANT:
                processor = new StaticGIAnt(0, source, target);
                break;
            case Options.RADON:
                processor = new RADON(0, source, target);
                break;
            case Options.STATIC_RADON:
                processor = new StaticRADON(0, source, target);
                break;
            case Options.CRTREE:
                processor = new CRTree(0, source, target);
                break;
            case Options.PLANE_SWEEP:
                processor = new PlaneSweep(0, source, target, PlaneSweepStructure.LIST_SWEEP);
                break;
            case Options.STRIPE_SWEEP:
                processor = new StripeSTRSweep(0, source, target);
                break;
            case Options.PBSM:
                processor = new PBSM(0, source, target, PlaneSweepStructure.STRIPED_SWEEP);
                break;
            case Options.QUADTREE:
                processor = new QuadTree(0, source, target);
                break;
            case Options.RTREE:
                processor = new RTree(0, source, target);
                break;
            default:
                processor = new GIAnt(0, source, target);
        }
        return processor;
    }

    public static InterlinkingResults run(){
        long time = Calendar.getInstance().getTimeInMillis();
        AbstractBatchAlgorithm processor = getProcessor(algorithm);
        processor.applyProcessing();
        RelatedGeometries relatedGeometries = processor.getResults();
        time = Calendar.getInstance().getTimeInMillis() - time;
        System.out.println("Interlinker completed in " + time+"ms");
        InterlinkingResults newResults = new InterlinkingResults(algorithm, relatedGeometries, source.getSize(), sourceFilename,
                target.getSize(), targetFilename, time, processor.getResultsText());
        results.add(newResults);
        return newResults;
    }


    public static String getConfiguration(String algorithm) {
        try{
            AbstractBatchAlgorithm processor = getProcessor(algorithm);
            return  processor.getMethodInfo();
        }
        catch (UnsupportedOperationException e){
            return "Not supported yet";
        }

    }


    public static void setAlgorithmType(String algorithmType) {
        InterlinkingManager.algorithmType = algorithmType;
    }

    public static void setAlgorithm(String algorithm) {
        InterlinkingManager.algorithm = algorithm;
    }

    public static void setBudget(int budget) {
        InterlinkingManager.budget = budget;
    }

    public static String getAlgorithmType() {
        return algorithmType;
    }

    public static String getAlgorithm() {
        return algorithm;
    }

    public static int getBudget() {
        return budget;
    }
}
