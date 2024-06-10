package fonda.scheduler.model;

public class DataRow {
    private final String machine;

    private final String workflow;

    private final String task;

    private final float realtime;

    private final float taskinputsize;

    private final float wchar;

    public DataRow(String machine, String workflow, String task, float realtime, float taskinputsize, float wchar){

        this.machine = machine;

        this.workflow = workflow;

        this.task = task;

        this.realtime = realtime;

        this.taskinputsize = taskinputsize;

        this.wchar = wchar;

    }

    public String getMachine(){
        return machine;
    }

    public String getWorkflow(){
        return workflow;
    }

    public String getTask(){
        return task;
    }

    public float getRealtime(){
        return realtime;
    }

    public float getTaskInputSize(){
        return taskinputsize;
    }

    public float getWchar(){
        return wchar;
    }

}
