package fonda.scheduler.controller;
//package fonda.scheduler.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;

import fonda.scheduler.model.*;
import java.util.*;
//import java.util.List;

class MainTest {

    @BeforeEach
    void setUp(){
        //List<Datarow> rawdata = new Arraylist<>();
    }

    /*
    @Test
    void calcExp() {
    }

    @Test
    void calcVar() {

    }*/
    @Test
    void testcalcVarnull(){
        List<Object> workList = new ArrayList<>();
        String machine = "a";
        workList.add(machine);
        String workflow = "b";
        workList.add(workflow);
        String task = "c";
        workList.add(task);
        int zahl = 1;
        workList.add(zahl);
        float zahl2 = 1;
        workList.add(zahl2);
        workList.add(null);//Exp
        workList.add(zahl2);

        List<DataRow> rawdata = new ArrayList<>();
        float rxpx = 1;
        float TaskinputSize = 1;
        float wchar = 1;
        DataRow row = new DataRow(machine,workflow, task, rxpx, TaskinputSize, wchar);
        rawdata.add(row);
        assertEquals(0, Main.calcVar(workList,rawdata),"Worklist-entry ist null.");
    }

    /*@Test
    void testcalcVarnull2(){
        List<Object> workList = new ArrayList<>();
        String machine = "a";
        workList.add(machine);
        String workflow = "b";
        workList.add(workflow);
        String task = "c";
        workList.add(task);
        int zahl = 1;
        workList.add(zahl);
        float zahl2 = 1;
        workList.add(zahl2);
        workList.add(zahl2);//Exp
        workList.add(zahl2);
        List<DataRow> rawdata = new ArrayList<>();
        //float rxpx = Float.parseFloat(null);//Failed here
        float TaskinputSize = 1;
        float wchar = 1;
        DataRow row = new DataRow(machine,workflow, task, rxpx, TaskinputSize, wchar);
        rawdata.add(row);
        assertEquals(Optional.ofNullable(null), Main.calcVar(workList,rawdata),"Data-entry ist null.");
    }*/

    @Test
    void testcalcVarnull3(){
        String machine = "a";
        String workflow = "b";
        String task = "c";
        List<DataRow> rawdata = new ArrayList<>();
        float rxpx = 1;
        float TaskinputSize = 1;
        float wchar = 1;
        DataRow row = new DataRow(machine,workflow, task, rxpx, TaskinputSize, wchar);
        rawdata.add(row);
        assertEquals(0, Main.calcVar(null,rawdata),"Worklist ist null.");
    }

    @Test
    void testcalcVarnull4(){
        List<Object> workList = new ArrayList<>();
        String machine = "a";
        workList.add(machine);
        String workflow = "b";
        workList.add(workflow);
        String task = "c";
        workList.add(task);
        int zahl = 1;
        workList.add(zahl);
        float zahl2 = 1;
        workList.add(zahl2);
        workList.add(zahl2);//Exp
        workList.add(zahl2);
        assertEquals(0, Main.calcVar(workList,null),"Data ist null.");
    }
    @Test
    void testcalcVarzero(){
        List<Object> workList = new ArrayList<>();
        String machine = "a";
        workList.add(machine);
        String workflow = "b";
        workList.add(workflow);
        String task = "c";
        workList.add(task);
        int zahl = 1;
        workList.add(zahl);
        float zahl2 = 1;
        workList.add(zahl2);
        float zahl3 = 0;
        workList.add(zahl3);//Exp
        workList.add(zahl2);

        List<DataRow> rawdata = new ArrayList<>();
        float rxpx = 10;
        float TaskinputSize = 1;
        float wchar = 1;
        DataRow row = new DataRow(machine,workflow, task, rxpx, TaskinputSize, wchar);
        rawdata.add(row);
        assertEquals( 100 , Main.calcVar(workList,rawdata),"Worklist-entry is 0.");
    }

