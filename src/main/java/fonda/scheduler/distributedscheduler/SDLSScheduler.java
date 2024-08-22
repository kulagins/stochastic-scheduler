package fonda.scheduler.distributedscheduler;

import java.util.*;

import fonda.scheduler.model.*;

//import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
//import org.apache.commons.math3.analysis.UnivariateFunction;
//import org.apache.commons.math3.analysis.integration.*;
//import org.apache.commons.math3.util.Precision;
import org.javatuples.Pair;
import org.jgrapht.graph.*;

import static org.apache.commons.math3.special.Erf.erf;
import static org.apache.commons.math3.special.Erf.erfInv;
//import ;

public class SDLSScheduler {

    private List<DataRow> rawdata;

    private List<MyProcessor> cluster;

    private DirectedAcyclicGraph<MyVertex, MyEdge> graph;

    private String workflow;

    public SDLSScheduler(List<DataRow> rawdata,
                         List<MyProcessor> cluster,
                         DirectedAcyclicGraph<MyVertex,MyEdge> graph,
                         String workflow){

    this.rawdata = rawdata;

    this.cluster = cluster;

    this.graph = graph;

    this.workflow = workflow;

    //Algorithm Returntype: Pair<time,schedule> -> schedule: Pair<Processor,Task>
    //sblevel_calc(cluster,rawdata,graph,workflow); //vorher mit listoflists
    //Pair<Float,List<Pair<MyVertex,MyProcessor>>> results = sdls_schedule(cluster,graph); //vorher listoflists dazw.
    //write results to file/save results for next runs thus can check if schedule exists for certain workflow change write back to include workflowname
    }

