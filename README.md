# Clustering_Activation_Network_Code

------------------------------ How to run ------------------------------------------

entrance:
Exp/Panel

command: 

Run the java program java Exp/Panel
Then type in the commands including dataset, tasks

Example: To sample and save 5 pyramids sets for dataset lastfm_asia, each set consists of 4 pyramids.

Run:
java Exp/Panel
Type:
lastfm_asia
SetPyramid samplingPyramidSet 5 4
0

<<<<<<<<<<<<<<<<<< ON SAMPLING PYRAMID SETS>>>>>>>>>>>>>>>>>>>>>
Format: SetPyramid samplingPyramidSet trials# pyramidNum#

Description: Sampling and save 5 pyramids sets, each of which consists of 4 pyramids, for later experiments.

Example: 
lastfm_asia
SetPyramid samplingPyramidSet 5 4
0

<<<<<<<<<<<<<<<<<< ON CLUSTERING (ANCO)  >>>>>>>>>>>>>>>>>>>>>
Example (CLUSTERING):
lastfm_asia
ClusterExtractor doClustering_Active_Online 4 0.7 7 3 16 0.3 3 0 100 20 3
0

