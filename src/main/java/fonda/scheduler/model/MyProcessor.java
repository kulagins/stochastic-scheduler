package fonda.scheduler.model;

public class MyProcessor {

    public String procname;

    public float procspeed;

    //public float stexp;

    //public float stvar;

    public float ftexp;

    public float ftvar;

    public MyProcessor(String name, float speed){
        this.procname = name;
        this.procspeed = speed;
        //this.stexp = 0;
        //this.stvar = 0;
        this.ftexp = 0;
        this.ftvar = 0;
    }

    public void setProcname(String name){
        this.procname = name;
    }
    public String getProcname(){
        return procname;
    }

    public void setProcspeed(float speed){
        this.procspeed = speed;
    }
    public float getProcspeed(){
        return procspeed;
    }

    /*public void setStexp(float startexp){
        this.stexp = startexp;
    }
    public float getStexp(){
        return stexp;
    }

    public void setStvar(float startvar){
        this.stvar = startvar;
    }
    public float getStvar(){
        return stvar;
    }*/

    public void setFtexp(float finishexp){
        this.ftexp = finishexp;
    }
    public float getFtexp(){
        return ftexp;
    }

    public void setFtvar(float finishvar){
        this.ftvar = finishvar;
    }
    public float getFtvar(){
        return ftvar;
    }
}
