/*********************************************
 * OPL 12.2 Model
 * Author: steghoja
 * Creation Date: 03.05.2012 at 14:59:51
 * AgentSet
 plants
 *********************************************/
 {string} plants = ...;

 float P_max[plants] = ...;
 int LAST_SIMULATION_STEP = ...;
 range TIMERANGE = 0..LAST_SIMULATION_STEP;
 float loadCurve[TIMERANGE] = ...;
 
 dvar float+ production[plants][TIMERANGE];
 dexpr float totalProduction[t in TIMERANGE] = sum ( p in plants ) production[p][t];
 dexpr float loadFactor[p in plants][t in TIMERANGE] = production[p][t] / P_max[p];
 dexpr float minLoadFact[t in TIMERANGE] = min(p in plants) loadFactor[p][t];
 dexpr float maxLoadFact[t in TIMERANGE] = max(p in plants) loadFactor[p][t];
 dexpr float violation = sum(t in TIMERANGE) abs(totalProduction[t]-loadCurve[t]);
 
 minimize violation;
 
 subject to {
   forall ( t in TIMERANGE ) {
     oc1: minLoadFact[t] >= 0.4; 
     oc2: maxLoadFact[t] <= 0.6; 
     forall(p in plants) {
 		production[p][t] <= P_max[p];
 	 }	
   }        
 };   

/* SOFT-CONSTRAINTS
oc1
oc2
End SOFT-CONSTRAINTS */