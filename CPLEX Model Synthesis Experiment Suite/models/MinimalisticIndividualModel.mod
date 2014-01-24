/*********************************************
 * OPL 12.4 Model
 * Author: alexander
 * Creation Date: Jan 13, 2014 at 11:38:25 AM
 *********************************************/
int lastSimStep = 10;
range TIMERANGE = 0..lastSimStep;

float Pmin = ...; float Pmax = ...; float fixedRamp = ...;
float demand[TIMERANGE] = ...;

dvar float+ production[TIMERANGE];

subject to {
  forall (t in 0..lastSimStep-1 ) {
    production[t] >= Pmin;
    production[t] <= Pmax;
    abs(production[t] - production[t+1]) <= fixedRamp;  
  }   
} 