    @Test
    void testcalcVarzero2(){
        List<Object> workList = new ArrayList<>();
        String machine = "a";
        workList.add(machine);
        String workflow = "b";
        workList.add(workflow);
        String task = "c";
        workList.add(task);
        int zahl = 1;
        workList.add(zahl);
        float zahl2 = 1;
        workList.add(zahl2);
        float zahl3 = 5;
        workList.add(zahl3);//Exp
        workList.add(zahl2);//Var

        List<DataRow> rawdata = new ArrayList<>();
        float rxpx = 0;
        float TaskinputSize = 1;
        float wchar = 1;
        DataRow row = new DataRow(machine,workflow, task, rxpx, TaskinputSize, wchar);
        rawdata.add(row);
        assertEquals(25, Main.calcVar(workList,rawdata),"Data-entry is 0.");
    }

    @Test
    void testcalcVar1(){
        List<Object> workList = new ArrayList<>();
        String machine = "a";
        workList.add(machine);
        String workflow = "b";
        workList.add(workflow);
        String task = "c";
        workList.add(task);
        int zahl = 1;
        workList.add(zahl);
        float zahl2 = 1;
        workList.add(zahl2);
        float zahl3 = 2;
        workList.add(zahl3);//Exp
        workList.add(zahl2);//Var

        List<DataRow> rawdata = new ArrayList<>();
        float rxpx = 5;
        float TaskinputSize = 1;
        float wchar = 1;
        DataRow row = new DataRow(machine,workflow, task, rxpx, TaskinputSize, wchar);
        rawdata.add(row);
        assertEquals(9, Main.calcVar(workList,rawdata),"Data-entry is 5 and worklist-entry is 2.");
    }

    @Test
    void testcalcVar2(){
        List<Object> workList = new ArrayList<>();
        String machine = "a";
        workList.add(machine);
        String workflow = "b";
        workList.add(workflow);
        String task = "c";
        workList.add(task);
        int zahl = 2;
        workList.add(zahl);
        float zahl2 = 1;
        workList.add(zahl2);
        float zahl3 = 2;
        workList.add(zahl3);//Exp
        workList.add(zahl2);//Var

        List<DataRow> rawdata = new ArrayList<>();
        float rxpx = 5;
        float TaskinputSize = 1;
        float wchar = 1;
        DataRow row = new DataRow(machine,workflow, task, rxpx, TaskinputSize, wchar);
        rawdata.add(row);
        float rxpx2 = 4;
        float Taskinputsize2 = 1;
        float wchar2 = 1;
        DataRow row2 = new DataRow(machine, workflow, task, rxpx2, Taskinputsize2, wchar2);
        rawdata.add(row2);
        assertEquals(6.5, Main.calcVar(workList,rawdata),"Variance is 6.5.");
    }

    @Test
    void testcalcVar3(){
        List<Object> workList = new ArrayList<>();
        String machine = "a";
        workList.add(machine);
        String workflow = "b";
        workList.add(workflow);
        String task = "c";
        workList.add(task);
        int zahl = 3;//Runs
        workList.add(zahl);
        float zahl2 = 1;
        workList.add(zahl2);
        float zahl3 = 2;
        workList.add(zahl3);//Exp
        workList.add(zahl2);//Var

        List<DataRow> rawdata = new ArrayList<>();
        float rxpx = 5;
        float TaskinputSize = 1;
        float wchar = 1;
        DataRow row = new DataRow(machine,workflow, task, rxpx, TaskinputSize, wchar);
        rawdata.add(row);
        float rxpx2 = 4;
        float Taskinputsize2 = 1;
        float wchar2 = 1;
        DataRow row2 = new DataRow(machine, workflow, task, rxpx2, Taskinputsize2, wchar2);
        rawdata.add(row2);
        float rxpx3 = 6;
        float Taskinputsize3 = 1;
        float wchar3 = 1;
        DataRow row3 = new DataRow(machine, workflow, task, rxpx3, Taskinputsize3, wchar3);
        rawdata.add(row3);
        assertEquals((float) 29 /3, Main.calcVar(workList,rawdata),"Variance is 6.5.");
    }
}