package processor;

import draw.DrawPDF;
import parser.DocParser;
import parser.Document;
import shapes.*;

import javax.print.Doc;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.*;


/**
 * @auther lianmeng
 * @create 20.07.23
 */
public class RSMT {

    private final DocParser parser;
    private Document document;

    public RSMT(String path, String outputPath) throws FileNotFoundException {
        this.parser = new DocParser();
        processToOutputIIC(path, outputPath);
    }

    public void processToOutputIIC(String path, String outputPath) throws FileNotFoundException {
        parser.parseInputToDocument(path);
        this.document = parser.getParseDoc();
        String[] names = path.split("/");
        this.document.setName(names[names.length - 1]);
        processToOutputIIC(this.document);
        DrawPDF drawPDF = new DrawPDF(this.document, outputPath);
        drawPDF.outputToPdf();
    }
    /**
     * The modified Version ov DW, obtain the optimal solution w.r.t. the daisy-chained Network Topology
     * All the coordinates are DOUBLE Type
     * @param document
     */
    public void processToOutputIIC(Document document){

        Master master = document.getMaster();
        ArrayList<Slave> slaves = document.getSlaves();

        ArrayList<Node> terminals = new ArrayList<>();
        //Node_0 is the master
        Node ms_node = new Node(master.getX_ct(), master.getY_ct(), 0, NodeType.Master);

        for (int i = 0; i < slaves.size(); ++i) {
            Node n = new Node(slaves.get(i).getX_ct(), slaves.get(i).getY_ct(), i + 1, NodeType.Terminal);
            terminals.add(n);
        }

        int t_cnt = slaves.size(); //number of terminals

        /*
         * Graph G = (N,A)
         * nodes contains all nodes in the graph
         */
        ArrayList<Node> nodes = new ArrayList<>();

        /*
         * We generate all nodes besides the terminals
         */
        steinerNodesGenerator(terminals, t_cnt, nodes, ms_node);

        /*ArrayList<Node> steiner_nodes = new ArrayList<>(nodes);
        steiner_nodes.removeAll(terminals);*/

        int node_cnt = nodes.size();
        System.out.println("node_cnt" + node_cnt);
        System.out.println(nodes);
        document.setSteinerPoints(nodes);

        /*
         * The Matrix d of shortest lengths
         * d_i,j = d_j,i = length of the shortest path between nodes i and j in N
         */
        double[][] d = new double[node_cnt + 1][node_cnt + 1];
        for (int i = 1; i <= node_cnt; ++i) {
            for (int j = 1; j <= node_cnt; ++j) {
                d[i][j] = add_BD(Math.abs(sub_BD(nodes.get(i - 1).getX_exact(), nodes.get(j - 1).getX_exact())), Math.abs(sub_BD(nodes.get(i - 1).getY_exact(), nodes.get(j - 1).getY_exact())));

            }
        }
        d[0][0] = 0.0;
        for (int i = 1; i <= node_cnt; ++i) {
            d[i][0] = d[0][i] = add_BD(Math.abs(nodes.get(i - 1).getX_exact() - master.getX_ct()), Math.abs(nodes.get(i - 1).getY_exact() - master.getY_ct()));
        }

        //debug
        /*for (int i = 1; i <= node_cnt; ++i) {
            for (int j = 1; j <= node_cnt; ++j) {
                System.out.print(d[i][j] + " ");
            }
            System.out.println();
        }*/


        /*
         * Algorithm A: (Computes the length of the Steiner tree connecting Y, and assigns this length to variable "v")
         * v is our MasterIC
         * In the paper: Y = {terminals}, C = Y - {v};
         * here: C = {terminals}, v = MasterIC
         */
        ArrayList<Value_S> Ss = new ArrayList<>();
        /*
         * For each t in C do
         *  For each J in N do
         *      S[{t},J] <- D(t,J);
         */
        for (Node t : terminals) {
            for (Node J : nodes) {
                Value_S tmpS = new Value_S();
                tmpS.addToSetD(t.getNum());
                tmpS.setSecondArg_Int(J.getNum());
                tmpS.setValue(d[t.getNum()][J.getNum()]);
                Ss.add(tmpS);

            }
        }
        //System.out.println("Ss = " + Ss);


        /*
         * Generate all the subsets of C, with cardinality >= 2
         */
        ArrayList<Integer> terminal_Int = new ArrayList<>();
        for (int i = 0; i < t_cnt; ++i) {
            terminal_Int.add(i + 1);
        }
        Set<ArrayList<Integer>> Ds = subsetsGenerator(terminal_Int, 2, t_cnt);

        for (int m = 2; m <= t_cnt - 1; ++m) {
            System.out.println("m = " + m);
            for (ArrayList<Integer> D : Ds) {
                if (D.size() == m) {
                    System.out.println("D = " + D);
                    for (Node I : nodes) {
                        Value_S tmpS = new Value_S();
                        tmpS.setFirstArg_Array(D);
                        tmpS.setSecondArg_Int(I.getNum());
                        Ss.add(tmpS);
                    }

                    Set<ArrayList<Integer>> Es = subsetsGenerator(D, 1, D.size() - 1);
                    System.out.println("Es=" + Es);
                    Set<ArrayList<Integer>> EsCopy = new HashSet<>(Es);
                    for (ArrayList<Integer> E : EsCopy) {
                        if (E.size() > 1 && E.size() < D.size() - 1) {
                            Es.remove(E);
                        }
                    }
                    System.out.println("Es.size()= " + Es.size());
                    //System.out.println("Es" + Es);
                    for (Node J : nodes) {
                        //System.out.println("Jnum= " + J.getNum());
                        double u = Double.MAX_VALUE;
                        /*
                         * for each E such that (D[1] in E) and (E proper subset of D) do
                         *      u <- min (u, S[E,J] + S[D-E,J])
                         */
                        ArrayList<Integer> tmpD_E = new ArrayList<>();
                        ArrayList<Integer> tmpD_without_E = new ArrayList<>();
                        for (ArrayList<Integer> E : Es) {
                            if (E.contains(D.get(0))) {
                                //System.out.println("E = " + E);
                                ArrayList<Integer> D_without_E = new ArrayList<>(D);
                                D_without_E.removeAll(E);
                                //System.out.println("D_without_E = " + D_without_E);
                                double S_E_J = Double.MAX_VALUE, S_D_without_E_J = Double.MAX_VALUE;
                                for (Value_S s : Ss) {
                                    ArrayList<Integer> setOfs = new ArrayList<>(s.getFirstArg_Array());
                                    if (setOfs.containsAll(E) && E.containsAll(setOfs) && s.getSecondArg_Int() == J.getNum()) {
                                        S_E_J = s.getValue();
                                        //System.out.println("S_E_J = " + S_E_J);
                                    } else if (setOfs.containsAll(D_without_E) && D_without_E.containsAll(setOfs) && s.getSecondArg_Int() == J.getNum()) {
                                        S_D_without_E_J = s.getValue();
                                        //System.out.println("S_D_without_E_J = " + S_D_without_E_J);
                                    }
                                }
                                double previous_u = u;
                                u = Math.min(u, add_BD(S_E_J, S_D_without_E_J));
                                //System.out.println("u" + u);
                                if (u < previous_u) { //u is updated
                                    tmpD_E = new ArrayList<>(E);
                                    tmpD_without_E = new ArrayList<>(D_without_E);
                                }
                            }
                        }
                        /*
                         * for each I in N do
                         *      S[D, I] <- min (S[D,I], D(I,J) + u)
                         */
                        //System.out.println("Ss = " + Ss);
                        for (Node I : nodes) {
                            for (Value_S s : Ss) {
                                ArrayList<Integer> setOfs = new ArrayList<>(s.getFirstArg_Array());
                                if (setOfs.containsAll(D) && D.containsAll(setOfs) && s.getSecondArg_Int() == I.getNum()) {
                                    double previous_sValue = s.getValue();
                                    s.setValue(Math.min(s.getValue(), add_BD(d[I.getNum()][J.getNum()], u)));
                                    if (s.getValue() < previous_sValue) { //s.Value updated
                                        s.setD_E(tmpD_E);
                                        s.setD_without_E(tmpD_without_E);
                                        s.setJ_Num(J.getNum());
                                    }

                                }
                            }

                        }
                        //System.out.println("Ss = " + Ss);


                    }

                }

            }


        }

        /*
         * (15) -- (20)
         */
        double v = Double.MAX_VALUE;
        //We retrieve the last J (we only give the last solution)
        int p_Num = 0;
        for (Node J : nodes) {
            //System.out.println("J.Num = " + J.getNum());
            double u = Double.MAX_VALUE;
            Set<ArrayList<Integer>> Es = subsetsGenerator(terminal_Int, 1, t_cnt - 1);
            for (ArrayList<Integer> E : Es) {
                if (E.contains(terminals.get(0).getNum())) {
                    /*
                     * u <- min (u, S[E,J] + S[C-E,J])
                     * for each J, we need to store the E and C-E such that the u achieves the Min.
                     */
                    //System.out.println("E = " + E);
                    ArrayList<Integer> C_without_E = new ArrayList<>(terminal_Int);
                    C_without_E.removeAll(E);

                    //System.out.println("C_without_E = " + C_without_E);

                    double S_E_J = Double.MAX_VALUE, S_C_without_E_J = Double.MAX_VALUE;
                    for (Value_S s : Ss) {
                        ArrayList<Integer> setOfs = new ArrayList<>(s.getFirstArg_Array());
                        if (setOfs.containsAll(E) && E.containsAll(setOfs) && s.getSecondArg_Int() == J.getNum()) {
                            S_E_J = s.getValue();
                            //System.out.println("S_E_J = " + S_E_J);
                        } else if (setOfs.containsAll(C_without_E) && C_without_E.containsAll(setOfs) && s.getSecondArg_Int() == J.getNum()) {
                            S_C_without_E_J = s.getValue();
                            //System.out.println("S_C_without_E_J = " + S_C_without_E_J);

                        }

                    }

                    double previous_u = u;
                    u = Math.min(u, add_BD(S_E_J, S_C_without_E_J));
                    if (u < previous_u) {//u is updated
                        J.setC_E(E);
                        J.setC_without_E(C_without_E);
                    }

                    /*System.out.println("u" + u);
                    System.out.println("S+S = " + add_BD(S_E_J, S_C_without_E_J));
                    System.out.println("J.Num = " + J.getNum());
                    System.out.println("J.E = " + J.getC_E());
                    System.out.println("J.C_without_E = " + J.getC_without_E());*/

                }

            }

            /*
             * v <- min (v, D(q,J) + u)
             * here: q is our masterIC
             */
            //System.out.println("u = " + u);

            double previous_v = v;
            v = Math.min(v, add_BD(d[0][J.getNum()], u));

            if (v < previous_v) { //updated v
                p_Num = J.getNum();
            }
        }

        System.err.println("p_Num = " + p_Num);
        System.err.println("v = " + v);
        //System.out.println("Ss = " + Ss);

        /*
         * Retrieve from the Algorithm-A
         */
        System.out.println(nodes);
        //p_node is the point that connect the MasterIC and the restGroup
        Node p_node = nodes.get(p_Num - 1);
        ArrayList<Integer> p_C_E = p_node.getC_E();
        System.out.println("p_C_E= " + p_C_E);
        ArrayList<Integer> p_C_without_E = p_node.getC_without_E();
        System.out.println("p_C_without_E= " + p_C_without_E);
        Path_Tree path_tree = new Path_Tree(ms_node);
        Path_Tree.T_Node p_Tnode = new Path_Tree.T_Node(p_node);
        path_tree.getMaster().addToChildren(p_Tnode);
        ArrayList<Path_Tree.T_Node> t_nodes = new ArrayList<>();
        retrieve_S_tree(nodes, Ss, p_Num, p_C_E, path_tree, p_Tnode, t_nodes);
        retrieve_S_tree(nodes, Ss, p_Num, p_C_without_E, path_tree, p_Tnode, t_nodes);
        /*
         * Retrieve Data from path_tree
         */
        ArrayList<Path> paths = new ArrayList<>();
        Path path = new Path(new Point(master.getX_ct(), master.getY_ct()), new Point(p_node.getX_exact(), p_node.getY_exact()));
        paths.add(path);
        for (Path_Tree.T_Node t_node : t_nodes) {
            System.out.println("t_node= " + t_node);
            if (t_node.getChildren() != null) {
                for (Path_Tree.T_Node child_tnode : t_node.getChildren()) {
                    path = new Path(new Point(t_node.x, t_node.y), new Point(child_tnode.x, child_tnode.y));
                    if (!contained(paths, path)){
                        paths.add(path);
                    }
                    System.out.println("child=" + child_tnode);
                }
            }
            if (t_node.getParent() != null) {
                path = new Path(new Point(t_node.getParent().x, t_node.getParent().y), new Point(t_node.x, t_node.y));

                if (!contained(paths, path)){
                    paths.add(path);
                }

                System.out.println("parent=" + t_node.getParent());
            }




        }

//        /*
//        Detect opposite relations between each path
//         */
//        ArrayList<Keepout> obstacles = document.getKeepouts();
//        ArrayList<Point> points = new ArrayList<>();
//        for (Path tmpPath : paths){
//            Point src = tmpPath.startPoint;
//            Point dest = tmpPath.endPoint;
//            if (!points.contains(src)){
//                points.add(src);
//            }
//            if (!points.contains(dest)){
//                points.add(dest);
//            }
//        }
//        for (Point point : points){
//            for (Keepout obstacle : obstacles){
//                basicBinaryVariables(point, obstacle);
//            }
//        }
//        ArrayList<Path> copy_path = new ArrayList<>(paths);
//        for (Path targetPath : copy_path){
//            Point src = targetPath.startPoint;
//            Point dest = targetPath.endPoint;
//            for (Keepout obstacle : obstacles){
//                //l->r || r->l || t->b || b->t
//                if ((src.getPseudo_oRel_qs().get(obstacle)[4] == 1 && dest.getPseudo_oRel_qs().get(obstacle)[5] == 1) ||
//                        (src.getPseudo_oRel_qs().get(obstacle)[5] == 1 && dest.getPseudo_oRel_qs().get(obstacle)[4] == 1)||
//                        (src.getPseudo_oRel_qs().get(obstacle)[6] == 1 && dest.getPseudo_oRel_qs().get(obstacle)[7] == 1)||
//                        (src.getPseudo_oRel_qs().get(obstacle)[7] == 1 && dest.getPseudo_oRel_qs().get(obstacle)[6] == 1)){
//
//                    ArrayList<Double> detours = new ArrayList<>();
//                    int minCnt = -1;
//                    for (int c_cnt = 0; c_cnt < obstacle.corners.size(); ++c_cnt){
//                        Point corner = obstacle.corners.get(c_cnt);
//                        if (!corner.canbeBypass){
//                            detours.add(Double.POSITIVE_INFINITY);
//                        }
//                        else {
//                            detours.add(dist(src, corner) + dist(corner, dest));
//                        }
//                        double minDetour = Collections.min(detours);
//                        minCnt = detours.indexOf(minDetour);
//                    }
//                    Point bypassCorner = obstacle.corners.get(minCnt);
//                    //delete and add
//                    paths.remove(targetPath);
//                    Path p1 = new Path(src, bypassCorner);
//                    Path p2 = new Path(bypassCorner, dest);
//                    paths.add(p1);
//                    paths.add(p2);
//
//
//
//                }
//
//            }
//
//
//        }
//
        //recompute the final wire length
        int length = 0;
        for (Path subpath : paths){
            length += dist(subpath.startPoint, subpath.endPoint);
        }
        System.out.println("withoutObstaclesLength= " + length);




        document.setPaths(paths);

    }


