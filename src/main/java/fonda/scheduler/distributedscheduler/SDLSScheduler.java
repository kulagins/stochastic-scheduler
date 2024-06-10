package fonda.scheduler.distributedscheduler;

import java.text.DecimalFormat;
import java.util.*;

import fonda.scheduler.model.*;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.*;
import org.apache.commons.math3.util.Precision;
import org.javatuples.Pair;
import org.jgrapht.graph.*;
import org.apache.commons.math3.*;

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
        float sb_levelexp = 0;
        float sb_levelvar = 0;
        //Calc w(p)
        float w_p = calcWp(cluster); //vorher calcwp(listoflists)

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
            List<Float> vx_wcharlist = new ArrayList<>();
            //System.out.println("currentvx:"+current_vx.getLabel());
            Set<MyEdge> edgelistpar = graph.outgoingEdgesOf(current_vx);
            for(MyEdge edge : edgelistpar){
                int counttis = 0;
                List<Pair<String,Float>> listOfAncestors = new ArrayList<>();
                MyVertex child = graph.getEdgeTarget(edge);
                //System.out.println("child:"+child.getLabel());
                float taskinputsize_child = 0;
                List<Float> tasksizelist = new ArrayList<>();
                List<List<Float>> wcharlist = new ArrayList<>();
                Set<MyEdge> edgelistchild = graph.incomingEdgesOf(child);
                for(MyEdge edge2 : edgelistchild){
                    MyVertex ancestor = graph.getEdgeSource(edge2);
                    //System.out.println("ancestor:"+ancestor.getLabel());
                    String name = ancestor.getLabel();
                    float wsum = 0;
                    List<Float> worklist2 = new ArrayList<>();
                    for (DataRow workrow : rawdata) {
                        if (workrow.getTask().equals(child.getLabel())
                                && workrow.getWorkflow().equals(workflow)
                                && counttis == 0) {
                            taskinputsize_child = taskinputsize_child + workrow.getTaskInputSize();
                            tasksizelist.add(workrow.getTaskInputSize());
                        }
                        if (workrow.getTask().equals(name)
                                && workrow.getWorkflow().equals(workflow)) {
                            wsum = wsum + workrow.getWchar();
                            worklist2.add(workrow.getWchar());
                        }
                        if (workrow.getTask().equals(current_vx.getLabel())
                                && workrow.getWorkflow().equals(workflow)) {
                            vx_wcharlist.add(workrow.getWchar());
                        }
                    }
                    counttis++;
                    wcharlist.add(worklist2);
                    listOfAncestors.add(new Pair<>(name,wsum));
                }
                float wtotal = 0;
                float w_vx = 0;
                for (Pair<String,Float> pair : listOfAncestors){
                    wtotal = wtotal + pair.getValue1();
                    if (current_vx.getLabel().equals(pair.getValue0())){
                        w_vx = pair.getValue1();
                    }
                }
                //System.out.println("wvx:"+w_vx);
                //System.out.println("wtotal:"+wtotal);
                //System.out.println("tischild:"+taskinputsize_child); //fail check were wrong
                float w_connectexp = taskinputsize_child * (w_vx/wtotal);
                //System.out.println("wconnectexp:"+w_connectexp);
                float w_connectvar;
                float w_connectvar_sum = 0;
                float smallestsize =  smallestSize(wcharlist,tasksizelist,vx_wcharlist);

                for (int j = 0; j < smallestsize; j++){ //change to suit worstcase only smallest n calculations!!
                    float taskinputsizework = tasksizelist.get(j);
                    float currentvx_wchar = vx_wcharlist.get(j);
                    float wchar_divider = 0;
                    for (int k = 0; k < wcharlist.size(); k++){
                        wchar_divider = wchar_divider + wcharlist.get(k).get(j);
                    }
                    //System.out.println("tiswork:"+taskinputsizework);
                    //System.out.println("currentvxchar:"+currentvx_wchar);
                    //System.out.println("wchardividertotal:"+wchar_divider);
                    float help = (taskinputsizework * (currentvx_wchar/wchar_divider)) - w_connectexp;
                    //System.out.println("help:"+help);
                    w_connectvar_sum = w_connectvar_sum + (help * help);
                }
                w_connectvar = w_connectvar_sum/smallestsize;
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
                //
                float child_sblevelexp = child.getSb_levelexp();
                float child_sblevelvar = child.getSb_levelvar();
                float sblevelexp = child_sblevelexp + w_connectexp;
                //System.out.println("Sblevelexp with connectexp: "+ sblevelexp+ "without: "+ child_sblevelexp);
                float sblevelvar = child_sblevelvar + w_connectvar;
                // Comparison of sb_levels
                if (current_vx.getSb_levelexp()== 0 && current_vx.getSb_levelvar() == 0){
                    current_vx.setSb_levelexp(sblevelexp);
                    //System.out.println("sblevelexp without node itself set to:"+sblevelexp);
                    current_vx.setSb_levelvar(sblevelvar);
                }else{
                    Pair<Float,Float> maxresult = maxValues(current_vx.getSb_levelexp(),sblevelexp,current_vx.getSb_levelvar(),sblevelvar);
                    float diffexp1 = Math.abs(sblevelexp - maxresult.getValue0());
                    float diffexp2 = Math.abs(current_vx.getSb_levelexp() - maxresult.getValue0());
                    float diffvar1 = Math.abs(sblevelvar - maxresult.getValue1());
                    float diffvar2 = Math.abs(current_vx.getSb_levelvar() - maxresult.getValue1());
                    //System.out.println("maxvalue 1: "+ maxresult.getValue0());
                    //System.out.println("maxvalue 2: "+ maxresult.getValue1());
                    //System.out.println("diffexp1:"+diffexp1);
                    //System.out.println("diffexp2:"+diffexp2);
                    //System.out.println("diffvar1:"+diffvar1);
                    //System.out.println("diffvar2:"+diffvar2);
                    if (diffexp1 <= diffexp2){
                        if(diffvar1 <= diffvar2){
                            current_vx.setSb_levelexp(sblevelexp);
                            current_vx.setSb_levelvar(sblevelvar);
                        }else {
                            current_vx.setSb_levelvar(sblevelvar);
                            System.out.println("MaxValues-Wrong values mistake. Take Note!");
                        }
                    }else{
                        System.out.println("Nothing changes!");
                    }
                    /*
                    if (diffexp1 < diffexp2 && diffvar2 < diffvar1 || diffexp1 > diffexp2 && diffvar1 < diffvar2){
                        System.out.println("Error in maxValue comparison. Mixed results. Error 406.");
                    }*/
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

    public static Pair<Float, Float> maxValues(float exp1, float exp2, float var1, float var2){
        float resultExp;
        float resultVar;

        if (exp1 == 0f && exp2 == 0f && var1 == 0f && var2 == 0f){
            return new Pair<>(0f,0f);
        }
        double epsilon =  Math.sqrt(var1 + var2);
        double xi = (exp1 - exp2)/epsilon;
        //System.out.println("xi :"+ xi);
        double psi = ( (1/Math.sqrt(2*Math.PI)) * Math.pow(Math.E, (- 1/2.0*(Math.pow(xi,2.0)))) );
        //System.out.println("psi :"+ psi);
        float help = (float) (xi/Math.sqrt(2));
        float phi = (float) ((erf(help)+1)/2);
        //System.out.println("minuserf-value: "+erf(-xi/Math.sqrt(2)) );
        float help2 = (float) (-xi/Math.sqrt(2));
        float nphi = (float) ((erf(help2)+1)/2); //either +1/2 and no minus in front or minus in front and +1/2 check formula !!!
        resultExp = (float) (exp2 + ( (exp1 - exp2)* phi ) + (epsilon* psi));
        //System.out.println("nphi:"+nphi);
        //System.out.println("Values from maxValues:");
        resultVar = (float)(( (Math.pow(exp1,2.0) + var1)* phi)
                + ( (Math.pow(exp2,2.0) + var2 )* nphi) //this term is added but with negativ integral result integralresult is a area under a func and should not be negativ becuz negativ area cannot exist
                + ( (exp1 + exp2)*epsilon*psi) //thus nphi should be positiv or negativ presign of 2nd term needed !!!
                - (Math.pow(resultExp,2.0)) );
        //System.out.println("phi:"+phi);
        float term1 = (float) ((Math.pow(exp1,2)+var1) *phi);
        //System.out.println("first term:"+ term1);

        float term2 = (float) ((Math.pow(exp2,2)+var2) *nphi);
        //System.out.println("nphi:"+nphi);
        //System.out.println("second term:"+ term2);

        float term3 = (float) ((exp1+exp2)*epsilon*psi);
        //System.out.println("third term:"+ term3);
        //System.out.println("resultvar:"+ resultVar);
        //System.out.println("4th term: "+ Math.pow(resultExp,2));
        return new Pair<>(resultExp, resultVar);
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
            Set<MyEdge> edgelist = graph.incomingEdgesOf(revtoplist.get(i));
            for (MyEdge edge : edgelist){
                MyVertex source = graph.getEdgeSource(edge);
                revtoplist.add(source);
                tasklist.remove(source);
            }
            i++;
        }
        return revtoplist;
    }

    private static float calcWp(List<MyProcessor> cluster){
        float totalspeed = 0;
        float amount = 0;
        for(MyProcessor currproc : cluster){
            float speed = currproc.getProcspeed();
            totalspeed = totalspeed + speed;
            amount++;
        }
        return totalspeed/amount;
    }

    /*
    private static float calcWp(List<List<Object>> listoflists){ //abändern so dass cluster ?!
        List<Object> worklist = new ArrayList<Object>();
        HashSet<Float> cpus = new HashSet<>();
        for (List<Object> listoflist : listoflists) {
            worklist = listoflist;
            cpus.add((float) worklist.get(7));
        }
        float m = 0;
        for (float p : cpus){
            m = m + p;
        }
        return m/ cpus.size();
    }*/
    private static float smallestSize(List<List<Float>>wcharlist, List<Float> tasksizelist, List<Float> vx_wcharlist){
        float smallestsize = vx_wcharlist.size();
        if(tasksizelist.size() < smallestsize){
            smallestsize = tasksizelist.size();
        }
        for ( List<Float> worksizelist :wcharlist){
            if (worksizelist.size() < smallestsize){
                smallestsize = worksizelist.size();
            }
        }
        return smallestsize;
    }

    public static Pair<Float,List<Pair<MyVertex,MyProcessor>>> sdls_schedule(
            List<MyProcessor> cluster,
            //List<List<Object>> listofLists,
            DirectedAcyclicGraph<MyVertex,MyEdge> graph
            ){
        MyVertex v_entry = null;
        Set<MyVertex> tasklist = graph.vertexSet();
        List<MyVertex> taskpool = new ArrayList<>();

        MyVertex entry = findEntry(tasklist, graph);
        taskpool.add(entry);

        float w_p = calcWp(cluster); //listoflists vorher
        List<Pair<MyVertex, MyProcessor>> schedule = new ArrayList<>(); // war String, Float wieso ? siehe blätter?
        int i = 0;
        while(!taskpool.isEmpty()){
            //MyVertex
            List<SDLentry> listSDL = new ArrayList<>();
            for (MyVertex v_i :taskpool){
                for (MyProcessor processor :cluster){
                    //calc DRT for this task-processor pair (use formula 3 from paper)
                    Pair<Float, Float> drtvalues = getdrt(v_i, graph, schedule, processor);
                    //System.out.println("drtvalues: "+ drtvalues);
                    //calcStvalues
                    Pair<Float, Float> stvalues = maxValues(processor.getFtexp(), drtvalues.getValue0(), processor.getFtvar(), drtvalues.getValue1());
                    //System.out.println("stvalues:"+ stvalues);
                    //System.out.println("processorft:"+processor.getFtexp());
                    //calcSDL;
                    Pair<Float, Float> sdlvalues = calculateSDL(v_i, processor, w_p, stvalues.getValue0(), stvalues.getValue1());
                    //System.out.println("sdlvalues: " + sdlvalues);
                    SDLentry sdlentry = new SDLentry(v_i, processor,sdlvalues.getValue0(), sdlvalues.getValue1(),stvalues.getValue0(),stvalues.getValue1());
                    listSDL.add(sdlentry); //check if list empty after each assign !!!?!?!
                }
            }
            //Stochastic greater than every other SDL//get the task with greatest SDL;
            Pair<MyVertex,MyProcessor> taskprocpair = stochasticGreatest(listSDL);
            schedule.add(taskprocpair);// adds task-processor-pair to the schedule(our assign task to proc)
            //System.out.println("taskprocpair :"+ taskprocpair);
            MyVertex pushedtask = taskprocpair.getValue0(); //set to stochastic greatest task, remove from taskpool and add to schedule list
            taskpool.remove(pushedtask); // remove only greatest Task?!?!
            pushedtask.setPushed(true);

            //push unconstrained childs into readypool
            pushChilds(taskpool, pushedtask, graph);
            //System.out.println("taskpool:"+ taskpool);
            i++;
            //update earliest execution start time
            //updateST Funktion schreiben beachten Formeln 1-4 im Paper!!! ?!?!
            //split into FT update and calcST in calcSDL from FT of the processor and the DRT of Task-proc-pair
            MyProcessor chosenproc = taskprocpair.getValue1();
            Pair<Float,Float> stvalues = findST(taskprocpair,listSDL);
            //System.out.println("stvalues: "+ stvalues);
            float completiontimeexp = stvalues.getValue0() + pushedtask.getExpected()/ chosenproc.getProcspeed() ;
            float completiontimevar = stvalues.getValue1() + pushedtask.getVariance()/ chosenproc.getProcspeed() ;
            //update the FT of the chosenproc and update the cttime for the pushedtask
            chosenproc.setFtexp(completiontimeexp);
            chosenproc.setFtvar(completiontimevar);

            pushedtask.setCTtimeexp(completiontimeexp);
            pushedtask.setCTtimevar(completiontimevar);
            //updateST(cluster, graph, pushedtask, taskprocpair.getValue1(),taskpool);
        }
        //System.out.println("i:"+i);
        float makespan = findmakespan(cluster); //change to completiontime for last task thus => makespan = finishtime
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
    public static Pair<Float,Float> calculateSDL( MyVertex vertex, MyProcessor processor, float w_p, float stexp, float stvar){
        float varcompcapExp = calcVaryCompcap(vertex.getExpected(), w_p, processor);
        float varcompcapVar = calcVaryCompcap(vertex.getVariance(), w_p, processor);
        //System.out.println("varcompcap-values: "+varcompcapExp + " "+ varcompcapVar);
        float sdlexp = vertex.getSb_levelexp() - stexp + varcompcapExp;//prüfen ob ST bei beiden gleich und ob/wie rechnungen anpassen
        float sdlvar = vertex.getSb_levelvar() - stvar + varcompcapVar;
        return new Pair<>(sdlexp,sdlvar);
    }

    public static float calcVaryCompcap(float v_i, float w_p, MyProcessor processor){
        float varcompcap = (v_i/w_p) - (v_i/ processor.getProcspeed());
        return varcompcap;
    }

    public static Pair<MyVertex, MyProcessor> stochasticGreatest(List<SDLentry> listSDL){
        MyVertex greatestname = null;
        Double greatestx = null;
        MyProcessor greatestproc = null;
        for (SDLentry currentry : listSDL){
            float my;
            double sigma;
            if (currentry.getTaskname().getLabel().equals("Start") || currentry.getTaskname().getLabel().equals("End")){
                my = Math.abs(currentry.getSdlexp());
                sigma = Math.sqrt(Math.abs(currentry.getSdlvar()));
            }else {
                my = currentry.getSdlexp();
                sigma = Math.sqrt(currentry.getSdlvar());
            }
            //System.out.println("sigma: "+ sigma);
            double errfctterm = 1 + ((0.9*sigma*Math.sqrt(2*Math.PI)*Math.sqrt(2))/(-Math.sqrt(Math.PI)*sigma));
            //System.out.println("errfctterm: "+ errfctterm);
            double x = (erfInv(errfctterm)*2*sigma)-(Math.sqrt(2)*my)/(-Math.sqrt(2));
            //System.out.println("x :"+ x);
            if (greatestx == null || greatestx < x ){
                greatestx = x;
                greatestname = currentry.getTaskname();
                greatestproc = currentry.getProcname();
            }else {
                continue;
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
            //System.out.println("child:"+child.getLabel());
            Set<MyEdge> childancestors = graph.incomingEdgesOf(child);
            //System.out.println("ancestorslist:"+childancestors);
            boolean allpushed = true;
            for (MyEdge childedge : childancestors) {
                MyVertex ancestor = graph.getEdgeSource(childedge);
                //System.out.println("ancestors:"+ ancestor.getLabel());
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

    public static Pair<Float,Float> getdrt(MyVertex currtask,
                                           DirectedAcyclicGraph<MyVertex,MyEdge> graph,
                                           List<Pair<MyVertex, MyProcessor>> schedule,
                                           MyProcessor currproc){
        float drtexp = 0;
        float drtvar = 0;
        float ctexp = 0;
        float ctvar = 0;
        MyVertex biggestct = null;
        //get Ancestors of current task
        Set<MyEdge> inedges = graph.incomingEdgesOf(currtask);
        //get biggest completiontime (comp all ancestors)
        for (MyEdge edgeancestor : inedges){
            MyVertex ancestor = graph.getEdgeSource(edgeancestor);
            Pair<Float,Float> ctvalues = maxValues(ctexp,ancestor.getCTtimeexp(),ctvar, ancestor.getCTtimevar());
            float diffexp1 = Math.abs(ctexp - ctvalues.getValue0());
            float diffexp2 = Math.abs(ancestor.getCTtimeexp() - ctvalues.getValue0());
            float diffvar1 = Math.abs(ctvar - ctvalues.getValue1());
            float diffvar2 = Math.abs(ancestor.getCTtimevar() - ctvalues.getValue1());
            if (diffexp2 <= diffexp1){
                if (diffvar2 <= diffvar1){
                    ctexp = ancestor.getCTtimeexp();
                    ctvar = ancestor.getCTtimevar();
                    biggestct = ancestor;
                }else {
                    ctexp = ancestor.getCTtimeexp();
                    ctvar = ancestor.getCTtimevar();
                    biggestct = ancestor;
                    System.out.println("maxValues-getDRT wonky Fail. Diffexp correct but diffvar fail!");
                }
            }else {
                System.out.println("Diffexp failed!");
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

    public static Pair<Float,Float> findST(Pair<MyVertex,MyProcessor> taskprocpair, List<SDLentry> sdllist){
        float stexp = 0;
        float stvar = 0;
        for (SDLentry currentry : sdllist){
            if (currentry.getTaskname().equals(taskprocpair.getValue0()) && currentry.getProcname().equals(taskprocpair.getValue1())){
                stexp = currentry.getStexp();
                stvar = currentry.getStvar();
            }
        }
        return new Pair<>(stexp, stvar);
    }

    private static float findmakespan(List<MyProcessor> cluster){
        float makespan = 0;
        for (MyProcessor currproc : cluster){
            if (currproc.getFtexp() > makespan){
                makespan = currproc.getFtexp();
            }
        }
        return makespan;
    }

}