    public static void sblevel_calc(//List<List<Object>> listoflists,
                                    List<MyProcessor> cluster,
                                    List<DataRow> rawdata,
                                    DirectedAcyclicGraph<MyVertex, MyEdge> graph,
                                    String workflow){

        Set<MyVertex> tasklist = new HashSet<>(graph.vertexSet());
        //Iterate through Vertexset to find root(should be only task with no outgoing edges)
        MyVertex root = findRoot(tasklist,graph);

        //Construct RevTopList for control remove added Tasks from tasklist, if tasklist empty at the end gj
        List<MyVertex> revtoplist = constructRevToplist(root,graph,tasklist);

        //Calc Sb-Level
        double sb_levelexp = 0;
        double sb_levelvar = 0;
        //Calc w(p)
        double w_p = calcWp(cluster); //vorher calcwp(listoflists)

        //v_exit sb_level
        if (root != null) {
            sb_levelexp = root.getExpected() / w_p;
            root.setSb_levelexp(sb_levelexp);
            sb_levelvar = root.getVariance() / w_p;
            root.setSb_levelvar(sb_levelvar);
        }
        revtoplist.remove(root);

        //while-loop
        while (!revtoplist.isEmpty()){
            MyVertex current_vx = revtoplist.get(0);
            List<Double> vx_wcharlist = new ArrayList<>();

            Set<MyEdge> edgelistpar = graph.outgoingEdgesOf(current_vx);
            for(MyEdge edge : edgelistpar){
                int counttis = 0;
                List<Pair<String,Double>> listOfAncestors = new ArrayList<>();
                MyVertex child = graph.getEdgeTarget(edge);

                double taskinputsize_child = 0;
                List<Double> tasksizelist = new ArrayList<>();
                List<List<Double>> wcharlist = new ArrayList<>();
                Set<MyEdge> edgelistchild = graph.incomingEdgesOf(child);
                for(MyEdge edge2 : edgelistchild){
                    MyVertex ancestor = graph.getEdgeSource(edge2);

                    String name = ancestor.getLabel();
                    double wsum = 0;
                    List<Double> worklist2 = new ArrayList<>();
                    for (DataRow workrow : rawdata) {

                        if (workrow.getTask().equalsIgnoreCase(child.getLabel())
                                && workrow.getWorkflow().equalsIgnoreCase(workflow)
                                && counttis == 0) {
                            taskinputsize_child = taskinputsize_child + workrow.getTaskInputSize();
                            tasksizelist.add(workrow.getTaskInputSize());
                            //System.out.println("tis-child: " + taskinputsize_child);
                        }
                        if (workrow.getTask().equalsIgnoreCase(name)
                                && workrow.getWorkflow().equalsIgnoreCase(workflow)) {
                            wsum = wsum + workrow.getWchar();
                            worklist2.add(workrow.getWchar());
                            //System.out.println("wsum: "+wsum);
                        }
                        if (workrow.getTask().equalsIgnoreCase(current_vx.getLabel())
                                && workrow.getWorkflow().equalsIgnoreCase(workflow)) {
                            vx_wcharlist.add(workrow.getWchar());
                            //System.out.println("vxwchar: "+ workrow.getWchar());
                        }
                    }
                    counttis++;
                    wcharlist.add(worklist2);
                    listOfAncestors.add(new Pair<>(name,wsum));
                }
                double wtotal = 0;
                double w_vx = 0;
                for (Pair<String,Double> pair : listOfAncestors){
                    wtotal = wtotal + pair.getValue1();
                    if (current_vx.getLabel().equals(pair.getValue0())){
                        w_vx = pair.getValue1();
                    }
                }
                //System.out.println("wtotal:"+wtotal);
                double wfraction = 0;
                if (w_vx != 0 && wtotal != 0){
                    wfraction = w_vx/wtotal;
                }
                /*else{
                    System.out.println("wvx: "+ w_vx);
                    System.out.println("wtotal: "+wtotal);
                }*/
                double w_connectexp = taskinputsize_child * wfraction;

                double w_connectvar;
                double w_connectvar_sum = 0;
                double smallestsize =  smallestSize(wcharlist,tasksizelist,vx_wcharlist);
                //System.out.println("connectexp: "+ w_connectexp);

                for (int j = 0; j < smallestsize; j++){
                    double taskinputsizework = tasksizelist.get(j);
                    double currentvx_wchar = vx_wcharlist.get(j);
                    double wchar_divider = 0;
                    for (int k = 0; k < wcharlist.size(); k++){
                        wchar_divider = wchar_divider + wcharlist.get(k).get(j);
                    }

                    double help = (taskinputsizework * (currentvx_wchar/wchar_divider)) - w_connectexp;

                    w_connectvar_sum = w_connectvar_sum + (help * help);
                }

                if (smallestsize != 0 && w_connectvar_sum != 0){
                    w_connectvar = w_connectvar_sum/smallestsize;
                }else {
                    w_connectvar = 0;
                }

                //add weights to edge
                if (child.getLabel().equals("End") || current_vx.getLabel().equals("Start")){
                    edge.setExpected(0);
                    edge.setVariance(0);
                    w_connectexp = 0;
                    w_connectvar = 0;
                }else {
                    edge.setExpected(w_connectexp);
                    edge.setVariance(w_connectvar);
                }
                //System.out.println("wconnect after test:"+ w_connectexp+" "+w_connectvar);
                double child_sblevelexp = child.getSb_levelexp();
                double child_sblevelvar = child.getSb_levelvar();
                double sblevelexp = child_sblevelexp + w_connectexp;

                double sblevelvar = child_sblevelvar + w_connectvar;
                // Comparison of sb_levels
                if (current_vx.getSb_levelexp()== 0 && current_vx.getSb_levelvar() == 0){
                    current_vx.setSb_levelexp(sblevelexp);

                    current_vx.setSb_levelvar(sblevelvar);
                }else{
                    Pair<Double,Double> maxresult = maxValues(current_vx.getSb_levelexp(),sblevelexp,current_vx.getSb_levelvar(),sblevelvar);
                    current_vx.setSb_levelexp(maxresult.getValue0());
                    current_vx.setSb_levelvar(maxresult.getValue1());
                }
            }

            //Add sb_level of task itself to its sb_level
            sb_levelexp = current_vx.getSb_levelexp()+(current_vx.getExpected()/w_p);
            current_vx.setSb_levelexp(sb_levelexp);
            sb_levelvar = current_vx.getSb_levelvar()+(current_vx.getVariance()/w_p);
            current_vx.setSb_levelvar(sb_levelvar);
            //remove Task from revtoplist
            revtoplist.remove(current_vx);
        }
    }

