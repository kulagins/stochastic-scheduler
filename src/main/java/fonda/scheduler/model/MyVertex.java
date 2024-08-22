package fonda.scheduler.model;

//import java.util.*;

public class MyVertex {
        public String label;
        public double expected;
        public double variance;

        public double sb_levelexp;

        public double sb_levelvar;

        public boolean pushed;

        public double completiontimeexp;

        public double completiontimevar;

        public MyVertex(){

        }
        public MyVertex(String name){
                this.label = name;
        }

        public void setLabel(String label){
            this.label = label;
        }
        public String getLabel(){
            return label;
        }

        public void setExpected(double expected) {
            this.expected = expected;
        }
        public double getExpected(){
            return expected;
        }

        public void setVariance(double variance) {
            this.variance = variance;
        }

        public double getVariance() {
            return variance;
        }

        public void setSb_levelexp(double sb_levelexp){this.sb_levelexp = sb_levelexp;}

        public double getSb_levelexp(){return sb_levelexp;}

        public void setSb_levelvar(double sb_levelvar){this.sb_levelvar = sb_levelvar;}

        public double getSb_levelvar(){return sb_levelvar;}

        public void setPushed(boolean pushed){this.pushed = pushed;}

        public boolean getPushed(){return this.pushed;}

        public void setCTtimeexp(double ctexp){this.completiontimeexp = ctexp;}
        public double getCTtimeexp(){return this.completiontimeexp;}

        public void setCTtimevar(double ctvar){this.completiontimevar = ctvar;}
        public double getCTtimevar(){return this.completiontimevar;}
}
