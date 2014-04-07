Synthesis Constraint Models for Distributed Energy Management
==========================

This readme file explains all necessary steps to execute
experiments presented in the SEN-MAS-2014 paper.
Please note that the experiments were actually conducted 
on a cluster with workers having 4 cores and 32 GB RAM each.

However, with our efforts we hope to make it easier to validate and 
communicate our approach. 

The synthesis process itself (describe in section 5) can be used without
CPLEX licenses, and is exemplified in the project "CSP Model Synthesis"

Also, the transformation from CSOPs with Constraint Relationships
to solvable weighted-CSP instances can be inspected and tested in the project
"CPLEX Constraint Relationships".

However, for the projects "CPLEX Model Synthesis Experiment Suite" and "Multi-objective optimisation",
we highly depend on CPLEX libraries.
==================================
= CAVEAT CPLEX

As our approach highly depends on 
a working IBM ILOG CPLEX installation
you need a working version installed (tested with CPLEX 12.4)

-> if interested, go to http://www-01.ibm.com/software/commerce/optimization/cplex-optimizer/
   for a trial version and/or academic/student licenses
   Install and include relevant CPLEX directories to Path Variable when asked
==================================

1. Download and install eclipse from http://www.eclipse.org/downloads/
   (we used eclipse for Java developers 2.0, eclipse 4.3)
2. Get the source code from github
3. Import all projects into eclipse
4. Configuring the projects to work with CPLEX
   Select the project "CPLEX Model Synthesis Experiment Suite"
   a) Linux (tested with Ubuntu 13.10): 
      Open "SingleExperiment.launch" with your favourite text editor
      Replace
      <mapEntry key="LD_LIBRARY_PATH" value="/home/alexander/Programs/CPLEX/opl/bin/x86-64_sles10_4.1"/>
      with your appropriate CPLEX path (should contain "libcplex124.so" etc.)      
   b) Windows (tested with Windows 7):
      Make sure that the environment variables CPLEX_STUDIO_BINARIES124 and CPLEX_STUDIO_DIR124 are set properly
      and run de.uniaugsburg.isse.ExperimentSeries
      
   Add the file "oplall.jar" from your CPLEX installation to the "lib" directories in CPLEX Model Synthesis Experiment Suite and
   Multi-objective optimisation
5. If you see output containing "solving : /tmp/step_model6284360767749019205.mod" everything is working
6. Upon finishing, the results are stored in the folder "results"

Alexander Schiendorfer,
Augsburg University
----------------------------------

http://www.informatik.uni-augsburg.de/lehrstuehle/swt/se/staff/aschiendorfer/
