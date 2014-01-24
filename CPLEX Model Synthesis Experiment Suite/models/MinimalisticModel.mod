/*********************************************
 * OPL 12.4 Model
 * Author: alexander
 * Creation Date: Jan 13, 2014 at 11:38:25 AM
 *********************************************/
int lastSimStep = 10;
range TIMERANGE = 0..lastSimStep;

{string} PowerPlants = ...;

tuple PowerPlantData {
	float minimal;
	float maximal;
	float fixedRamp;
};

PowerPlantData plants[PowerPlants] = ...;

float demand[TIMERANGE] = ...;

dvar float+ production[PowerPlants][TIMERANGE];
dexpr float totalProduction[t in TIMERANGE] = 
            (sum( p in PowerPlants ) (production[p][t]));
            
minimize
  sum ( t in TIMERANGE ) abs(demand[t] - totalProduction[t]);

subject to {
  forall ( p in PowerPlants, t in 0..lastSimStep-1 ) {
    production[p][t] >= plants[p].minimal;
    production[p][t] <= plants[p].maximal;
    abs(production[p][t] - production[p][t+1]) <= plants[p].fixedRamp;  
  }   
} 