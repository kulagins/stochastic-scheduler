package fonda.scheduler.labeller;

import fonda.scheduler.distributedscheduler.K8Helper;
import fonda.scheduler.distributedscheduler.SDLSScheduler;
import fonda.scheduler.distributedscheduler.SJFNScheduler;
import fonda.scheduler.model.*;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.base.OperationContext;
import io.fabric8.kubernetes.client.informers.ListerWatcher;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedInformerEventListener;
import io.fabric8.kubernetes.client.informers.impl.DefaultSharedIndexInformer;
import org.javatuples.Pair;

import org.jgrapht.nio.dot.DOTImporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


//package org.jgrapht.nio.dot;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.*;
import org.jgrapht.util.*;
//import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.fail;

public class CurrentPodNodeStatus {

    private KubernetesClient client;

    private OperationContext operationContext;

    private final List<List<Object>> listofLists;

    private final List<DataRow> rawdata;

    private final List<MyProcessor> cluster;

    private ConcurrentLinkedQueue<SharedInformerEventListener> workerQueueNode;

    DefaultSharedIndexInformer<Node, NodeList> defaultSharedIndexInformerNode;

    private static final Logger logger = LoggerFactory.getLogger(CurrentPodNodeStatus.class);

    private final List<Node> nodeList;

