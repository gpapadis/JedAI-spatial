package dit.anonymous.webapp.execution.er;

import java.util.List;

import dit.anonymous.webapp.utilities.WorkflowDetailsManager;
import org.javatuples.Triplet;
import org.scify.jedai.blockbuilding.IBlockBuilding;
import org.scify.jedai.blockprocessing.IBlockProcessing;
import org.scify.jedai.datamodel.AbstractBlock;
import org.scify.jedai.datamodel.AttributeClusters;
import org.scify.jedai.datamodel.EntityProfile;
import org.scify.jedai.utilities.BlocksPerformance;
import org.scify.jedai.utilities.datastructures.AbstractDuplicatePropagation;

import dit.anonymous.webapp.utilities.configurations.Options;

public class BlockUtils {
    
         /**
     * Run a block building method and return its blocks
     *
     * @param er_mode     Entity Resolution type
     * @param clusters   Clusters from schema clustering, if applicable (can be null)
     * @param profilesD1 List of profiles from the 1st dataset
     * @param profilesD2 List of profiles from the 2nd dataset
     * @param bb         Block building method instance to get blocks with
     * @return List of blocks generated by block building method
     */
    public static List<AbstractBlock> runBlockBuilding(String er_mode, AttributeClusters[] clusters,
                                                 List<EntityProfile> profilesD1, List<EntityProfile> profilesD2,
                                                 IBlockBuilding bb) {
        if (er_mode.equals(Options.DIRTY_ER)) {
            if (clusters == null) {
                // Dirty ER without schema clustering
                return bb.getBlocks(profilesD1);
            } else {
                // Dirty ER with schema clustering
                return bb.getBlocks(profilesD1, null, clusters);
            }
        } else {
            if (clusters == null) {
                // Clean-clean ER without schema clustering
                return bb.getBlocks(profilesD1, profilesD2);
            } else {
                // Clean-clean ER with schema clustering
                return bb.getBlocks(profilesD1, profilesD2, clusters);
            }
        }
    }
    
    
    
    /**
     * Process blocks using a given block processing method
     *
     * @param duProp        Duplicate propagation (from ground-truth)
     * @param finalRun      Set to true to print clusters performance
     * @param blocks        Blocks to process
     * @param currentMethod Method to process the blocks with
     * @return Processed list of blocks
     */
    public static Triplet<List<AbstractBlock>, BlocksPerformance, Double> runBlockProcessing(AbstractDuplicatePropagation duProp, boolean finalRun,
    List<AbstractBlock> blocks, IBlockProcessing currentMethod, WorkflowDetailsManager details_manager) {
        double overheadStart;
        double overheadEnd;
        BlocksPerformance blp = null;
        overheadStart = System.currentTimeMillis();

        if (!blocks.isEmpty()) {
            blocks = currentMethod.refineBlocks(blocks);
            overheadEnd = System.currentTimeMillis();

            if (finalRun && WorkflowManager.ground_truth != null) {
                // Print blocks performance
                blp = new BlocksPerformance(blocks, duProp);
                blp.setStatistics();
                details_manager.print_BlockBuildingPerformance(blp, (float)((overheadEnd - overheadStart)/1000), currentMethod.getMethodConfiguration(),  currentMethod.getMethodName());
                
            }
            return new Triplet<>(blocks, blp, (overheadEnd - overheadStart)/1000);
        }
        return new Triplet<>(blocks, null, 0.0);
    }  


     /**
     * Get total comparisons that will be made for a list of blocks
     *
     * @param blocks List of blocks
     * @return Number of comparisons
     */
    public static float getTotalComparisons(List<AbstractBlock> blocks) {
        float originalComparisons = 0f;
        originalComparisons = blocks.stream()
                .map(AbstractBlock::getNoOfComparisons)
                .reduce(originalComparisons, (accumulator, _item) -> accumulator + _item);
        System.out.println("Original comparisons\t:\t" + originalComparisons);
        return originalComparisons;
    }


    /**
     * Optimize a given block processing method randomly using the given list of blocks.
     * Modifies the original block processing object and sets it to use the best found
     * random configuration.
     *
     * @param bp     Block processing method object
     * @param blocks Blocks to optimize with
     * @param random If true will use random search, otherwise grid
     */
    public static void optimizeBlockProcessing(IBlockProcessing bp, List<AbstractBlock> blocks, boolean random) {
        List<AbstractBlock> cleanedBlocks;
        double bestA = 0;
        int bestIteration = 0;
        double originalComparisons = getTotalComparisons(blocks);

        int iterationsNum = random ? WorkflowManager.NO_OF_TRIALS : bp.getNumberOfGridConfigurations();
        for (int j = 0; j < iterationsNum; j++) {
            if (random) {
                bp.setNextRandomConfiguration();
            } else {
                bp.setNumberedGridConfiguration(j);
            }
            cleanedBlocks = bp.refineBlocks(blocks);
            if (cleanedBlocks.isEmpty()) {
                continue;
            }

            BlocksPerformance blp = new BlocksPerformance(cleanedBlocks, WorkflowManager.ground_truth);
            blp.setStatistics();
            double recall = blp.getPc();
            double rr = 1 - blp.getAggregateCardinality() / originalComparisons;
            double a = rr * recall;
            if (bestA < a) {
                bestIteration = j;
                bestA = a;
            }
        }
        System.out.println("\n\nBest iteration\t:\t" + bestIteration);
        System.out.println("Best performance\t:\t" + bestA);

        if (random) {
            bp.setNumberedRandomConfiguration(bestIteration);
        } else {
            bp.setNumberedGridConfiguration(bestIteration);
        }
    }

}