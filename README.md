Code: code is available under the folder code/src/. <br/>
Data: data is available under the folder code/Data/. <br/>

Instructions on running the code: <br/>

To run the code, please run Exp/Panel.java and then input the commands based on the tasks. Below are the details and several sample tasks are provided. <br/>

We included the parameters in a soft-coded way. The program Exp/Panel.java provides a command line interface for a user to input the commands in the console. Specifically, when the program Exp/Panel.java run, a user is required to input the information in the console including: <br/>
1) data_set_name,  <br/>
2) which_method_to_run,  <br/>
3) parameters.  <br/>

------------------------------------------------------------------------------------------------------------
Sample Task-1. Do the clustering using method ANCO on data set lastfm_asia.

Commands:<br/>
lastfm_asia<br/>
ClusterExtractor doClustering_Active_Online 0.3 3 0 100 3<br/>
0<br/>

<br/>
The explanation for the commands: <br/>
Line 1: data_set_name <br/>
Line 2: ClusterExtractor which_method_to_run epsilon# mu# min_time# max_time# trial_No# <br/>
Line 3: 0<br/>

Line 1 specifies the data set "lastfm_asia". The file paths will then point to the folder corresponding to the data set "lastfm_asia".<br/>
Line 2 specifies the task and related parameters. The task is to "Extract the clustering" using the method "ANCO". The values of parameters epsilon and mu is "0.3" and "3", respectively. The time varies from "0" to "100". "We will do the clustering "3" times.<br/>
Line 3 terminates the program.<br/>
(NOTE: Please remember to type "0" at the end, which is for terminating the program, then click Enter)  <br/>

Output:<br/>
The clustering results obtained would be stored under the folder Data/lastfm_asia/extractedCluster/degree order/.<br/>

------------------------------------------------------------------------------------------------------------
Sample Task-2. Do the clustering using method ANCOR on data set lastfm_asia.

Commands:<br/>
lastfm_asia<br/>
ClusterExtractor doClustering_Active_Online_LocalUpdate 0.3 3 0 100 3<br/>
0<br/>

<br/>
The explanation for the commands: <br/>
Line 1: data_set_name <br/>
Line 2: ClusterExtractor which_method_to_run epsilon# mu# min_time# max_time# trial_No# <br/>
Line 3: 0<br/>

Line 1 specifies the data set "lastfm_asia". The file paths will then point to the folder corresponding to the data set "lastfm_asia".<br/>
Line 2 specifies the task and related parameters. The task is to "Extract the clustering" using the method "ANCOR". The values of parameters epsilon and mu is "0.3" and "3", respectively. The time varies from "0" to "100". "We will do the clustering "3" times.<br/>
Line 3 terminates the program.<br/>
(NOTE: Please remember to type "0" at the end, which is for terminating the program, then click Enter)  <br/>

Output:<br/>
The clustering results obtained would be stored under the folder Data/lastfm_asia/extractedCluster/degree order/.

------------------------------------------------------------------------------------------------------------
Sample Task-3. Do the clustering using method ANCF on data set lastfm_asia.

Commands:<br/>
lastfm_asia<br/>
ClusterExtractor doClustering_Active 0.3 3 0 100 3<br/>
0<br/>

<br/>
The explanation for the commands: <br/>
Line 1: data_set_name <br/>
Line 2: ClusterExtractor which_method_to_run epsilon# mu# min_time# max_time# trial_No# <br/>
Line 3: 0<br/>

Line 1 specifies the data set "lastfm_asia". The file paths will then point to the folder corresponding to the data set "lastfm_asia".<br/>
Line 2 specifies the task and related parameters. The task is to "Extract the clustering" using the method "ANCF". The values of parameters epsilon and mu is "0.3" and "3", respectively. The time varies from "0" to "100". "We will do the clustering "3" times.<br/>
Line 3 terminates the program.<br/>
(NOTE: Please remember to type "0" at the end, which is for terminating the program, then click Enter)  <br/>

Output:<br/>
The clustering results obtained would be stored under the folder Data/lastfm_asia/extractedCluster/degree order/.

------------------------------------------------------------------------------------------------------------
Sample Task-4. Calculate the running time of method ANCO on data set lastfm_asia.

Commands:<br/>
lastfm_asia<br/>
ClusteringTime clusteringTime_Active_Online 0.3 3 0 100 3<br/>
0<br/>

<br/>
The explanation for the commands: <br/>
Line 1: data_set_name <br/>
Line 2: ClusteringExp which_method_to_run epsilon# mu# min_time# max_time# trial_No# <br/>
Line 3: 0<br/>

Line 1 specifies the data set "lastfm_asia". The file paths will then point to the folder corresponding to the data set "lastfm_asia".<br/>
Line 2 specifies the task and related parameters. The task is to "Calculate the clustering time" of method "ANCO". The values of parameters epsilon and mu is "0.3" and "3", respectively. The time varies from "0" to "100". "We will do the clustering "3" times.<br/>
Line 3 terminates the program.<br/>
(NOTE: Please remember to type "0" at the end, which is for terminating the program, then click Enter)  <br/>

Output:<br/>
The clustering results obtained would be stored under the folder Data/lastfm_asia/extractedCluster/degree order/.

------------------------------------------------------------------------------------------------------------
Sample Task-5. Construct 5 indices for dataset lastfm_asia, each set consists of 4 pyramids.

Commands:<br/>
lastfm_asia<br/>
SetPyramid samplingPyramidSet 5 4<br/>
0<br/>

<br/>
The explanation for the commands: <br/>
Line 1: data_set_name <br/>
Line 2: SetPyramid which_method_to_run index_No# pyramid_No# <br/>
Line 3: 0<br/>

Line 1 specifies the data set "lastfm_asia". <br/>
Line 2 specifies the task and related parameters.<br/>
Line 3 terminates the program.<br/>

Output:<br/>
The indices would be stored under the folder Data/lastfm_asia/setPyramid/.