    public static Pair<Double, Double> maxValues(double exp1, double exp2, double var1, double var2){
        double resultExp;
        double resultVar;

        if (exp1 == 0d && exp2 == 0d && var1 == 0d && var2 == 0d){
            return new Pair<>(0d,0d);
        }
        double epsilon =  Math.sqrt(var1 + var2);
        double xi = (exp1 - exp2)/epsilon;

        double psi = ( (1/Math.sqrt(2*Math.PI)) * Math.pow(Math.E, (- 1/2.0*(Math.pow(xi,2.0)))) );

        double help = (double) (xi/Math.sqrt(2));
        double phi = (double) ((erf(help)+1)/2);

        double help2 = (double) (-xi/Math.sqrt(2));
        double nphi = (double) ((erf(help2)+1)/2); //either +1/2 and no minus in front or minus in front and +1/2 check formula !!!
        resultExp = (double) (exp2 + ( (exp1 - exp2)* phi ) + (epsilon* psi));

        resultVar = (double) (( (Math.pow(exp1,2.0) + var1)* phi)
                + ( (Math.pow(exp2,2.0) + var2 )* nphi) //this term is added but with negativ integral result integralresult is a area under a func and should not be negativ becuz negativ area cannot exist
                + ( (exp1 + exp2)*epsilon*psi) //thus nphi should be positiv or negativ presign of 2nd term needed !!!
                - (Math.pow(resultExp,2.0)) );

        double diffexp1 = Math.abs(exp1 - resultExp);
        double diffexp2 = Math.abs(exp2 - resultExp);
        double diffvar1 = Math.abs(var1 - resultVar);
        double diffvar2 = Math.abs(var2 - resultVar);

        if (diffexp1 <= diffexp2){
            if(diffvar1 <= diffvar2){
                return new Pair<>(exp1, var1);
            }else {
                //System.out.println("MaxValues-Wrong values mistake. Take Note!");
                return new Pair<>(exp1,var1);
            }
        }else{
            if(diffvar2 <= diffvar1){
                return new Pair<>(exp2, var2);
            }else {
                //System.out.println("MaxValues-Wrong values mistake. Take Note!");
                return new Pair<>(exp2,var2);
            }
        }
        //return new Pair<>(resultExp, resultVar);
    }

    private static MyVertex findRoot(Set<MyVertex> vertexList, DirectedAcyclicGraph<MyVertex,MyEdge> graph){
        MyVertex root = null;
        for(MyVertex task : vertexList){
            if (graph.outDegreeOf(task) == 0){
                root = task;
                break;
            }
        }
        if (root == null){
            System.out.println("Root was not changed from null.");
        }
        return root;
    }

    private static List<MyVertex> constructRevToplist(MyVertex root,
                                                      DirectedAcyclicGraph<MyVertex,MyEdge> graph,
                                                      Set<MyVertex> tasklist){
        List<MyVertex> revtoplist = new ArrayList<>();
        revtoplist.add(root);
        tasklist.remove(root);
        int i = 0;
        while(!tasklist.isEmpty()) {
            if (i != revtoplist.size()){
            Set<MyEdge> edgelist = graph.incomingEdgesOf(revtoplist.get(i));
            for (MyEdge edge : edgelist){
                MyVertex source = graph.getEdgeSource(edge);
                revtoplist.add(source);
                tasklist.remove(source);
            }
            i++;
            }else {
                break;
            }

        }
        return revtoplist;
    }

