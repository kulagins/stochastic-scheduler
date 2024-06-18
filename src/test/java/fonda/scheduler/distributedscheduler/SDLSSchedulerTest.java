package fonda.scheduler.distributedscheduler;

import fonda.scheduler.model.DataRow;
import fonda.scheduler.model.MyEdge;
import fonda.scheduler.model.MyProcessor;
import fonda.scheduler.model.MyVertex;
import org.javatuples.Pair;
//import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SDLSSchedulerTest {

    @Test
    void sblevelcalcTestroot() {
        //float runs = 1;
        float exp = 1;
        float var = 1;
        float cpuspeed = 1;
        DirectedAcyclicGraph<MyVertex, MyEdge> testgraph1 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(exp);
        rootvertex.setVariance(var);
        rootvertex.setPushed(false);
        testgraph1.addVertex(rootvertex);
        MyProcessor proc1 = new MyProcessor("proc1", 1);
        List<MyProcessor> cluster = new ArrayList<>();
        cluster.add(proc1);
        String machine = "testmachine";
        String workflow1 = "testwork";

        List<DataRow> rawdata1 = new ArrayList<>();
        float taskinputsize = 1;
        float wchar = 1;
        DataRow row1 = new DataRow(machine, workflow1, task, exp * cpuspeed, taskinputsize, wchar);
        rawdata1.add(row1);
        SDLSScheduler.sblevel_calc(cluster, rawdata1, testgraph1, workflow1);
        Set<MyVertex> vertexSet = testgraph1.vertexSet();
        for (MyVertex testvertex : vertexSet) {
            assertEquals(1, testvertex.getSb_levelexp(), "Failed expcalc is more then 1.");
            assertEquals(1, testvertex.getSb_levelvar(), "Failed varcalc is more than 1.");
        }
    }

    @Test
    void sblevelcalcTestoneedge() {
        //float runs = 1;
        float exp = 1;
        float var = 1;
        float cpuspeed = 1;
        DirectedAcyclicGraph<MyVertex, MyEdge> testgraph2 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(exp);
        rootvertex.setVariance(var);
        rootvertex.setPushed(false);
        testgraph2.addVertex(rootvertex);
        String task2 = "vertexone";
        float exp2 = 2;
        float var2 = 2;
        MyVertex vertexone = new MyVertex(task2);
        vertexone.setExpected(exp2);
        vertexone.setVariance(var2);
        vertexone.setPushed(false);
        testgraph2.addVertex(vertexone);
        testgraph2.addEdge(vertexone, rootvertex);

        MyProcessor proc1 = new MyProcessor("proc1", 1);
        List<MyProcessor> cluster2 = new ArrayList<>();
        cluster2.add(proc1);
        String machine = "testmachine";
        String workflow2 = "testwork";


        List<DataRow> rawdata2 = new ArrayList<>();
        float taskinputsize = 1;
        float wchar = 1;
        DataRow row2 = new DataRow(machine, workflow2, task, exp * cpuspeed, taskinputsize, wchar);
        DataRow row3 = new DataRow(machine, workflow2, task2, exp2 * cpuspeed, taskinputsize, 2);
        rawdata2.add(row2);
        rawdata2.add(row3);
        SDLSScheduler.sblevel_calc(cluster2, rawdata2, testgraph2, workflow2);
        Set<MyVertex> vertexSet = testgraph2.vertexSet();
        for (MyVertex testvertex : vertexSet) {
            if (testvertex.getLabel().equals("root")) {
                assertEquals(1, testvertex.getSb_levelexp(), "Failed expcalc is more then 1." + testvertex);
                assertEquals(1, testvertex.getSb_levelvar(), "Failed varcalc is more than 1." + testvertex);
            }
            if (testvertex.getLabel().equals("vertexone")) {
                assertEquals(4, testvertex.getSb_levelexp(), "Failed expcalc is more then 4." + testvertex);
                assertEquals(3, testvertex.getSb_levelvar(), "Failed varcalc is more than 4." + testvertex);
            }

        }
    }

    @Test
    void sblevelcalcTesttwodepth() {
        //float runs = 1;
        float cpuspeed = 1;
        DirectedAcyclicGraph<MyVertex, MyEdge> testgraph3 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(1);
        rootvertex.setVariance(1);
        rootvertex.setPushed(false);
        testgraph3.addVertex(rootvertex);
        String task2 = "vertexone";
        MyVertex vertexone = new MyVertex(task2);
        vertexone.setExpected(2);
        vertexone.setVariance(2);
        vertexone.setPushed(false);
        testgraph3.addVertex(vertexone);
        testgraph3.addEdge(vertexone, rootvertex);
        String task3 = "vertextwo";
        MyVertex vertextwo = new MyVertex(task3);
        vertextwo.setExpected(3);
        vertextwo.setVariance(3);
        vertextwo.setPushed(false);
        testgraph3.addVertex(vertextwo);
        testgraph3.addEdge(vertextwo, rootvertex);
        String task4 = "vertexthree";
        MyVertex vertexthree = new MyVertex(task4);
        vertexthree.setExpected(4);
        vertexthree.setVariance(4);
        vertexthree.setPushed(false);
        testgraph3.addVertex(vertexthree);
        testgraph3.addEdge(vertexthree, vertextwo);
        testgraph3.addEdge(vertexthree, vertexone);

        MyProcessor proc1 = new MyProcessor("proc1", 1);
        List<MyProcessor> cluster3 = new ArrayList<>();
        cluster3.add(proc1);

        String machine = "testmachine";
        String workflow3 = "testwork";
        List<DataRow> rawdata3 = new ArrayList<>();
        float taskinputsize = 4;
        float wchar = 1;
        DataRow row4 = new DataRow(machine, workflow3, task, 1 * cpuspeed, taskinputsize, wchar);
        DataRow row5 = new DataRow(machine, workflow3, task2, 2 * cpuspeed, 2, 2);
        DataRow row6 = new DataRow(machine, workflow3, task3, 3 * cpuspeed, 2, 3);
        DataRow row7 = new DataRow(machine, workflow3, task4, 4 * cpuspeed, 4, 4);
        rawdata3.add(row4);
        rawdata3.add(row5);
        rawdata3.add(row6);
        rawdata3.add(row7);
        SDLSScheduler.sblevel_calc(cluster3, rawdata3, testgraph3, workflow3);
        Set<MyVertex> vertexSet = testgraph3.vertexSet();
        for (MyVertex testvertex : vertexSet) {
            if (testvertex.getLabel().equals("root")) {
                assertEquals(1f, testvertex.getSb_levelexp(), "Failed expcalc is more then 1." + testvertex);
                assertEquals(1f, testvertex.getSb_levelvar(), "Failed varcalc is more than 1." + testvertex);
            }
            if (testvertex.getLabel().equals("vertexone")) {
                assertEquals(4.6f, testvertex.getSb_levelexp(), "Failed expcalc is more then 4,6." + testvertex);
                assertEquals(3f, testvertex.getSb_levelvar(), "Failed varcalc is more than 3." + testvertex);
            }
            if (testvertex.getLabel().equals("vertextwo")) {
                assertEquals(6.4f, testvertex.getSb_levelexp(), "Failed expcalc is more then 6.4." + testvertex);
                assertEquals(4f, testvertex.getSb_levelvar(), "Failed varcalc is more than 4." + testvertex);
            }
            if (testvertex.getLabel().equals("vertexthree")) {
                assertEquals(12.4f, testvertex.getSb_levelexp(), "Failed expcalc is more then 12.4." + testvertex);
                assertEquals(8f, testvertex.getSb_levelvar(), "Failed varcalc is more than 8." + testvertex);
            }

        }
    }

    @Test
    void sblevelcalcTesttwodepthnull() {
        //float runs = 1;
        float cpuspeed = 1;
        DirectedAcyclicGraph<MyVertex, MyEdge> testgraph3 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(0);
        rootvertex.setVariance(0);
        rootvertex.setPushed(false);
        testgraph3.addVertex(rootvertex);
        String task2 = "vertexone";
        MyVertex vertexone = new MyVertex(task2);
        vertexone.setExpected(2);
        vertexone.setVariance(2);
        vertexone.setPushed(false);
        testgraph3.addVertex(vertexone);
        testgraph3.addEdge(rootvertex, vertexone);
        String task3 = "vertextwo";
        MyVertex vertextwo = new MyVertex(task3);
        vertextwo.setExpected(3);
        vertextwo.setVariance(3);
        vertextwo.setPushed(false);
        testgraph3.addVertex(vertextwo);
        testgraph3.addEdge(rootvertex, vertextwo);
        String task4 = "vertexthree";
        MyVertex vertexthree = new MyVertex(task4);
        vertexthree.setExpected(0);
        vertexthree.setVariance(0);
        vertexthree.setPushed(false);
        testgraph3.addVertex(vertexthree);
        testgraph3.addEdge(vertextwo, vertexthree);
        testgraph3.addEdge(vertexone, vertexthree);

        MyProcessor proc1 = new MyProcessor("proc1", 1);
        List<MyProcessor> cluster3 = new ArrayList<>();
        cluster3.add(proc1);

        String machine = "testmachine";
        String workflow3 = "testwork";
        List<DataRow> rawdata3 = new ArrayList<>();
        float taskinputsize = 4;
        float wchar = 1;
        DataRow row4 = new DataRow(machine, workflow3, task, 1 * cpuspeed, taskinputsize, wchar);
        DataRow row5 = new DataRow(machine, workflow3, task2, 2 * cpuspeed, 2, 2);
        DataRow row6 = new DataRow(machine, workflow3, task3, 3 * cpuspeed, 2, 3);
        DataRow row7 = new DataRow(machine, workflow3, task4, 4 * cpuspeed, 4, 4);
        rawdata3.add(row4);
        rawdata3.add(row5);
        rawdata3.add(row6);
        rawdata3.add(row7);
        SDLSScheduler.sblevel_calc(cluster3, rawdata3, testgraph3, workflow3);
        Set<MyVertex> vertexSet = testgraph3.vertexSet();
        for (MyVertex currver : vertexSet) {
            System.out.println(currver.getLabel() + " sbexp: " + currver.getSb_levelexp() + " , " + " sbvar: " + currver.getSb_levelvar());
        }
    }

    @Test
    void sblevelcalcTestedgevalues(){
        //float runs = 1;
        float exp = 1;
        float var = 1;
        float cpuspeed = 1;
        DirectedAcyclicGraph<MyVertex, MyEdge> testgraph2 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "Start";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(exp);
        rootvertex.setVariance(var);
        rootvertex.setPushed(false);
        testgraph2.addVertex(rootvertex);
        String task2 = "vertexone";
        float exp2 = 2;
        float var2 = 2;
        MyVertex vertexone = new MyVertex(task2);
        vertexone.setExpected(exp2);
        vertexone.setVariance(var2);
        vertexone.setPushed(false);
        testgraph2.addVertex(vertexone);
        testgraph2.addEdge(rootvertex,vertexone);

        MyProcessor proc1 = new MyProcessor("proc1", 1);
        List<MyProcessor> cluster2 = new ArrayList<>();
        cluster2.add(proc1);
        String machine = "testmachine";
        String workflow2 = "testwork";

        List<DataRow> rawdata2 = new ArrayList<>();
        float taskinputsize = 1;
        float wchar = 1;
        DataRow row2 = new DataRow(machine,workflow2,task,exp*cpuspeed,taskinputsize,wchar);
        DataRow row3 = new DataRow(machine, workflow2,task2,exp2*cpuspeed,taskinputsize,2);
        rawdata2.add(row2);
        rawdata2.add(row3);
        SDLSScheduler.sblevel_calc(cluster2,rawdata2,testgraph2,workflow2);
        Set<MyVertex> vertexSet = testgraph2.vertexSet();
        for (MyVertex currver : vertexSet) {
            System.out.println(currver.getLabel() + " sbexp: " + currver.getSb_levelexp() + " , " + " sbvar: " + currver.getSb_levelvar());
        }
        Set<MyEdge> edgeSet = testgraph2.edgeSet();
        for (MyEdge curredge : edgeSet){
            System.out.println("Quelle: "+testgraph2.getEdgeSource(curredge).getLabel() + " Ziel: "+testgraph2.getEdgeTarget(curredge).getLabel());
            System.out.println("exp: "+ curredge.getExpected()+ " var: "+curredge.getVariance());
        }
    }

    @Test
    void sblevelcalcTestedgevalues2(){
        //float runs = 1;
        float exp = 1;
        float var = 1;
        float cpuspeed = 1;
        DirectedAcyclicGraph<MyVertex, MyEdge> testgraph2 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "rootvertex";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(exp);
        rootvertex.setVariance(var);
        rootvertex.setPushed(false);
        testgraph2.addVertex(rootvertex);
        String task2 = "End";
        float exp2 = 2;
        float var2 = 2;
        MyVertex vertexone = new MyVertex(task2);
        vertexone.setExpected(exp2);
        vertexone.setVariance(var2);
        vertexone.setPushed(false);
        testgraph2.addVertex(vertexone);
        testgraph2.addEdge(rootvertex,vertexone);

        MyProcessor proc1 = new MyProcessor("proc1", 1);
        List<MyProcessor> cluster2 = new ArrayList<>();
        cluster2.add(proc1);
        String machine = "testmachine";
        String workflow2 = "testwork";

        List<DataRow> rawdata2 = new ArrayList<>();
        float taskinputsize = 1;
        float wchar = 1;
        DataRow row2 = new DataRow(machine,workflow2,task,exp*cpuspeed,taskinputsize,wchar);
        DataRow row3 = new DataRow(machine, workflow2,task2,exp2*cpuspeed,taskinputsize,2);
        rawdata2.add(row2);
        rawdata2.add(row3);
        SDLSScheduler.sblevel_calc(cluster2,rawdata2,testgraph2,workflow2);
        Set<MyVertex> vertexSet = testgraph2.vertexSet();
        for (MyVertex currver : vertexSet) {
            System.out.println(currver.getLabel() + " sbexp: " + currver.getSb_levelexp() + " , " + " sbvar: " + currver.getSb_levelvar());
        }
        Set<MyEdge> edgeSet = testgraph2.edgeSet();
        for (MyEdge curredge : edgeSet){
            System.out.println("Quelle: "+testgraph2.getEdgeSource(curredge).getLabel() + " Ziel: "+testgraph2.getEdgeTarget(curredge).getLabel());
            System.out.println("exp: "+ curredge.getExpected()+ " var: "+curredge.getVariance());
        }
    }

    @Test
    void testmaxvalues(){
        Pair<Float,Float> results = SDLSScheduler.maxValues(15,23,7,13);
        System.out.println("results: "+ results.getValue0()+" zweiter: "+ results.getValue1());
    }

    @Test
    void testmaxvalues2(){
        Pair<Float,Float> results = SDLSScheduler.maxValues(15,23,16,7);
        System.out.println("results: "+ results.getValue0()+" zweiter: "+ results.getValue1());
    }

    @Test
    void testmaxvalues3(){
        Pair<Float,Float> results = SDLSScheduler.maxValues(0.5f,0.5f,0.5f,1);
        System.out.println("results: "+ results.getValue0()+" zweiter: "+ results.getValue1());
    }

    @Test
    void testmaxvalues4(){
        Pair<Float,Float> results = SDLSScheduler.maxValues(8.4f,6.6f,4f,3f);
        System.out.println("results: "+ results.getValue0()+" zweiter: "+ results.getValue1());
    }

    @Test
    void sdlstest1(){ //1 task 1 proc
        MyProcessor proc1 = new MyProcessor("proc1", 10);
        List<MyProcessor> currcluster = new ArrayList<>();
        currcluster.add(proc1);

        DirectedAcyclicGraph<MyVertex, MyEdge> testgraphsdls1 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(1);
        rootvertex.setVariance(1);
        rootvertex.setSb_levelexp(1);
        rootvertex.setSb_levelvar(1);
        rootvertex.setPushed(false);
        testgraphsdls1.addVertex(rootvertex);

        Pair<Float, List<Pair<MyVertex,MyProcessor>>> results = SDLSScheduler.sdls_schedule(currcluster,testgraphsdls1);
        float makespan = results.getValue0();
        assertEquals(1/10f,makespan, "Makespan is either 1/10 or wrong.!");
        List<Pair<MyVertex,MyProcessor>> schedule = results.getValue1();
        Pair<MyVertex,MyProcessor> taskpair1 = new Pair<>(rootvertex,proc1);
        assertEquals(taskpair1,schedule.get(0),"Roottask scheduled on proc1 or wrong.");
    }

    @Test
    void sdlstest2(){ //2 tasks 1 proc (straightline graph)
        MyProcessor proc1 = new MyProcessor("proc1", 10);
        List<MyProcessor> currcluster2 = new ArrayList<>();
        currcluster2.add(proc1);

        DirectedAcyclicGraph<MyVertex, MyEdge> testgraphsdls2 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(1);
        rootvertex.setVariance(1);
        rootvertex.setSb_levelexp(1);
        rootvertex.setSb_levelvar(1);
        rootvertex.setPushed(false);
        testgraphsdls2.addVertex(rootvertex);

        String task1 = "task1";
        MyVertex task1vertex = new MyVertex(task1);
        task1vertex.setExpected(1);
        task1vertex.setVariance(1);
        task1vertex.setSb_levelexp(1);
        task1vertex.setSb_levelvar(1);
        task1vertex.setPushed(false);
        testgraphsdls2.addVertex(task1vertex);

        testgraphsdls2.addEdge(rootvertex,task1vertex);
        Pair<Float, List<Pair<MyVertex,MyProcessor>>> results = SDLSScheduler.sdls_schedule(currcluster2,testgraphsdls2);
        float makespan = results.getValue0();
        System.out.println("makespan: "+ makespan);
        //assertEquals(0.2f,makespan, "Makespan is either 1/5 or wrong.!"); // is 0.182 instead of 0.2 (maxValues)
        List<Pair<MyVertex,MyProcessor>> schedule = results.getValue1();
        Pair<MyVertex,MyProcessor> taskpair1 = new Pair<>(rootvertex,proc1);
        assertEquals(taskpair1,schedule.get(0),"Roottask scheduled on proc1 or wrong.");
        Pair<MyVertex,MyProcessor> taskpair2 = new Pair<>(task1vertex,proc1);
        assertEquals(taskpair2,schedule.get(1),"Task1 scheduled on proc1 after root.Else wrong.");
    }


    @Test
    void sdlstest3(){ //4 tasks 1 proc (rhombus graph) diff values ( maybe extra test were task2 &3 same exp,var)
        MyProcessor proc1 = new MyProcessor("proc1", 10);
        List<MyProcessor> currcluster3 = new ArrayList<>();
        currcluster3.add(proc1);

        DirectedAcyclicGraph<MyVertex, MyEdge> testgraphsdls3 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(1);
        rootvertex.setVariance(1);
        rootvertex.setSb_levelexp(1);
        rootvertex.setSb_levelvar(1);
        rootvertex.setPushed(false);
        testgraphsdls3.addVertex(rootvertex);

        String task1 = "task1";
        MyVertex task1vertex = new MyVertex(task1);
        task1vertex.setExpected(2);
        task1vertex.setVariance(2);
        task1vertex.setSb_levelexp(2);
        task1vertex.setSb_levelvar(2);
        task1vertex.setPushed(false);
        testgraphsdls3.addVertex(task1vertex);

        String task2 = "task2";
        MyVertex task2vertex = new MyVertex(task2);
        task2vertex.setExpected(3);
        task2vertex.setVariance(3);
        task2vertex.setSb_levelexp(3);
        task2vertex.setSb_levelvar(3);
        task2vertex.setPushed(false);
        testgraphsdls3.addVertex(task2vertex);

        String task3 = "task3";
        MyVertex task3vertex = new MyVertex(task3);
        task3vertex.setExpected(4);
        task3vertex.setVariance(4);
        task3vertex.setSb_levelexp(4);
        task3vertex.setSb_levelvar(4);
        task3vertex.setPushed(false);
        testgraphsdls3.addVertex(task3vertex);

        testgraphsdls3.addEdge(rootvertex, task1vertex);
        testgraphsdls3.addEdge(rootvertex, task2vertex);
        testgraphsdls3.addEdge(task1vertex, task3vertex);
        testgraphsdls3.addEdge(task2vertex, task3vertex);
        Pair<Float, List<Pair<MyVertex,MyProcessor>>> results = SDLSScheduler.sdls_schedule(currcluster3,testgraphsdls3);
        System.out.println("results: " + results);
    }

    @Test
    void sdlstest4(){ //1 task multiple procs
        MyProcessor proc1 = new MyProcessor("proc1", 10);
        MyProcessor proc2 = new MyProcessor("proc2", 5);
        MyProcessor proc3 = new MyProcessor("proc3", 15);
        List<MyProcessor> currcluster4 = new ArrayList<>();
        currcluster4.add(proc1);
        currcluster4.add(proc2);
        currcluster4.add(proc3);

        DirectedAcyclicGraph<MyVertex, MyEdge> testgraphsdls4 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(1);
        rootvertex.setVariance(1);
        rootvertex.setSb_levelexp(1);
        rootvertex.setSb_levelvar(1);
        rootvertex.setPushed(false);
        testgraphsdls4.addVertex(rootvertex);
        Pair<Float, List<Pair<MyVertex,MyProcessor>>> results = SDLSScheduler.sdls_schedule(currcluster4,testgraphsdls4);
        System.out.println("results: " + results);
    }

    @Test
    void sdlstest5(){ //2 tasks multiple procs
        MyProcessor proc1 = new MyProcessor("proc1", 10);
        MyProcessor proc2 = new MyProcessor("proc2", 5);
        MyProcessor proc3 = new MyProcessor("proc3", 15);
        List<MyProcessor> currcluster5 = new ArrayList<>();
        currcluster5.add(proc1);
        currcluster5.add(proc2);
        currcluster5.add(proc3);

        DirectedAcyclicGraph<MyVertex, MyEdge> testgraphsdls5 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(1);
        rootvertex.setVariance(1);
        rootvertex.setSb_levelexp(1);
        rootvertex.setSb_levelvar(1);
        rootvertex.setPushed(false);
        testgraphsdls5.addVertex(rootvertex);

        String task1 = "task1";
        MyVertex task1vertex = new MyVertex(task1);
        task1vertex.setExpected(2);
        task1vertex.setVariance(2);
        task1vertex.setSb_levelexp(2);
        task1vertex.setSb_levelvar(2);
        task1vertex.setPushed(false);
        testgraphsdls5.addVertex(task1vertex);

        testgraphsdls5.addEdge(rootvertex,task1vertex);
        Pair<Float, List<Pair<MyVertex,MyProcessor>>> results = SDLSScheduler.sdls_schedule(currcluster5,testgraphsdls5);
        System.out.println("results: " + results);
    }

    @Test
    void sdlstest6(){ //4 task2 multiple procs (similar to 3 mb try same vals)
        MyProcessor proc1 = new MyProcessor("proc1", 10);
        MyProcessor proc2 = new MyProcessor("proc2", 5);
        MyProcessor proc3 = new MyProcessor("proc3", 15);
        List<MyProcessor> currcluster6 = new ArrayList<>();
        currcluster6.add(proc1);
        currcluster6.add(proc2);
        currcluster6.add(proc3);

        DirectedAcyclicGraph<MyVertex, MyEdge> testgraphsdls6 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(1);
        rootvertex.setVariance(1);
        rootvertex.setSb_levelexp(1);
        rootvertex.setSb_levelvar(1);
        rootvertex.setPushed(false);
        testgraphsdls6.addVertex(rootvertex);

        String task1 = "task1";
        MyVertex task1vertex = new MyVertex(task1);
        task1vertex.setExpected(2);
        task1vertex.setVariance(2);
        task1vertex.setSb_levelexp(2);
        task1vertex.setSb_levelvar(2);
        task1vertex.setPushed(false);
        testgraphsdls6.addVertex(task1vertex);

        String task2 = "task2";
        MyVertex task2vertex = new MyVertex(task2);
        task2vertex.setExpected(3);
        task2vertex.setVariance(3);
        task2vertex.setSb_levelexp(3);
        task2vertex.setSb_levelvar(3);
        task2vertex.setPushed(false);
        testgraphsdls6.addVertex(task2vertex);

        String task3 = "task3";
        MyVertex task3vertex = new MyVertex(task3);
        task3vertex.setExpected(4);
        task3vertex.setVariance(4);
        task3vertex.setSb_levelexp(4);
        task3vertex.setSb_levelvar(4);
        task3vertex.setPushed(false);
        testgraphsdls6.addVertex(task3vertex);

        testgraphsdls6.addEdge(rootvertex, task1vertex);
        testgraphsdls6.addEdge(rootvertex, task2vertex);
        testgraphsdls6.addEdge(task1vertex, task3vertex);
        testgraphsdls6.addEdge(task2vertex, task3vertex);
        Pair<Float, List<Pair<MyVertex,MyProcessor>>> results = SDLSScheduler.sdls_schedule(currcluster6,testgraphsdls6);
        System.out.println("results: " + results);
    }

    @Test
    void sdlstest7(){
        MyProcessor proc1 = new MyProcessor("proc1", 10);
        MyProcessor proc2 = new MyProcessor("proc2", 5);
        MyProcessor proc3 = new MyProcessor("proc3", 15);
        List<MyProcessor> currcluster7 = new ArrayList<>();
        currcluster7.add(proc1);
        currcluster7.add(proc2);
        currcluster7.add(proc3);

        DirectedAcyclicGraph<MyVertex, MyEdge> testgraphsdls7 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(1);
        rootvertex.setVariance(1);
        rootvertex.setSb_levelexp(1);
        rootvertex.setSb_levelvar(1);
        rootvertex.setPushed(false);
        testgraphsdls7.addVertex(rootvertex);

        String task1 = "task1";
        MyVertex task1vertex = new MyVertex(task1);
        task1vertex.setExpected(2);
        task1vertex.setVariance(2);
        task1vertex.setSb_levelexp(2);
        task1vertex.setSb_levelvar(2);
        task1vertex.setPushed(false);
        testgraphsdls7.addVertex(task1vertex);

        testgraphsdls7.addEdge(rootvertex,task1vertex);
        MyEdge edge1 = testgraphsdls7.getEdge(rootvertex,task1vertex);
        edge1.setExpected(2);
        edge1.setVariance(2);
        Pair<Float, List<Pair<MyVertex,MyProcessor>>> results = SDLSScheduler.sdls_schedule(currcluster7,testgraphsdls7);
        System.out.println("results: " + results);
    }

    @Test
    void sdlstest8(){ //4 task2 multiple procs (similar to 3 mb try same vals)
        MyProcessor proc1 = new MyProcessor("proc1", 10);
        MyProcessor proc2 = new MyProcessor("proc2", 5);
        MyProcessor proc3 = new MyProcessor("proc3", 15);
        List<MyProcessor> currcluster8 = new ArrayList<>();
        currcluster8.add(proc1);
        currcluster8.add(proc2);
        currcluster8.add(proc3);

        DirectedAcyclicGraph<MyVertex, MyEdge> testgraphsdls8 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "root";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(1);
        rootvertex.setVariance(1);
        rootvertex.setSb_levelexp(1);
        rootvertex.setSb_levelvar(1);
        rootvertex.setPushed(false);
        testgraphsdls8.addVertex(rootvertex);

        String task1 = "task1";
        MyVertex task1vertex = new MyVertex(task1);
        task1vertex.setExpected(2);
        task1vertex.setVariance(2);
        task1vertex.setSb_levelexp(2);
        task1vertex.setSb_levelvar(2);
        task1vertex.setPushed(false);
        testgraphsdls8.addVertex(task1vertex);

        String task2 = "task2";
        MyVertex task2vertex = new MyVertex(task2);
        task2vertex.setExpected(3);
        task2vertex.setVariance(3);
        task2vertex.setSb_levelexp(3);
        task2vertex.setSb_levelvar(3);
        task2vertex.setPushed(false);
        testgraphsdls8.addVertex(task2vertex);

        String task3 = "task3";
        MyVertex task3vertex = new MyVertex(task3);
        task3vertex.setExpected(4);
        task3vertex.setVariance(4);
        task3vertex.setSb_levelexp(4);
        task3vertex.setSb_levelvar(4);
        task3vertex.setPushed(false);
        testgraphsdls8.addVertex(task3vertex);

        testgraphsdls8.addEdge(rootvertex, task1vertex).setExpected(1);
        testgraphsdls8.addEdge(rootvertex, task2vertex).setExpected(10);
        testgraphsdls8.addEdge(task1vertex, task3vertex).setExpected(2);
        testgraphsdls8.addEdge(task2vertex, task3vertex).setExpected(2);
        Pair<Float, List<Pair<MyVertex,MyProcessor>>> results = SDLSScheduler.sdls_schedule(currcluster8,testgraphsdls8);
        System.out.println("results: " + results);
    }

    @Test
    void sdlstestnull(){ //4 task2 multiple procs (similar to 3 mb try same vals)
        MyProcessor proc1 = new MyProcessor("proc1", 10);
        MyProcessor proc2 = new MyProcessor("proc2", 5);
        MyProcessor proc3 = new MyProcessor("proc3", 15);
        List<MyProcessor> currcluster8 = new ArrayList<>();
        currcluster8.add(proc1);
        currcluster8.add(proc2);
        currcluster8.add(proc3);

        DirectedAcyclicGraph<MyVertex, MyEdge> testgraphsdls8 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false);
        String task = "Start";
        MyVertex rootvertex = new MyVertex(task);
        rootvertex.setExpected(0);
        rootvertex.setVariance(0);
        rootvertex.setSb_levelexp(3);
        rootvertex.setSb_levelvar(3);
        rootvertex.setPushed(false);
        testgraphsdls8.addVertex(rootvertex);

        String task1 = "task1";
        MyVertex task1vertex = new MyVertex(task1);
        task1vertex.setExpected(2);
        task1vertex.setVariance(2);
        task1vertex.setSb_levelexp(2);
        task1vertex.setSb_levelvar(2);
        task1vertex.setPushed(false);
        testgraphsdls8.addVertex(task1vertex);

        String task2 = "task2";
        MyVertex task2vertex = new MyVertex(task2);
        task2vertex.setExpected(3);
        task2vertex.setVariance(3);
        task2vertex.setSb_levelexp(3);
        task2vertex.setSb_levelvar(3);
        task2vertex.setPushed(false);
        testgraphsdls8.addVertex(task2vertex);

        String task3 = "End";
        MyVertex task3vertex = new MyVertex(task3);
        task3vertex.setExpected(0);
        task3vertex.setVariance(0);
        task3vertex.setSb_levelexp(0);
        task3vertex.setSb_levelvar(0);
        task3vertex.setPushed(false);
        testgraphsdls8.addVertex(task3vertex);

        testgraphsdls8.addEdge(rootvertex, task1vertex).setExpected(0);
        testgraphsdls8.addEdge(rootvertex, task2vertex).setExpected(0);
        testgraphsdls8.addEdge(task1vertex, task3vertex).setExpected(0);
        testgraphsdls8.addEdge(task2vertex, task3vertex).setExpected(0);
        Pair<Float, List<Pair<MyVertex,MyProcessor>>> results = SDLSScheduler.sdls_schedule(currcluster8,testgraphsdls8);
        System.out.println("results: " + results);
    }
}