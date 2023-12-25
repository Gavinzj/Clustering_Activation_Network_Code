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
ClusterExtractor doClustering_Active_Online 0.7 7 0.3 3 0 100 20 3<br/>
0<br/>

<br/>
The explanation for the commands: <br/>
Line 1: data_set_name <br/>
Line 2: ClusterExtractor which_method_to_run theta# iteration_No# epsilon# mu# min_time# max_time# trial_No# <br/>
Line 3: 0

(NOTE: Please remember to type "0" at the end, which is for terminating the program, then click Enter)  

------------------------------------------------------------------------------------------------------------

Run the java program java Exp/Panel
Then type in the commands including dataset, tasks

Example: To sample and save 5 pyramids sets for dataset lastfm_asia, each set consists of 4 pyramids.

Run:
java Exp/Panel
Type:
lastfm_asia
SetPyramid samplingPyramidSet 5 4
0

------------------------------------------------------------------------------------------------------------
<<<<<<<<<<<<<<<<<< ON SAMPLING PYRAMID SETS>>>>>>>>>>>>>>>>>>>>>
Format: SetPyramid samplingPyramidSet trials# pyramidNum#

Description: Sampling and save 5 pyramids sets, each of which consists of 4 pyramids, for later experiments.

Example: 
lastfm_asia
SetPyramid samplingPyramidSet 5 4
0

