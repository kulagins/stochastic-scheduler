package fonda.scheduler.model;

import org.jgrapht.graph.DefaultEdge;

public class MyEdge extends DefaultEdge {

    private float expected;

    private float variance;

    public MyEdge(){

    }

    public void setExpected(float value){
        this.expected = value;
    }

    public float getExpected(){
        return expected;
    }

    public void setVariance(float value){
        this.variance = value;
    }

    public float getVariance(){
        return variance;
    }
}