    private void retrieve_S_tree(ArrayList<Node> nodes, ArrayList<Value_S> Ss, int current_Num, ArrayList<Integer> set, Path_Tree path_tree, Path_Tree.T_Node current_Tnode, ArrayList<Path_Tree.T_Node> t_nodes) {
        if (set.size() > 1) {
            for (Value_S s : Ss) {
                if (s.getFirstArg_Array().containsAll(set) && set.containsAll(s.getFirstArg_Array()) && s.getSecondArg_Int() == current_Num) {

                    int next_Num_L = s.getJ_Num();
                    Path_Tree.T_Node nextNode = new Path_Tree.T_Node(nodes.get(next_Num_L - 1));
                    nextNode.setParent(current_Tnode);
                    t_nodes.add(nextNode);
                    ArrayList<Integer> S_L = s.getD_E();
                    ArrayList<Integer> S_R = s.getD_without_E();
                    retrieve_S_tree(nodes, Ss, next_Num_L, S_L, path_tree, nextNode, t_nodes);
                    retrieve_S_tree(nodes, Ss, next_Num_L, S_R, path_tree, nextNode, t_nodes);

                }
            }
        } else {
            if (set.get(0) != current_Num) {
                Node cildnode = nodes.get(set.get(0) - 1);
                Path_Tree.T_Node child = new Path_Tree.T_Node(cildnode.getNum(), cildnode.getX_exact(), cildnode.getY_exact(), current_Tnode, null);
                current_Tnode.addToChildren(child);
                t_nodes.add(child);
            }
        }
    }

