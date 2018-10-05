package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 11/26/17.
 */
public final class GenerationInfo {

    public static final int WANDERER_NEIGHBORHOOD_COLLISION = 2;
    SvgFileProcessor skeletonPathFile;
    SvgFileProcessor decoElementFile;
    SvgFileProcessor regionFile;
    SvgFileProcessor collisionFile;
    Main.SkeletonPathType skeletonPathType;
    Main.SkeletonRenderType skeletonRenderType;

    ConvexHullBound regionConvexHull;

    boolean linearizeCommands;
    boolean drawBound;

    double pointDistributionDist;
    double poissonRadius;
    double initialLength;
    double randomFactor;
    double decorationDensity = 0.0;
    double decorationSize = 0.0, decorationGap, initialAngle;
    TreeNode<Point> spanningTree;
    List<TreeTraversal.NodeType> nodeType;
    List<SvgPathCommand> collisionCommands = new ArrayList<>();

    public GenerationInfo() {
        regionFile = null;
        skeletonPathFile = null;
        decoElementFile = null;
        linearizeCommands = true;
    }

    public ConvexHullBound getRegionConvexHull() {
        if (regionConvexHull == null)
            regionConvexHull = ConvexHullBound.fromCommands(regionFile.getCommandList());
        return regionConvexHull;
    }

    public void setDecorationGap(double decorationGap) {
        if (this.decorationSize < 0.01)
            this.decorationGap = decorationGap;
        else
            this.decorationGap = decorationGap * this.decorationSize;
    }

    public void setPoissonRadius(double poissonRadius) {
        this.poissonRadius = poissonRadius;
    }

    String getParameterString() {
        String region = "";
        String skeleton = "";
        String render = "";
        String deco = "";
        String ans = "";
        String parameter = "";
        String pointDensity = "";
        if (regionFile != null)
            region = regionFile.getfFileName();

        if (decoElementFile != null)
            deco = decoElementFile.getfFileName();
        else
            deco = "NO_DECO";

        if (skeletonPathType != null) {
            skeleton = skeletonPathType.toString();
            if (skeletonPathType.isTreeStructure())
                pointDensity = String.format("pointDensity_%.1f", pointDistributionDist);
        }

        if (skeletonRenderType != null) {
            render = skeletonRenderType.toString();
            switch (skeletonRenderType) {
                case WANDERER:
                    parameter = String.format("decoDensity_%.1f_decoSize_%.1f_gapLen_%.1f", decorationDensity, decorationSize, decorationGap);
            }
        }


        ans = String.format("%s-%s-%s-%s-%s-%s", region, deco, skeleton, render, pointDensity, parameter);


        return ans;
    }

}
