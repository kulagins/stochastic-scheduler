package fonda.scheduler.model;

public class SDLentry {

    public MyVertex taskname;

    public MyProcessor processor;

    public float sdlexp;

    public float sdlvar;

    public float stexp;

    public float stvar;

    public SDLentry(MyVertex task, MyProcessor processor, float sdlexp, float sdlvar, float stexp, float stvar){
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

    public void setSdlexp(float sdlexp) {
        this.sdlexp = sdlexp;
    }
    public float getSdlexp(){
        return this.sdlexp;
    }

    public void setSdlvar(float sdlvar){
        this.sdlvar = sdlvar;
    }
    public float getSdlvar(){
        return this.sdlvar;
    }

    public float getStexp(){return this.stexp;}
    public float getStvar(){return this.stvar;}
}