    private Set<ArrayList<Integer>> subsetsGenerator(ArrayList<Integer> intArray, int min_cnt, int max_cnt) {
        Set<ArrayList<Integer>> subsets = new HashSet<>();
        int cnt = intArray.size();
        for (int i = 1; i < (1 << cnt); i++) {
            ArrayList<Integer> tmpArray = new ArrayList<>();
            // Print current subset
            for (int j = 0; j < cnt; j++) {
                if ((i & (1 << j)) > 0) {
                    tmpArray.add(intArray.get(j));
                }
            }
            if (tmpArray.size() >= min_cnt && tmpArray.size() <= max_cnt) {
                subsets.add(tmpArray);
            }
        }
        //System.out.println(subsets);
        return subsets;

    }


    private void steinerNodesGenerator(ArrayList<Node> set, int node_cnt, ArrayList<Node> nodes, Node ms_node) {
        for (Node tn : set) {
            nodes.add(tn);
        }
        ArrayList<Node> set_copy = new ArrayList<>(set);
        set_copy.add(ms_node);
        for (int i = 0; i < set_copy.size(); ++i) {
            double tmpX = set_copy.get(i).getX_exact();
            //System.out.println(tmpX);
            for (int j = 0; j < set_copy.size(); ++j) {
                if (j != i) {
                    double tmpY = set_copy.get(j).getY_exact();
                    Node tmpN = new Node(tmpX, tmpY, NodeType.SteinerNode);

                    if (!set_copy.contains(tmpN)) {
                        tmpN.setNum(nodes.size() + 1);
                        //System.out.println(tmpN);
                        nodes.add(tmpN);
                    } else {
                        System.out.println("This Steiner Point is in Terminals");
                    }
                }

            }
        }
    }

