package fonda.scheduler.model;

import org.jgrapht.graph.DefaultEdge;

public class MyEdge extends DefaultEdge {

    private double expected;

    private double variance;

    public MyEdge(){

    }

    public void setExpected(double value){
        this.expected = value;
    }

    public double getExpected(){
        return expected;
    }

    public void setVariance(double value){
        this.variance = value;
    }

    public double getVariance(){
        return variance;
    }
}
