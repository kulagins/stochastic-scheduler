package fonda.scheduler.model;

public class SDLentry {

    public MyVertex taskname;

    public MyProcessor processor;

    public double sdlexp;

    public double sdlvar;

    public double stexp;

    public double stvar;

    public SDLentry(MyVertex task, MyProcessor processor, double sdlexp, double sdlvar, double stexp, double stvar){
        this.taskname = task;
        this.processor = processor;
        this.sdlexp = sdlexp;
        this.sdlvar = sdlvar;
        this.stexp = stexp;
        this.stvar = stvar;
    }

    public MyVertex getTaskname(){
        return taskname;
    }

    public MyProcessor getProcname(){
        return processor;
    }

    //public void setSdlexp(float sdlexp) {
    //    this.sdlexp = sdlexp;
    //}
    public double getSdlexp(){
        return this.sdlexp;
    }

    //public void setSdlvar(float sdlvar){
    //    this.sdlvar = sdlvar;
    //}
    public double getSdlvar(){
        return this.sdlvar;
    }

    public double getStexp(){return this.stexp;}
    public double getStvar(){return this.stvar;}
}
