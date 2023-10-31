#  Stochastic scheduler

## Installation
### Custom scheduler
* Clone this project
* Build it with maven:
  * In IntelliJ
    * open the maven tab (the "m" symbol on the right from the code)
  ![image](https://github.com/kulagins/stochastic-scheduler/assets/75316111/5837c4ca-b7ca-4544-8353-87b8386bc743)
 
  
    * Under "Lifecycle", double-click "clean", then "install"
  * Without an IDE, type "mvn clean", then "mvn install"
  * Check that the procecess finishes without errors
* Run: in IntelliJ, open "src/java/controller", right-click on "Main" and choose "Run".
* The setup is correctly completed if you see additional outputs in the console (no achieved as of now).
### Kubernetes
*  In this and next sections, substitute your-path with the location of this folder on your computer
*  Start the cluster
*  Create the description for the custom scheduler: "kubectl apply -f your-path/new-scheduler.yaml"
*  In **all next .yaml files** in this section: check the paths before executing and substitute with your paths if necessary
* Create the persistent volume and its claim: "kubectl apply -f your-path/pv.yaml", "kubectl apply -f your-path/pvc.yaml"
  * Check is the claim is in "BOUND" status: kubectl get pvc
* Create the debugger pod that allows us to change properties of future workflows directly (workaround): "kubectl apply -f your-path/testpod.yaml"
  * Check if the pod is running kubectl get pods and look for the ubuntu-sleep-pod
  * Enter the inside of the pod with "kubectl exec -it ubuntu-sleep-pod -- /bin/bash"
  * There should be a folder called "data"
*  Create clusterrole: "kubectl apply -f your-path/clusterrole.yaml"
### Nextflow
* If you have any errors in this part, try out another workflow from these: rnaseq, sarek TBD more
* Start the workflow with  `nextflow kuberun nf-core/sarek -v myclaim:/data --outdir ./ -remoteProfile test`
 *  In the best case, it should run for a while and end with something like:
   ```
[12/7ca26a] Submitted process > NFCORE_SAREK:SAREK:VCF_QC_BCFTOOLS_VCFTOOLS:VCFTOOLS_TSTV_COUNT (test)
[8f/cbfded] Submitted process > NFCORE_SAREK:SAREK:CUSTOM_DUMPSOFTWAREVERSIONS (1)
[70/8d66c9] Submitted process > NFCORE_SAREK:SAREK:MULTIQC
-[nf-core/sarek] Pipeline completed successfully-

 ```
We are still not using our custom scheduler!
 ### Running everything together: workaround
 If during `nextflow kuberun nf-core/sarek -v myclaim:/data --outdir ./ -remoteProfile test`, the Tarema code does not write outputs, then the scheduler name has not been passed properly.
 To work around this issue:
  * In CurrentPodNodeStatus.java, comment out line 58.
  * Put a debug break point in eventReceived (line 62)
  * Restart the nextflow workflow and await when the program goes to this breakpoint
    
 ### Running everything together correctly: TBD
 We need to force nextflow to use the custom scheduler instead of the default kubernetes scheduler.
 This should theoretically be possible in the nextflow call (nextflow smth smth -scheduler-name new-scheduler ), but TBD if this is possible and how.
 * Alternative: change nextflow config of the workflows that we will run.
 * For that, go inside the debugger pod: "kubectl exec -it ubuntu-sleep-pod -- /bin/bash"
  * Change to /data/projects/nf-core/sarek
  * Try "nano nextflow.config" (or vim if your prefer it)
   * If no nano is available, do "apt-get update & apt-get install nano", then rerun the nextflow call.
    To Be Done: Fix problems with the custom scheduler. When the correct scheduler is chosen, the custom scheduler will print messages on the console.


The following is a collection of ideas how to define the scheduler name in the kubernetes call.

* Change to /data/projects/nf-core/sarek. At the  end of nextflow.config, add this code:
    ```
    k8s{
        scheduler{
                name=new-scheduler
        }

    }
    ```
 * Change to /data/projects/nf-core/sarek/conf 
 * open test.config with nano (this defines how the profile test is being executed) and add this at the end:
  ```
k8s {
   scheduler {
      name = 'new-scheduler'
   }  
}
   ```

    UPD: another idea is to add this to line 155 in nextflow.config, da wo `profiles` anfängt:
```
  newprof {
        includeConfig 'conf/test.config'
        k8s.scheduler.strategy = "new-scheduler"
    }

```
und es dann mit -remoteProfile newprof zu rufen. aber der neue Scheduler wird immer noch nicht aufgerufen

Another idea: there might be something wrong with new-scheduler.yaml. Maybe something needs to be substitutes around lines 55-60


Another idea: move the compiled jar to /usr/local/bin and name it kube-scheduler. Still doesn't help