    /**
     * 提供精確的加法運算。
     *
     * @param value1 被加數
     * @param value2 加數
     * @return 兩個引數的和
     */
    public static Double add_BD(Double value1, Double value2) {
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精確的減法運算。
     *
     * @param value1 被減數
     * @param value2 減數
     * @return 兩個引數的差
     */
    public static double sub_BD(Double value1, Double value2) {
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        return b1.subtract(b2).doubleValue();
    }

    public double dist(Point p1, Point p2){
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p1.y);
    }

    public boolean contained (ArrayList<Path> paths, Path cPath){

        for (Path path : paths){
            if (path.startPoint.equals(cPath.startPoint) && path.endPoint.equals(cPath.endPoint)){
                return true;
            }
        }
        return false;
    }

    private void basicBinaryVariables(Point base, Keepout o) {

        /*
        oDir_q: UL, UR, LR, LL, L, R, T, B
         */
        int[] odir_q = new int[8];
        //UL
        if (base.getY() < base.getX() + o.getMaxY() - o.getMinX()) {
            odir_q[0] = 0;
        } else
            odir_q[0] = 1;
        //UR
        if (base.getY() <= -base.getX() + o.getMaxY() + o.getMaxX()) {
            odir_q[1] = 0;
        } else
            odir_q[1] = 1;
        //LR
        if (base.getY() <= base.getX() + o.getMinY() - o.getMaxX()) {
            odir_q[2] = 1;
        } else
            odir_q[2] = 0;
        //LL
        if (base.getY() < -base.getX() + o.getMinY() + o.getMinX()) {
            odir_q[3] = 1;
        } else
            odir_q[3] = 0;
        //L
        if (base.getX() < o.getMinX()) {
            odir_q[4] = 0;
        } else
            odir_q[4] = 1;
        //R
        if (base.getX() > o.getMaxX()) {
            odir_q[5] = 0;
        } else
            odir_q[5] = 1;
        //T
        if (base.getY() > o.getMaxY()) {
            odir_q[6] = 0;
        } else
            odir_q[6] = 1;
        //B
        if (base.getY() < o.getMinY()) {
            odir_q[7] = 0;
        } else
            odir_q[7] = 1;
        base.addToPseudo_oDir_qs(o, odir_q);

        /*
        oRel_q:
         */
        int[] orel_q = new int[8];
        //UpperLeft
        if (odir_q[3] + odir_q[1] + odir_q[4] * odir_q[6] == 0) {
            orel_q[0] = 1;
        } else orel_q[0] = 0;
        //UpperRight
        if (odir_q[0] + odir_q[2] + odir_q[5] * odir_q[6] == 0) {
            orel_q[1] = 1;
        } else orel_q[1] = 0;
        //LowerLeft
        if (odir_q[0] + odir_q[2] + odir_q[4] * odir_q[7] == 0) {
            orel_q[2] = 1;
        } else orel_q[2] = 0;
        //LowerRight
        if (odir_q[1] + odir_q[3] + odir_q[5] * odir_q[7] == 0) {
            orel_q[3] = 1;
        } else orel_q[3] = 0;

        //d_Left
        if (odir_q[5] + odir_q[6] + odir_q[7] == 3) {
            orel_q[4] = 1;
        } else orel_q[4] = 0;
        //d_Right
        if (odir_q[4] + odir_q[6] + odir_q[7] == 3) {
            orel_q[5] = 1;
        } else orel_q[5] = 0;
        //d_Top
        if (odir_q[4] + odir_q[5] + odir_q[7] == 3) {
            orel_q[6] = 1;
        } else orel_q[6] = 0;
        //d_Bottom
        if (odir_q[4] + odir_q[5] + odir_q[6] == 3) {
            orel_q[7] = 1;
        } else orel_q[7] = 0;


        base.addToPseudo_oRel_qs(o, orel_q);

    }

}
