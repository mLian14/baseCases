package parser;

import shapes.*;

import java.util.ArrayList;

public class Document {
    private String name;
    private Master master;
    private ArrayList<Slave> slaves;
    private ArrayList<Keepout> keepouts;

    private ArrayList<Path> paths;

    private ArrayList<Node> steinerPoints;


    public Document() {
        this.slaves = new ArrayList<>();
        this.keepouts = new ArrayList<>();
        this.paths = new ArrayList<>();
        this.steinerPoints = new ArrayList<>();
    }

    public ArrayList<Node> getSteinerPoints() {
        return steinerPoints;
    }

    public void setSteinerPoints(ArrayList<Node> steinerPoints) {
        this.steinerPoints = steinerPoints;
    }

    public ArrayList<Path> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<Path> paths) {
        this.paths = paths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Master getMaster() {
        return master;
    }

    public void setMaster(Master master) {
        this.master = master;
    }

    public ArrayList<Slave> getSlaves() {
        return slaves;
    }

    public void addToSlaves(Slave slave) {
        this.slaves.add(slave);
    }

    public ArrayList<Keepout> getKeepouts() {
        return keepouts;
    }

    public void addToUni_keepouts(Keepout o) {
        this.keepouts.add(o);
    }


}