    private static double calcWp(List<MyProcessor> cluster){
        double totalspeed = 0;
        double amount = 0;
        for(MyProcessor currproc : cluster){
            double speed = currproc.getProcspeed();
            totalspeed = totalspeed + speed;
            amount++;
        }
        return totalspeed/amount;
    }

    private static double smallestSize(List<List<Double>>wcharlist, List<Double> tasksizelist, List<Double> vx_wcharlist){
        double smallestsize = vx_wcharlist.size();
        if(tasksizelist.size() < smallestsize){
            smallestsize = tasksizelist.size();
        }
        for ( List<Double> worksizelist :wcharlist){
            if (worksizelist.size() < smallestsize){
                smallestsize = worksizelist.size();
            }
        }
        return smallestsize;
    }

    public static Pair<Double,List<Pair<MyVertex,MyProcessor>>> sdls_schedule(
            List<MyProcessor> cluster,
            //List<List<Object>> listofLists,
            DirectedAcyclicGraph<MyVertex,MyEdge> graph
            ){

        Set<MyVertex> tasklist = graph.vertexSet();
        List<MyVertex> taskpool = new ArrayList<>();

        MyVertex entry = findEntry(tasklist, graph);
        taskpool.add(entry);

        double w_p = calcWp(cluster); //listoflists before
        List<Pair<MyVertex, MyProcessor>> schedule = new ArrayList<>();

        while(!taskpool.isEmpty()){
            //MyVertex
            List<SDLentry> listSDL = new ArrayList<>();
            for (MyVertex v_i :taskpool){
                for (MyProcessor processor :cluster){
                    //calc DRT for this task-processor pair (use formula 3 from paper)
                    Pair<Double, Double> drtvalues = getdrt(v_i, graph, schedule, processor);

                    //calcStvalues
                    Pair<Double, Double> stvalues = maxValues(processor.getFtexp(), drtvalues.getValue0(), processor.getFtvar(), drtvalues.getValue1());

                    //calcSDL;
                    Pair<Double, Double> sdlvalues = calculateSDL(v_i, processor, w_p, stvalues.getValue0(), stvalues.getValue1());
                    SDLentry sdlentry = new SDLentry(v_i, processor,sdlvalues.getValue0(), sdlvalues.getValue1(),stvalues.getValue0(),stvalues.getValue1());
                    listSDL.add(sdlentry); //check if list empty after each assign !!!?!?!
                }
            }

            //Stochastic greater than every other SDL//get the task with greatest SDL;
            Pair<MyVertex,MyProcessor> taskprocpair = stochasticGreatest(listSDL);
            schedule.add(taskprocpair);// adds task-processor-pair to the schedule(our assign task to proc)

            MyVertex pushedtask = taskprocpair.getValue0(); //set to stochastic greatest task, remove from taskpool and add to schedule list
            taskpool.remove(pushedtask);
            pushedtask.setPushed(true);

            /*if (pushedtask.getLabel().equalsIgnoreCase("trimgalore")){
                for (SDLentry entryy : listSDL){
                    System.out.println("task: "+entryy.getTaskname().getLabel());
                    System.out.println("proc: "+entryy.getProcname().getProcname());
                    System.out.println("stexp: "+entryy.getStexp());
                    System.out.println("sdlexp: "+entryy.getSdlexp());
                }
                System.out.println("task: "+ pushedtask.getLabel()+ " proc: "+taskprocpair.getValue1().getProcname());
            }*/
            //push unconstrained childs into readypool
            pushChilds(taskpool, pushedtask, graph);

            //update earliest execution start time
            //updateST Funktion schreiben beachten Formeln 1-4 im Paper!!! ?!?!
            //split into FT update and calcST in calcSDL from FT of the processor and the DRT of Task-proc-pair
            MyProcessor chosenproc = taskprocpair.getValue1();
            Pair<Double,Double> stvalues = findST(taskprocpair,listSDL);
            double completiontimeexp;
            double completiontimevar = stvalues.getValue1() + pushedtask.getVariance()/ chosenproc.getProcspeed() ;
            if (pushedtask.getLabel().equalsIgnoreCase("Start") || pushedtask.getLabel().equalsIgnoreCase("End")){
                completiontimeexp = chosenproc.getFtexp() + 0; //add nothing if start or end
            }else {
                if (pushedtask.getExpected() == 0){
                    completiontimeexp = stvalues.getValue0() + 60000/chosenproc.getProcspeed(); //set exp for task missing data (1min)
                }else {
                    completiontimeexp = stvalues.getValue0() + pushedtask.getExpected() / chosenproc.getProcspeed();
                }
            }
            //update the FT of the chosenproc and update the cttime for the pushedtask
            chosenproc.setFtexp(completiontimeexp);
            chosenproc.setFtvar(completiontimevar);

            //System.out.println("ctvar:"+completiontimevar);
            pushedtask.setCTtimeexp(completiontimeexp);
            pushedtask.setCTtimevar(completiontimevar);
            System.out.println("task:"+pushedtask.getLabel()+ " proc: "+chosenproc.getProcname());
            //System.out.println("completionexp:"+completiontimeexp);
            //System.out.println("task added-time: "+ pushedtask.getExpected()/chosenproc.getProcspeed());
            //updateST(cluster, graph, pushedtask, taskprocpair.getValue1(),taskpool);
        }
        double makespan = findmakespan(cluster); //change to completiontime for last task thus => makespan = finishtime
        resetCluster(cluster); //return cluster to start state after finishing the calculation for one workflow
        return new Pair<>(makespan, schedule);
    }

