package jackiequiltpatterndeterminaiton;

/**
 * Created by JacquelineLi on 11/26/17.
 */
public final class GenerationInfo {

    private SvgFileProcessor skeletonPathFile;
    private SvgFileProcessor decoElementFile;
    private SvgFileProcessor regionFile;
    private double pointDistributionDist;
    private TreeNode<Point> spanningTree;
    private boolean linearizeCommands;
    private double poissonRadius;

    public GenerationInfo() {
        regionFile = null;
        skeletonPathFile = null;
        decoElementFile = null;
        linearizeCommands = true;
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
}
