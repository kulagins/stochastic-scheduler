#  Stochastic scheduler

## Installation
### Custom scheduler
* Clone this project
* Build it with maven:
  * In IntelliJ
    * open the maven tab (the "m" symbol on the right from the code)
    * Under "Lifecycle", double-click "clean", then "install"
  * Without an IDE, type "mvn clean", then "mvn install"
  * Check that the procecess finishes without errors
* Run: in IntelliJ, open "src/java/controller", right-click on "Main" and choose "Run".
### Kubernetes
*  In this and next sections, substitute your-path with the location of this folder on your computer
*  Start the cluster
*  Create the description for the custom scheduler: "kubectl apply -f your-path/new-scheduler.yaml"
*  In **all next .yaml files** in this section: check the paths before executing and substitute with your paths if necessary
* Create the persistent volume and its claim: "kubectl apply -f your-path/pv.yaml", "kubectl apply -f your-path/pvc.yaml"
  * Check is the claim is in "BOUND" status: kubectl get pvc
* Create the debugger pod that allows us to change properties of future workflows directly (workaround): "kubectl apply -f ~/workspace/tarema-k8-scheduler/testpod.yaml"
  * Check if the pod is running kubectl get pods and look for the ubuntu-sleep-pod
  * Enter the inside of the pod with "kubectl exec -it ubuntu-sleep-pod -- /bin/bash"
  * There should be a folder called "data"
*  Create clusterrole: "kubectl apply -f your-path/clusterrole.yaml"
### Nextflow
* If you have any errors in this part, try out another workflow from these: rnaseq, sarek TBD more
* Start the workflow with " nextflow kuberun nf-core/sarek -v myclaim:/data --outdir ./ -remoteProfile test"
 *  In the best case, it should run for a while and end with something like:
   ```
[12/7ca26a] Submitted process > NFCORE_SAREK:SAREK:VCF_QC_BCFTOOLS_VCFTOOLS:VCFTOOLS_TSTV_COUNT (test)
[8f/cbfded] Submitted process > NFCORE_SAREK:SAREK:CUSTOM_DUMPSOFTWAREVERSIONS (1)
[70/8d66c9] Submitted process > NFCORE_SAREK:SAREK:MULTIQC
-[nf-core/sarek] Pipeline completed successfully-

 ```
We are still not using our custom scheduler!

 ### Running everything together: TBD
 We need to force nextflow to use the custom scheduler instead of the default kubernetes scheduler.
 This should theoretically be possible in the nextflow call (nextflow smth smth -scheduler-namen )
 * Go inside the debugger pod: "kubectl exec -it ubuntu-sleep-pod -- /bin/bash"
  * Change to /data/projects/nf-core/sarek
  * Try "nano nextflow.config" (or vim if your prefer it)
   * If no nano is available, do "apt-get update & apt-get install nano"
  * At the  end of nextflow.config, add this code:
    ```
    k8s{
        scheduler{
                name=new-scheduler
        }

    }
    ```

    then rerun the nextflow call.
    To Be Done: Fix problems with the custom scheduler. When the correct scheduler is chosen, the custom scheduler will print messages on the console.
