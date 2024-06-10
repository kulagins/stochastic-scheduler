package fonda.scheduler.controller;

import fonda.scheduler.labeller.CurrentPodNodeStatus;
import fonda.scheduler.model.DataRow;
import fonda.scheduler.model.MyProcessor;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.javatuples.Pair;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        List<MyProcessor> cluster = readCluster();

        Pair<List<List<Object>>,List<DataRow>> listpair = readIn(cluster);
        List<List<Object>> listofLists;
        listofLists = listpair.getValue0();
        List<DataRow> rawdata;
        rawdata = listpair.getValue1();

        KubernetesClient client = new DefaultKubernetesClient();

        CurrentPodNodeStatus nodeInformation = new CurrentPodNodeStatus(client, listofLists, rawdata, cluster);

    }

    //readin of cluster-structure
    public static List<MyProcessor>  readCluster(){
        List<MyProcessor> cluster = new ArrayList<>();
        try {
            FileReader in = new FileReader("src/main/resources/node_description.csv");
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line=br.readLine()) != null){
                String [] cpuvalues = line.split(",");

                String machinename = cpuvalues[0];
                int amount = 0;
                try{
                    if (!cpuvalues[1].equals("Amount")){
                        amount = Integer.parseInt(cpuvalues[1]);
                    }
                }catch (Exception e){
                    System.out.println("Amount of machine is not a int-value.(Impossible). Error-value:" + cpuvalues[1]);
                    continue;
                }
                float cpuspeed = 0;
                try{
                    if (!cpuvalues[2].equals("CPU")){
                        cpuspeed = Float.parseFloat(cpuvalues[2]);
                    }
                }catch (Exception e){
                    System.out.println("Processorspeed is not a float value. Error 205");
                    continue;
                }
                MyProcessor processor = new MyProcessor(machinename,cpuspeed);
                for (int i = 0; i < amount; i++){
                    cluster.add(processor);
                }

            }
        }catch (IOException e){
            System.out.println("IOException in Node-Description readin.");
        }
        return cluster;
    }

    //readin data.csv returns listoflists and rawdata-list
    public static Pair<List<List<Object>>,List<DataRow>> readIn(List<MyProcessor> cluster){
        float cpuspeed = 0;

        List<List<Object>> listofLists = new ArrayList<>();

        List<DataRow> rawdata = new ArrayList<>();
        float sum_e;
        float expected = 0;
        float variance = 0;

        try{
            FileReader in2 = new FileReader("src/main/resources/data.csv");
            BufferedReader br2 = new BufferedReader(in2);
            String line2;
            //System.out.println("Reader erfolgreich.");
            while((line2 = br2.readLine()) != null){
                //Unterscheidung anhand von Task und Maschine

                String [] values = line2.split(",");

                String machine = values[0];
                String workflow = values[2];
                String task = values[3];
                if (machine.equals("Machine") && workflow.equals("Workflow") && task.equals("Task")){
                    continue;
                }
                float TaskinputSize = 0;

                try {
                    if (!values[10].equals("TaskInputSize")) {
                        TaskinputSize = Float.parseFloat(values[10]);
                    }
                }catch (Exception e){
                    System.out.print("Error on Taskinputsize parsing. Error 201"+ values[10]);
                    break;
                    //continue;
                }
                //Cpumatching based on clusterlist
                for (MyProcessor workproc : cluster) {
                    if (machine.toLowerCase().equals(workproc.getProcname().toLowerCase())) {
                        cpuspeed = workproc.getProcspeed();
                        break;
                    }
                }

                float real = 0;
                try{
                    if (!values[8].equals("Real")){
                        real = Float.parseFloat(values[8]);
                    }
                }
                catch(Exception e){
                    System.out.println("Error on realtime parsing. Error 202");
                    break;
                }

                float rxpx = real*cpuspeed; //realtime right now zero producing errors
                int runs = 1;
                sum_e = rxpx;//when first run

                float wchar = 0;
                try{
                    if (!values[13].equals("wchar")){
                        wchar = Float.parseFloat(values[13]);
                    }
                }
                catch (Exception e){
                    System.out.println("Error on wchar parsing. Error 203");
                    break;
                }

                DataRow row = new DataRow(machine,workflow, task, rxpx, TaskinputSize, wchar);

                rawdata.add(row);

                List<Object> innerList = new ArrayList<Object>();
                innerList.add(machine);//0
                innerList.add(workflow);//1
                innerList.add(task);//2
                innerList.add(runs);//3
                innerList.add(sum_e);//4
                innerList.add(expected);//5
                innerList.add(variance);//6
                innerList.add(cpuspeed);//7

                List<Object> workList = new ArrayList<Object>();

                //Add first entry when list is empty
                if(listofLists.isEmpty()){
                    listofLists.add(innerList);
                    continue;
                }

                //Checking if entry is in list already if so alter entry(add info)
                for (int i = 0; i <= listofLists.size(); i++){
                    if(i < listofLists.size()) {
                        workList = listofLists.get(i);
                        if (machine.equals(workList.get(0)) &&
                                workflow.equals(workList.get(1)) &&
                                task.equals(workList.get(2))) {

                            int cur_runs = (int) workList.get(3);
                            cur_runs += 1;
                            workList.set(3, cur_runs);

                            float k = (float) workList.get(4);
                            k = k + rxpx;
                            workList.set(4, k);

                            //add list back at correct spot
                            listofLists.set(i, workList);
                            break;
                        }
                    }else {
                        //adding to listoflists if no match found
                        listofLists.add(innerList);
                        break;
                    }
                }
            }
        }
        catch(IOException e){
            System.out.println("IOException error in Data.csv read.");
        }

        List<Object> workList = new ArrayList<Object>();
        //Expected(5) & Variance(6) for all entries:
        for(int j = 0; j < listofLists.size(); j++){
            workList = listofLists.get(j);
            expected = calcExp(workList);
            workList.set(5,expected);

            variance = calcVar(workList,rawdata);
            workList.set(6,variance);

            listofLists.set(j,workList);
        }

        return new Pair<>(listofLists,rawdata);
    }

    //calculates the expected value of worklist-values
    public static float calcExp(List<Object> workList ){
        if(workList == null){
            System.out.println("Worklist is null. Returned 0.");
            return 0;
        }
        float expected;
        float expect_sum;
        try {
            expect_sum = (float) workList.get(4);
        }catch (NullPointerException E){
            System.out.println("Nullpointerexception in calcExp. Returned 0.");
            return 0;
        }
        int run_sum = (int)workList.get(3);
        if(run_sum == 0){
            return 0;
        }
        expected = expect_sum/run_sum;
        return expected;
    }

    //calculates the variance of worklist-values
    public static float calcVar(List<Object> workList, List<DataRow> rawdata){
        if (workList == null || rawdata == null){
            System.out.println("Worklist or rawdata is null. Returned 0.");
            return 0;
        }
        float help;
        float help2 = 0;//counter for runs
        float sum_v = 0;
        float variance;
        float expected;
        try{
            expected = (float)workList.get(5);
        }catch(NullPointerException E){
            System.out.println("Expected-value is null in calcVar. Returned 0.");
            return 0;
        }
        int run_sum = (int)workList.get(3);
        if (run_sum == 0){
            return 0;
        }
        for(int k = 0; k < rawdata.size(); k++) {
            if (help2 == run_sum) {
                help = 0;
                help2 = 0;
                break;
            }
            String machine = (String) workList.get(0);
            String workflow = (String) workList.get(1);
            String task = (String) workList.get(2);

            if (machine.equals(rawdata.get(k).getMachine())
                && task.equals(rawdata.get(k).getTask())
                && workflow.equals(rawdata.get(k).getWorkflow())
            ) {
                help = rawdata.get(k).getRealtime() - expected;
                sum_v = sum_v + (help * help);
                help2 = help2 + 1;
            }
        }
        variance = sum_v/run_sum;
        return variance;
    }
}
