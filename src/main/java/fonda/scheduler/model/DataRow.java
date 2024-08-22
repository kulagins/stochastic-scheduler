package fonda.scheduler.model;

public class DataRow {
    private final String machine;

    private final String workflow;

    private final String task;

    private final double realtime;

    private final double taskinputsize;

    private final double wchar;

    public DataRow(String machine, String workflow, String task, double realtime, double taskinputsize, double wchar){

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

    public double getRealtime(){
        return realtime;
    }

    public double getTaskInputSize(){
        return taskinputsize;
    }

    public double getWchar(){
        return wchar;
    }

}
