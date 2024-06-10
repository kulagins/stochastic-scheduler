package fonda.scheduler.labeller;

import fonda.scheduler.distributedscheduler.SDLSScheduler;
import fonda.scheduler.distributedscheduler.SJFNScheduler;
import fonda.scheduler.model.*;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
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

    public CurrentPodNodeStatus(KubernetesClient client, List<List<Object>> listofLists, List<DataRow> rawdata, List<MyProcessor> cluster) {
        this.client = client;

        this.listofLists = listofLists;

        this.rawdata = rawdata;

        this.cluster = cluster;

        SJFNScheduler.podList = new PodListWithIndex();

        this.workerQueueNode = new ConcurrentLinkedQueue<>();

        this.operationContext = new OperationContext();


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
            importer2.importGraph(result2, new FileReader("src/main/resources/methylseq_sparse.dot"));
        }catch (Exception e){
            System.out.println("Error could not import the File. Error 404i.");
        }*/

        //System.out.print("stop before watcher");
        //System.out.println("Failed here if printed.");
        //result2.getEdgeSource().getLabel();
        List<Pair<String, List<Pair<MyVertex,MyProcessor>>>> schedulelist = new ArrayList<>();
        client.pods().watch(options, new Watcher<Pod>() {
            @Override
            public void eventReceived(Action action, Pod pod) {

            //System.out.println("hello");

                switch (action) {
                    case ADDED:
                        logger.info("[" + pod.getSpec().getContainers().get(0).getName() + "] - " + "New Pod added to Scheduler: " + pod.getMetadata().getName());
                        //System.out.println(pod);
                        String name2 ="";
                        try {
                            name2 = pod.getMetadata().getLabels().get("nextflow.io/processName");
                        }catch(Exception e){
                            System.out.println("Error 606: No processname found.");
                        }
                        String [] names = name2.split("_");
                        String workflowname = names[2];
                        //Check if schedule available
                        for (int j = 0; j < schedulelist.size(); j++){
                            Pair<String, List<Pair<MyVertex,MyProcessor>>> currpair = schedulelist.get(j);
                            if (currpair.getValue0().equals(workflowname)){
                                System.out.println("Schedule already existed.");
                                break;
                            }
                            if (j == (schedulelist.size()-1)){
                                //import dot-file based on workflow from names
                                DirectedAcyclicGraph<MyVertex, MyEdge> result = new DirectedAcyclicGraph<>(
                                        SupplierUtil.createSupplier(MyVertex.class),
                                        SupplierUtil.createSupplier(MyEdge.class),
                                        false
                                );

                                DOTImporter<MyVertex, MyEdge> importer = new DOTImporter<>();
                                importer.addVertexAttributeConsumer((p,a)->{    //adds attributes to graph
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
                                            List<Object> worklist = new ArrayList<>();
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
                                    //importer.importGraph(result, new FileReader("src/main/resources/methylseq_sparse.dot"));

                                    importer.importGraph(result, new FileReader("src/main/resources/"+workflowname+"_sparse.dot"));
                                }catch(Exception e){
                                    System.out.println("Error while reading dot-File.");
                                }
                                //SDLSScheduler result = new SDLSScheduler()
                                SDLSScheduler.sblevel_calc(cluster, rawdata, result, workflowname);
                                Pair<Float, List<Pair<MyVertex,MyProcessor>>> results = SDLSScheduler.sdls_schedule(cluster, result);
                                //System.out.println(name2);
                                schedulelist.add(new Pair<>(workflowname, results.getValue1()));
                                break;
                            }
                        }
                        break;
                    case MODIFIED:
                        break;
                    case DELETED:
                        logger.info("[" + pod.getSpec().getContainers().get(0).getName() + "] - " + "Pod deleted: " + pod.getMetadata().getName());
                        SJFNScheduler.podList.removePodFromList(pod);
                        SJFNScheduler.scheduleSJFN(null);
                        
                }

            }

            @Override
            public void onClose(WatcherException cause) {

            }
        });


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