    private static MyVertex findEntry(Set<MyVertex> tasklist, DirectedAcyclicGraph<MyVertex,MyEdge> graph){
        MyVertex entry = null;
        for(MyVertex task : tasklist){
            if (graph.inDegreeOf(task) == 0){
                entry = task;
                break;
            }
        }
        if (entry == null){
            System.out.println("Root was not changed from null.");
        }
        return entry;
    }
    public static Pair<Double,Double> calculateSDL( MyVertex vertex, MyProcessor processor, double w_p, double stexp, double stvar){
        double varcompcapExp = calcVaryCompcap(vertex.getExpected(), w_p, processor);
        double varcompcapVar = calcVaryCompcap(vertex.getVariance(), w_p, processor);
        //System.out.println("sbexp: "+vertex.getSb_levelexp());
        //System.out.println("wp:" + w_p);
        double sdlexp = vertex.getSb_levelexp() - stexp + varcompcapExp;//prüfen ob ST bei beiden gleich und ob/wie rechnungen anpassen
        double sdlvar = vertex.getSb_levelvar() - stvar + varcompcapVar;
        return new Pair<>(sdlexp,sdlvar);
    }

    public static double calcVaryCompcap(double v_i, double w_p, MyProcessor processor){
        if (v_i == 0d){
            v_i = 1;
        }
        double result = (v_i/w_p) - (v_i/ processor.getProcspeed());
        //System.out.println("result: "+result);
        return result;
    }

    public static Pair<MyVertex, MyProcessor> stochasticGreatest(List<SDLentry> listSDL){
        MyVertex greatestname = null;
        Double greatestx = null;
        MyProcessor greatestproc = null;
        for (SDLentry currentry : listSDL){
            double my;
            double sigma;
            if (currentry.getTaskname().getLabel().equals("Start") || currentry.getTaskname().getLabel().equals("End")){
                my = Math.abs(currentry.getSdlexp());
                sigma = Math.sqrt(Math.abs(currentry.getSdlvar()));
            }else {
                my = currentry.getSdlexp();
                sigma = Math.sqrt(currentry.getSdlvar());
            }

            double errfctterm = 1 + ((0.9*sigma*Math.sqrt(2*Math.PI)*Math.sqrt(2))/(-Math.sqrt(Math.PI)*sigma));

            double x = (erfInv(errfctterm)*2*sigma)-(Math.sqrt(2)*my)/(-Math.sqrt(2));

            if (greatestx == null || greatestx < x ){
                greatestx = x;
                greatestname = currentry.getTaskname();
                greatestproc = currentry.getProcname();
            }
        }
        return new Pair<>(greatestname,greatestproc);
    }

