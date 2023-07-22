package parser;

import shapes.Keepout;
import shapes.Master;
import shapes.Point;
import shapes.Slave;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class DocParser {
    private Document parseDoc;

    public DocParser() {
        this.parseDoc = new Document();
    }

    public void processInput(String[] input){
        /*
        Read files
         */
        for (int i = 0; i < input.length; ++i){
            if (input[i].equals("")) continue;

            else if (input[i].equals("Uni_Keepouts")){
                i++;
                int uni_o_cnt = 0;
                while (!input[i].equals("FIN")){
                    uni_o_cnt++;
                    String[] coordinates = input[i].split(" ");
                    Keepout o = new Keepout(("o" + uni_o_cnt), Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]), Integer.parseInt(coordinates[3]));
                    Point lowerLeft = new Point(o.getMinX(), o.getMinY());
                    Point lowerRight = new Point(o.getMaxX(), o.getMinY());
                    Point upperLeft = new Point(o.getMinX(), o.getMaxY());
                    Point upperRight = new Point(o.getMaxX(), o.getMaxY());
                    o.corners = new ArrayList<>(Arrays.asList(lowerLeft, lowerRight, upperLeft, upperRight));

                    if (Integer.parseInt(coordinates[4]) == 1){
                        o.corners.get(0).canbeBypass = false;
                    }
                    if (Integer.parseInt(coordinates[5]) == 1){
                        o.corners.get(1).canbeBypass = false;
                    }
                    if (Integer.parseInt(coordinates[6]) == 1){
                        o.corners.get(2).canbeBypass = false;
                    }
                    if (Integer.parseInt(coordinates[7]) == 1){
                        o.corners.get(3).canbeBypass = false;
                    }

                    parseDoc.addToUni_keepouts(o);
                    i++;
                }
            }
            else if (input[i].equals("Master")){
                i++;
                String[] coordinates = input[i].split(" ");
                Master master = new Master(Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
                master.setName(coordinates[0]);
                parseDoc.setMaster(master);
            }
            else if (input[i].equals("Slave")){
                i++;
                while (!input[i].equals("FIN")){
                    String[] coordinates = input[i].split(" ");
                    Slave sv = new Slave(Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
                    sv.setName(coordinates[0]);
                    parseDoc.addToSlaves(sv);
                    i++;
                }
            }



        }

    }

    private String[] readFiles(String path) {
        File dir = new File(path);
        File[] files = dir.listFiles();
        ArrayList<String> allLines = new ArrayList<>();
        ArrayList<String> lines;
        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                allLines.addAll(lines);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return allLines.toArray(new String[]{});
    }

    public void parseInputToDocument(String path) {
        this.processInput(this.readFiles(path));
    }

    public Document getParseDoc() {
        return parseDoc;
    }
}
