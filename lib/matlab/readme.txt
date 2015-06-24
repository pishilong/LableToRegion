
Put all the files to below folder to use or execute:
%YourPath\SLEP_package_4.1\SLEP\functions\L1\L1R\


LeastRBatch.m is wrapper function to execute LeastR in batch mode. this targets providing an interface for calling from Jar package.
Running deploytool adding LeastRBatch.m would automatically add LeastR.m sll_opts dependencies.


LeastRBatchTest.m is simple test to test LeastRBatch.m

LeastRBatchTestFromFiles is a test to test LeastR in batch mode reading in matrix from files.

sll_opts.m is dependent file which is needed while running or packaing LeastRBatch.m into jar package. 


NOTE:
LeastRBatchTestFromFiles.m would need input files AMatrix.txt YMatrix.txt which are generated from genEnhancedAYInBatch method in RegionMatrix.java