    public static void pushChilds(List<MyVertex> taskpool, MyVertex pushedtask, DirectedAcyclicGraph<MyVertex,MyEdge> graph) {
        //System.out.println("vertex:"+ pushedtask.getLabel());
        Set<MyEdge> outedges = new HashSet<>();
        if (graph.outDegreeOf(pushedtask) != 0){
            outedges = graph.outgoingEdgesOf(pushedtask);
        }
        /*
        try {
            outedges = graph.outgoingEdgesOf(pushedtask);
        }catch (Exception e){
            System.out.println("something went wrong in pushchilds edges!");
        }*/

        for (MyEdge edge : outedges) {
            MyVertex child = graph.getEdgeTarget(edge);
            Set<MyEdge> childancestors = graph.incomingEdgesOf(child);

            boolean allpushed = true;
            for (MyEdge childedge : childancestors) {
                MyVertex ancestor = graph.getEdgeSource(childedge);

                if (!ancestor.getPushed()) {
                    allpushed = false;
                    break;
                }
            }
            if (allpushed) {
                taskpool.add(child);
            }
        }

    }

    public static Pair<Double,Double> getdrt(MyVertex currtask,
                                           DirectedAcyclicGraph<MyVertex,MyEdge> graph,
                                           List<Pair<MyVertex, MyProcessor>> schedule,
                                           MyProcessor currproc){
        double drtexp;
        double drtvar;
        double ctexp = 0;
        double ctvar = 0;
        MyVertex biggestct = null;
        //get Ancestors of current task
        Set<MyEdge> inedges = graph.incomingEdgesOf(currtask);
        //get biggest completiontime (comp all ancestors)
        for (MyEdge edgeancestor : inedges){
            MyVertex ancestor = graph.getEdgeSource(edgeancestor);
            Pair<Double,Double> ctvalues = maxValues(ctexp,ancestor.getCTtimeexp(),ctvar, ancestor.getCTtimevar());
            ctexp = ctvalues.getValue0();
            ctvar = ctvalues.getValue1();
            if (ctvalues.getValue0() == ancestor.getCTtimeexp() && ctvalues.getValue1() == ancestor.getCTtimevar()){
                biggestct = ancestor;
            }
        }
        //check if on same proc: if yes: drt = 0 ,no: drt = cttime + edge
        MyProcessor ancestorproc = new MyProcessor("emptyname", 0);
        for (Pair<MyVertex,MyProcessor> taskprocpair : schedule){
            if (taskprocpair.getValue0().equals(biggestct)){
                ancestorproc = taskprocpair.getValue1();
            }
        }
        if (ancestorproc.equals(currproc) || ancestorproc.getProcname().equals("emptyname")){
            drtexp = 0;
            drtvar = 0;
        }else {
            drtexp = ctexp;
            drtvar = ctvar;
        }
        return new Pair<>(drtexp,drtvar);
    }

    public static Pair<Double,Double> findST(Pair<MyVertex,MyProcessor> taskprocpair, List<SDLentry> sdllist){
        double stexp = 0;
        double stvar = 0;
        for (SDLentry currentry : sdllist){
            if (currentry.getTaskname().equals(taskprocpair.getValue0()) && currentry.getProcname().equals(taskprocpair.getValue1())){
                stexp = currentry.getStexp();
                stvar = currentry.getStvar();
            }
        }
        return new Pair<>(stexp, stvar);
    }

    private static double findmakespan(List<MyProcessor> cluster){
        double makespan = 0;
        for (MyProcessor currproc : cluster){
            if (currproc.getFtexp() > makespan){
                makespan = currproc.getFtexp();
            }
        }
        return makespan;
    }

    private static void resetCluster(List<MyProcessor> cluster){
        for (MyProcessor currproc: cluster){
            currproc.setFtexp(0);
        }
    }

}
