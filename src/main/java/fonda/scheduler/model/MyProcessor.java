package fonda.scheduler.model;

public class MyProcessor {

    public String procname;

    public double procspeed;

    //public float stexp;

    //public float stvar;

    public double ftexp;

    public double ftvar;

    public MyProcessor(String name, double speed){
        this.procname = name;
        this.procspeed = speed;
        this.ftexp = 0;
        this.ftvar = 0;
    }

    //public void setProcname(String name){
    //    this.procname = name;
    //}
    public String getProcname(){
        return procname;
    }

    //public void setProcspeed(float speed){
    //    this.procspeed = speed;
    //}
    public double getProcspeed(){
        return procspeed;
    }

    public void setFtexp(double finishexp){
        this.ftexp = finishexp;
    }
    public double getFtexp(){
        return ftexp;
    }

    public void setFtvar(double finishvar){
        this.ftvar = finishvar;
    }
    public double getFtvar(){
        return ftvar;
    }
}
