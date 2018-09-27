package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 11/26/17.
 */
public final class GenerationInfo {

    public Main.SkeletonRenderType skeletonRenderType;
    private SvgFileProcessor skeletonPathFile;
    private SvgFileProcessor decoElementFile;
    private SvgFileProcessor regionFile;
    private ConvexHullBound regionConvexHull;
    private SvgFileProcessor collisionFile;
    private double pointDistributionDist;
    private TreeNode<Point> spanningTree;
    private boolean linearizeCommands;
    private List<TreeTraversal.NodeType> nodeType;
    private boolean drawBound;
    private double poissonRadius;
    private double initialLength;
    private double randomFactor;
    private double decorationSize = 0.0, decorationGap, initialAngle;
    private double decoElmentScalingFactor = 1.0;
    private List<SvgPathCommand> collisionCommands = new ArrayList<>();
    public GenerationInfo() {
        regionFile = null;
        skeletonPathFile = null;
        decoElementFile = null;
        linearizeCommands = true;
    }

    public double getInitialLength() {
        return initialLength;
    }

    public void setInitialLength(Double initialLength) {
        this.initialLength = initialLength;
    }

    public ConvexHullBound getRegionConvexHull() {
        if (regionConvexHull == null)
            regionConvexHull = ConvexHullBound.fromCommands(regionFile.getCommandList());
        return regionConvexHull;
    }

    public List<TreeTraversal.NodeType> getNodeType() {
        return nodeType;
    }

    public void setNodeType(List<TreeTraversal.NodeType> nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isDrawBound() {
        return drawBound;
    }

    public List<SvgPathCommand> getCollisionCommands() {
        return collisionCommands;
    }

    public void setCollisionCommands(List<SvgPathCommand> collisionCommands) {
        this.collisionCommands = collisionCommands;
    }

    public SvgFileProcessor getCollisionFile() {
        return collisionFile;
    }

    public void setCollisionFile(SvgFileProcessor collisionFile) {
        this.collisionFile = collisionFile;
    }

    public double getDecorationSize() {
        return decorationSize;
    }

    public void setDecorationSize(double decorationSize) {
        this.decorationSize = decorationSize;
    }

    public double getDecorationGap() {
        return decorationGap;
    }

    public void setDecorationGap(double decorationGap) {
        if (this.decorationSize < 0.01)
            this.decorationGap = decorationGap;
        else
            this.decorationGap = decorationGap * this.decorationSize;
    }

    public double getInitialAngle() {
        return initialAngle;
    }

    public void setInitialAngle(double initialAngle) {
        this.initialAngle = initialAngle;
    }

    public double getDecoElmentScalingFactor() {
        return decoElmentScalingFactor;
    }

    public void setDecoElmentScalingFactor(double decoElmentScalingFactor) {
        this.decoElmentScalingFactor = decoElmentScalingFactor;
    }

    public double getPoissonRadius() {
        return poissonRadius;
    }

    public void setPoissonRadius(double poissonRadius) {
        this.poissonRadius = poissonRadius;
    }

    public boolean isLinearizeCommands() {
        return linearizeCommands;
    }

    public void setLinearizeCommands(boolean linearizeCommands) {
        this.linearizeCommands = linearizeCommands;
    }

    public TreeNode<Point> getSpanningTree() {
        return spanningTree;
    }

    public void setSpanningTree(TreeNode<Point> spanningTree) {
        this.spanningTree = spanningTree;
    }

    public SvgFileProcessor getSkeletonPathFile() {
        return skeletonPathFile;
    }

    public void setSkeletonPathFile(SvgFileProcessor skeletonPathFile) {
        this.skeletonPathFile = skeletonPathFile;
    }

    public SvgFileProcessor getDecoElementFile() {
        return decoElementFile;
    }

    public void setDecoElementFile(SvgFileProcessor decoElementFile) {
        this.decoElementFile = decoElementFile;
    }

    public SvgFileProcessor getRegionFile() {
        return regionFile;
    }

    public void setRegionFile(SvgFileProcessor regionFile) {
        this.regionFile = regionFile;
    }

    public double getPointDistributionDist() {
        return pointDistributionDist;
    }

    public void setPointDistributionDist(double pointDistributionDist) {
        this.pointDistributionDist = pointDistributionDist;
    }

    public boolean getDrawBound() {
        return drawBound;
    }

    public void setDrawBound(boolean drawBound) {
        this.drawBound = drawBound;
    }

    public double getRandomFactor() {
        return randomFactor;
    }

    public void setRandomFactor(Double randomFactor) {
        this.randomFactor = randomFactor;
    }
}
