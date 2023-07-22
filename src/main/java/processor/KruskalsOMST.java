package processor;

import draw.DrawPDF;
import parser.DocParser;
import parser.Document;
import shapes.Master;
import shapes.Path;
import shapes.Point;
import shapes.Slave;

import javax.print.Doc;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @auther lianmeng
 * @create 21.07.23
 */
public class KruskalsOMST {

    private final DocParser parser;
    private Document document;

    public KruskalsOMST(String path, String outputPath) throws FileNotFoundException {
        this.parser = new DocParser();
        processToOutPutKruskalsIIC(path, outputPath);

    }


    public void processToOutPutKruskalsIIC(String path, String outputPath) throws FileNotFoundException {
        parser.parseInputToDocument(path);
        this.document = parser.getParseDoc();
        String[] names = path.split("/");
        this.document.setName(names[names.length - 1]);
        processToOutPutKruskalsIIC(this.document);
        DrawPDF drawPDF = new DrawPDF(this.document, outputPath);
        drawPDF.outputToPdf();

    }

    public void processToOutPutKruskalsIIC(Document document){
        Master master = document.getMaster();
        Point masterPoint = new Point(master.getX_ct(), master.getY_ct());
        ArrayList<Slave> slaves = document.getSlaves();
        ArrayList<Point> slavePoints = new ArrayList<>();
        for (Slave slave : slaves){
            Point point = new Point(slave.getX_ct(), slave.getY_ct());
            slavePoints.add(point);
        }

        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < slavePoints.size(); i++) {
            for (int j = i + 1; j < slavePoints.size(); j++) {
                edges.add(new Edge(slavePoints.get(i), slavePoints.get(j)));
            }
        }
        Collections.sort(edges, Comparator.comparingDouble(e -> e.weight));

        List<Edge> mst = new ArrayList<>();
        Point closestSlave = null;
        double minDistance = Double.MAX_VALUE;
        for (Point slavePoint : slavePoints) {
            double distance = new Edge(masterPoint, slavePoint).weight;
            if (distance < minDistance) {
                closestSlave = slavePoint;
                minDistance = distance;
            }
        }
        mst.add(new Edge(masterPoint, closestSlave));
        closestSlave.degree++;

        int numNodes = 1 + slaves.size(); // Master + Slaves
        Subset subsets[] = new Subset[numNodes];
        for (int i = 0; i < numNodes; i++){
            subsets[i] = new Subset(i, 0);
        }
        int j = 0;
        while (mst.size() < numNodes - 1){
            Edge nextEdge = edges.get(j);

            int x = findRoot(subsets, slavePoints.indexOf(nextEdge.src));
            int y = findRoot(subsets, slavePoints.indexOf(nextEdge.dest));

            if (x!=y && nextEdge.src.degree < 2 && nextEdge.dest.degree < 2){
                mst.add(nextEdge);
                nextEdge.src.degree ++;
                nextEdge.dest.degree ++;
                union(subsets, x, y);
            }
            j++;
        }

        System.out.println(
                "Following are the edges of the constructed MST:");
        double minCost = 0;
        for (Edge e : mst) {
            System.out.println(e.src + " -- "
                    + e.dest + " == "
                    + e.weight);
            minCost += e.weight;
        }
        System.out.println("Total cost of MST: " + minCost);

        ArrayList<Path> paths = new ArrayList<>();
        for (Edge edge : mst){
            paths.add(new Path(edge.src, edge.dest));
        }
        document.setPaths(paths);

    }

    private static int findRoot(Subset[] subsets, int i) {
        if (subsets[i].parent == i)
            return subsets[i].parent;

        subsets[i].parent = findRoot(subsets, subsets[i].parent);
        return subsets[i].parent;
    }



    //Defines edge structure
    static class Edge {
        Point src;
        Point dest;
        double weight;

        public Edge(Point src, Point dest) {
            this.src = src;
            this.dest = dest;
            this.weight = calculateDistance(src, dest);
        }

        private double calculateDistance(Point p1, Point p2) {
            double xDiff = p1.x - p2.x;
            double yDiff = p1.y - p2.y;
            return Math.ceil(Math.sqrt(xDiff * xDiff + yDiff * yDiff));
        }
    }

    // Defines subset element structure
    static class Subset {
        public int parent, rank;

        public Subset(int parent, int rank) {
            this.parent = parent;
            this.rank = rank;
        }
    }

    // Function to unite two disjoint sets
    private static void union(Subset[] subsets, int x,
                              int y) {
        int rootX = findRoot(subsets, x);
        int rootY = findRoot(subsets, y);

        if (subsets[rootY].rank < subsets[rootX].rank) {
            subsets[rootY].parent = rootX;
        } else if (subsets[rootX].rank
                < subsets[rootY].rank) {
            subsets[rootX].parent = rootY;
        } else {
            subsets[rootY].parent = rootX;
            subsets[rootX].rank++;
        }
    }









}