    public CurrentPodNodeStatus(KubernetesClient client, List<List<Object>> listofLists, List<DataRow> rawdata, List<MyProcessor> cluster) {
        this.client = client;

        this.listofLists = listofLists;

        this.rawdata = rawdata;

        this.cluster = cluster;

        SJFNScheduler.podList = new PodListWithIndex();

        this.workerQueueNode = new ConcurrentLinkedQueue<>();

        this.operationContext = new OperationContext();

        this.nodeList = client.nodes().list().getItems();



        setUpIndexInformerNode();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // setUpIndexInformerPod();

        ListOptions options = new ListOptions();
        //options.setFieldSelector("spec.schedulerName=new-scheduler");

        /*
        DirectedAcyclicGraph<MyVertex, MyEdge> result2 = new DirectedAcyclicGraph<>(
                SupplierUtil.createSupplier(MyVertex.class),
                SupplierUtil.createSupplier(MyEdge.class),
                false
        );

        DOTImporter<MyVertex, MyEdge> importer2 = new DOTImporter<>();
        importer2.addVertexAttributeConsumer((p,a)->{
            String name = p.getSecond();
            MyVertex myvertex = p.getFirst();
            //System.out.println("p ist :" + p);
            //System.out.println("a ist :" + a);
            Attribute attrs = a;
            int zaehler;
            if (name.equals("label")) {
                String label = attrs.getValue();
                myvertex.setLabel(attrs.getValue());
                for (zaehler = 0; zaehler < listofLists.size(); zaehler++) {
                    List<Object> worklist = new ArrayList<Object>();
                    worklist = listofLists.get(zaehler);
                    String taskname = (String) worklist.get(2);
                    if (taskname.equals(label)) { //add workflowname
                        float exp = (float) worklist.get(5);
                        float var = (float) worklist.get(6);
                        myvertex.setExpected(exp);
                        myvertex.setVariance(var);
                        myvertex.setPushed(false);
                    }
                    if (label.equals("Start") || label.equals("End")) {
                        myvertex.setExpected(0);
                        myvertex.setVariance(0);
                        myvertex.setPushed(false);
                    }
                }
            }

        });
        try{
            importer2.importGraph(result2, new FileReader("src/main/resources/bacass_sparse.dot"));
        }catch (Exception e){
            System.out.println("Error could not import the File. Error 404i." + e);
        }*/

        final long[] starttime = new long[1];
        final long[] finishtime = new long[1];
        List<Pair<String, List<Pair<MyVertex,MyProcessor>>>> schedulelist = new ArrayList<>();
        client.pods().watch(options, new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod pod) {
                //if (pod.getMetadata().getNamespace().equals("hoegvinc")) {
                    switch (action) {
                        case ADDED:
                            logger.info("[" + pod.getSpec().getContainers().get(0).getName() + "] - " + "New Pod added to Scheduler: " + pod.getMetadata().getName());
                            String name2 = "";
                            try {
                                name2 = pod.getMetadata().getLabels().get("nextflow.io/processName");
                                if (name2 == null) {
                                    starttime[0] = System.currentTimeMillis();
                                    System.out.println("There was no processname.");
                                    //pod.getSpec().setNodeName("hu-master-c23");
                                    //Map<String,String> nodemap = new HashMap<String,String>();
                                    //nodemap.put("usedby","hoegvinc");
                                    //pod.getSpec().setNodeSelector(nodemap);

                                    List<NodeSelectorTerm> nodeSelectorTerms = new ArrayList<>();
                                    NodeSelectorTerm nodeSelectorTerm = new NodeSelectorTerm();
                                    List<NodeSelectorRequirement> nodeSelectorRequirements = new ArrayList<>();
                                    NodeSelectorRequirement nodeSelectorRequirement = new NodeSelectorRequirement();
                                    nodeSelectorRequirement.setKey("usedby");
                                    nodeSelectorRequirement.setOperator("In");
                                    List<String> selectorvalues = new ArrayList<>();
                                    selectorvalues.add("hoegvinc");

                                    nodeSelectorRequirement.setValues(selectorvalues);
                                    nodeSelectorRequirements.add(nodeSelectorRequirement);

                                    nodeSelectorTerm.setMatchExpressions(nodeSelectorRequirements);

                                    nodeSelectorTerms.add(nodeSelectorTerm);

                                    NodeSelector nodeselec = new NodeSelector();
                                    nodeselec.setNodeSelectorTerms(nodeSelectorTerms);

                                    NodeAffinity nodeaffi = new NodeAffinity();
                                    nodeaffi.setRequiredDuringSchedulingIgnoredDuringExecution(nodeselec);
                                    Affinity affi = new Affinity();

                                    affi.setNodeAffinity(nodeaffi);
                                    pod.getSpec().setAffinity(affi);
                                    //System.out.println(pod.getSpec());//needs fixing
                                    break;
                                }
                            } catch (Exception e) {
                                System.out.println("Error 606: No processname found.");
                                break;
                            }

                            String[] names = name2.split("_");
                            String workflowname = names[2];
                            //Check if schedule available
                            for (int j = 0; j <= schedulelist.size(); j++) {
                                //Pair<String, List<Pair<MyVertex,MyProcessor>>> currpair = schedulelist.get(j);
                                if (!schedulelist.isEmpty() && schedulelist.get(j).getValue0().equalsIgnoreCase(workflowname)) {
                                    //Assign nodeName for pod (copy from below) -> extra function if it works
                                        Pair<String, List<Pair<MyVertex, MyProcessor>>> schedulepair = schedulelist.get(j);
                                        List<Pair<MyVertex, MyProcessor>> schedule = schedulepair.getValue1();
                                        schedulePod(nodeList, pod, schedule);
                                        System.out.println("Schedule already existed.");
                                        break;
                                }
                                if (schedulelist.isEmpty() || j == (schedulelist.size() - 1)) {
                                    //import dot-file based on workflow from names
                                    DirectedAcyclicGraph<MyVertex, MyEdge> result = new DirectedAcyclicGraph<>(
                                            SupplierUtil.createSupplier(MyVertex.class),
                                            SupplierUtil.createSupplier(MyEdge.class),
                                            false
                                    );

                                    DOTImporter<MyVertex, MyEdge> importer = new DOTImporter<>();
                                    importer.addVertexAttributeConsumer((p, a) -> {    //adds attributes to graph
                                        String name = p.getSecond();
                                        MyVertex myvertex = p.getFirst();

                                        Attribute attrs = a;
                                        int zaehler;
                                        if (name.equals("label")) {
                                            String label = attrs.getValue();
                                            myvertex.setLabel(attrs.getValue());
                                            for (zaehler = 0; zaehler < listofLists.size(); zaehler++) {
                                                List<Object> worklist = new ArrayList<>();
                                                worklist = listofLists.get(zaehler);
                                                String taskname = (String) worklist.get(2);
                                                if (taskname.equals(label)) { //add workflowname
                                                    double exp = (double) worklist.get(5);
                                                    double var = (double) worklist.get(6);
                                                    myvertex.setExpected(exp);
                                                    myvertex.setVariance(var);
                                                    myvertex.setPushed(false);
                                                }
                                                if (label.equals("Start") || label.equals("End")) {
                                                    myvertex.setExpected(0);
                                                    myvertex.setVariance(0);
                                                    myvertex.setPushed(false);
                                                }
                                            }
                                        }

                                    });

                                    try {
                                        //importer.importGraph(result, new FileReader("src/main/resources/methylseq_sparse"));
                                        //System.out.println("workflowname:"+ workflowname+ " lowercase: " +workflowname.toLowerCase());
                                        String workflownamelowercase = workflowname.toLowerCase();
                                        InputStreamReader in3 = new InputStreamReader(CurrentPodNodeStatus.class.getResourceAsStream("/"+workflownamelowercase+"_sparse.dot"), StandardCharsets.UTF_8);
                                        //importer.importGraph(result, new FileReader("src/main/resources/"+workflownamelowercase+"_sparse.dot"));
                                        importer.importGraph(result, in3);
                                    } catch (Exception e) {
                                        System.out.println("workflowname:"+ workflowname.toLowerCase());
                                        System.out.println("Error while reading dot-File.");
                                        break;
                                    }

                                    SDLSScheduler.sblevel_calc(cluster, rawdata, result, workflowname);
                                    Pair<Double, List<Pair<MyVertex, MyProcessor>>> results = SDLSScheduler.sdls_schedule(cluster, result);
                                    //System.out.println(cluster);
                                    schedulelist.add(new Pair<>(workflowname, results.getValue1()));
                                    System.out.println("Added schedule for "+workflowname+" to list. Expected time: " + results.getValue0());
                                    try {
                                        FileWriter writer = new FileWriter("testresults.txt", true);
                                        BufferedWriter bw = new BufferedWriter(writer);
                                        bw.write("Added schedule for "+workflowname+" to list. Expected time: " + results.getValue0());
                                        bw.newLine();
                                        bw.close();
                                    } catch (IOException e) {
                                        System.out.println("An error occured during creation of Writer.");
                                        e.printStackTrace();
                                    }

                                    schedulePod(nodeList, pod, results.getValue1()); //schedules pod which cause calculation
                                    /*for (int n = 0; n < schedulelist.size(); n++) {
                                        Pair<String, List<Pair<MyVertex, MyProcessor>>> schedulepair = schedulelist.get(n);
                                        if (workflowname.equals(schedulepair.getValue0())) {
                                            List<Pair<MyVertex, MyProcessor>> schedule = schedulepair.getValue1();
                                            for (int o = 0; o < schedule.size(); o++) {
                                                if (Objects.equals(pod.getMetadata().getName(), schedule.get(o).getValue0().getLabel())) {
                                                    String nodename = schedule.get(o).getValue1().getProcname();
                                                    Node currnode = findNode(nodeList, nodename);
                                                    Pair<Pod, Node> currpair = K8Helper.bindPodToNode(pod, currnode, -1.0);
                                                    System.out.println("Scheduled: " + currpair.getValue0().getMetadata().getLabels() + " to " + currnode.getMetadata().getName());
                                                    //pod.getSpec().setNodeName(nodename);
                                                    break;
                                                }
                                            }
                                        }
                                    }*/

                                    break;
                                }
                            }
                            break;
                        case MODIFIED:
                            break;
                        case DELETED:
                            logger.info("[" + pod.getSpec().getContainers().get(0).getName() + "] - " + "Pod deleted: " + pod.getMetadata().getName());
                            String taskname = pod.getMetadata().getName();
                            for (Pair<String, List<Pair<MyVertex, MyProcessor>>> schedulepair : schedulelist) {
                                if (pod.getMetadata().getLabels().get("nextflow.io/processName").equals(schedulepair.getValue0())) {
                                    List<Pair<MyVertex, MyProcessor>> schedule = schedulepair.getValue1();
                                    for (int p = 0; p < schedule.size(); p++) {
                                        Pair<MyVertex, MyProcessor> pair = schedule.get(p);
                                        if (pair.getValue0().getLabel().equals(taskname) && p == (schedule.size() - 1)) {
                                            finishtime[0] = System.currentTimeMillis();
                                            break;
                                        }
                                    }
                                }
                            }
                            long exectime = finishtime[0] - starttime[0];
                            System.out.println("The time for the execution of the workflow is: " + exectime);
                            try {
                                FileWriter writer = new FileWriter("testresults.txt",true);
                                BufferedWriter bw = new BufferedWriter(writer);
                                bw.write("The time for the execution of the workflow is: " + exectime);
                                bw.newLine();
                                bw.write("Starttime: "+ starttime[0]);
                                bw.write("Finishtime: "+finishtime[0]);
                                bw.newLine();
                                bw.close();
                            } catch (IOException e) {
                                System.out.println("An error occured during creation of Writer.");
                                e.printStackTrace();
                            }
                            //SJFNScheduler.podList.removePodFromList(pod);
                            //SJFNScheduler.scheduleSJFN(null);
                            break;
                    }
                //}

            }

            @Override
            public void onClose(WatcherException cause) {

            }
        });


    }

    private Node findNode(List<Node> nodeList, String procname){
        for (Node currnode : nodeList){
            if (currnode.getMetadata().getName().equals(procname)){
                return currnode;
            }
        }
        return null;
    }

    private void schedulePod(List<Node> nodeList, Pod pod, List<Pair<MyVertex,MyProcessor>>schedule){
        for (int o = 0; o < schedule.size(); o++) {
            if (pod.getMetadata().getName().equalsIgnoreCase(schedule.get(o).getValue0().getLabel())) {
                String nodename = schedule.get(o).getValue1().getProcname();
                Node currnode = findNode(nodeList, nodename);
                Pair<Pod, Node> currpair = K8Helper.bindPodToNode(pod, currnode, -1.0);
                System.out.println("Scheduled: " + currpair.getValue0().getMetadata().getLabels() + " to " + currnode.getMetadata().getName());
                try {
                    FileWriter writer = new FileWriter("testresults.txt",true);
                    BufferedWriter bw = new BufferedWriter(writer);
                    bw.write("Scheduled: " + currpair.getValue0().getMetadata().getLabels() + " to " + currnode.getMetadata().getName());
                    bw.newLine();
                    bw.close();
                } catch (IOException e) {
                    System.out.println("An error occured during creation of Writer.");
                    e.printStackTrace();
                }
                //check ob -1.0 korrekt ?!
                //pod.getSpec().setNodeName(nodename);
                break;
            }
        }
    }
    /**
     *
     */
    private void setUpIndexInformerNode() {
        ListerWatcher listerWatcher = new ListerWatcher() {
            @Override
            public Watch watch(ListOptions params, String namespace, OperationContext context, Watcher watcher) {
                return client.nodes().watch(new Watcher<Node>() {
                    @Override
                    public void eventReceived(Action action, Node resource) {

                    }

                    @Override
                    public void onClose(WatcherException cause) {

                    }
                });
            }

            @Override
            public Object list(ListOptions params, String namespace, OperationContext context) {
                if (client.nodes()==null){
                   return List.of();
                }
                try{SJFNScheduler.nodeList = client.nodes().list();}
                catch(Exception e){return List.of();}
                Collections.shuffle(SJFNScheduler.nodeList.getItems()); // use this for scheduling experiments
                return SJFNScheduler.nodeList;
            }
        };

        defaultSharedIndexInformerNode = new DefaultSharedIndexInformer(Node.class, listerWatcher, 24000, operationContext, workerQueueNode);

        try {
            defaultSharedIndexInformerNode.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        defaultSharedIndexInformerNode.addEventHandler(new ResourceEventHandler<Node>() {
            @Override
            public void onAdd(Node obj) {

            }

            @Override
            public void onUpdate(Node oldObj, Node newObj) {

            }

            @Override
            public void onDelete(Node obj, boolean deletedFinalStateUnknown) {

            }
        });
    }

}
