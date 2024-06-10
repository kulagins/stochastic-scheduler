package fonda.scheduler.model;

import java.util.*;

public class MyVertex {
        public static String id;
        public String label;
        public float expected;
        public float variance;

        public float sb_levelexp;

        public float sb_levelvar;

        public boolean pushed;

        public float completiontimeexp;

        public float completiontimevar;

        private MyVertex(){
            //import
        }
        public MyVertex(String label){
            this.label = label;
        }
        public void setLabel(String label){
            this.label = label;
        }
        public String getLabel(){
            return label;
        }

        public void setExpected(float expected) {
            this.expected = expected;
        }
        public float getExpected(){
            return expected;
        }

        public void setVariance(float variance) {
            this.variance = variance;
        }

        public float getVariance() {
            return variance;
        }

        public void setSb_levelexp(float sb_levelexp){this.sb_levelexp = sb_levelexp;}

        public float getSb_levelexp(){return sb_levelexp;}

        public void setSb_levelvar(float sb_levelvar){this.sb_levelvar = sb_levelvar;}

        public float getSb_levelvar(){return sb_levelvar;}

        public void setPushed(boolean pushed){this.pushed = pushed;}

        public boolean getPushed(){return this.pushed;}

        public void setCTtimeexp(float ctexp){this.completiontimeexp = ctexp;}
        public float getCTtimeexp(){return this.completiontimeexp;}

        public void setCTtimevar(float ctvar){this.completiontimevar = ctvar;}
        public float getCTtimevar(){return this.completiontimevar;}
}
