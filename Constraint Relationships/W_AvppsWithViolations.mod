/*********************************************
 * OPL 12.2 Model
 * Author: steghoja
 * Creation Date: 24.05.2011 at 11:22:27
 *********************************************/

using CPLEX;
//using CP;

range TIMERANGE = 0..95;

{string} ControllablePlants = ...;
{string} SolarPlants = ...;
{string} WindPlants = ...;
tuple powerPlantData {
	float minimalProduction;
	float maximalProduction;
	float rateOfChange;
	float pricePerKWh;
//	int startUpTime;
//	int continuousOperationTime;
};

powerPlantData PowerPlant[ControllablePlants] = ...;
powerPlantData SolarPlant[SolarPlants] = ...;
powerPlantData WindPlant[WindPlants] = ...;

float energyConsumption[TIMERANGE] = ...;
float solarRadiationFactor[TIMERANGE] = ...;
float windStrengthFactor[TIMERANGE] = ...;

// The scheduled energy production per controllable plant in each time step.
dvar float+ energyProduction[ControllablePlants][TIMERANGE];

// The amount of energy missing or superfluous to meet the demand in each time step.
dvar float violation[TIMERANGE];

// Determines whether or not a power plant is running in each time step.
dvar boolean powerPlantRunning[ControllablePlants][TIMERANGE];

dexpr float totalProduction[t in TIMERANGE] = (sum( p in ControllablePlants ) (energyProduction[p][t]) +
  			sum ( p in SolarPlants ) (solarRadiationFactor[t] * SolarPlant[p].maximalProduction ) +
  			sum ( p in WindPlants ) (windStrengthFactor[t] * WindPlant[p].maximalProduction ));
//dexpr float prodDifference[p in ControllablePlants][t in 0..card(TIMERANGE)-2] = abs(energyProduction[p][t] - energyProduction[p][t+1]);

minimize
  sum ( t in TIMERANGE ) abs(violation[t]); //+ sum( p in ControllablePlants, t in 0..94 ) prodDifference[p][t];

range PENALTYRANGE = 1..3;
dvar int+ penalty[PENALTYRANGE];
minimize 
	 sum ( i in PENALTYRANGE) penalty[i];


subject to {
  forall ( p in ControllablePlants ) {
    forall ( t in TIMERANGE ) {
      	// Each plants production must always be greater than its minimal production
  		minimalProductionConstraint:
  		  	(powerPlantRunning[p][t] == true) => 
  		  		PowerPlant[p].minimalProduction <= energyProduction[p][t];
  		// Each plants production may never exceed its maximal production
  		maximalProductionConstraint:
  		  	(energyProduction[p][t] <= PowerPlant[p].maximalProduction) || (penalty[3] == 4);
  		// If a power plant is not running, its output has to be zero
  		notRunningNoOutputConstraint:
  		   (powerPlantRunning[p][t] == false) => (energyProduction[p][t] == 0);
	}	
	forall ( t in 0..card(TIMERANGE)-2 ) {
	  // The percentual change in output must always be lower than the rate of change 
	  rateOfChangeConstraint:
	    abs(energyProduction[p][t] - energyProduction[p][t+1]) <= energyProduction[p][t] * PowerPlant[p].rateOfChange;  
	  // Limit the rate of change to 20% to avoid oscillating plants (weak constraint)
	  limitRateOfChangeConstraint:
	    ((powerPlantRunning[p][t] == true) => 
	    abs(energyProduction[p][t] - energyProduction[p][t+1]) <= energyProduction[p][t] * 0.2) || (penalty[1] == 2);
	}
  }  
  forall ( t in TIMERANGE ) {
    // Demand must always be satisfied (violation[t] allows deviation from full satisfaction)
  	satisfyDemandConstraint:
  		totalProduction[t] + violation[t] ==  energyConsumption[t];
  	// Limits the violations to 10% of total production, thus forcing the system to meet demand exactly in every time step (weak)
  	limitViolationsConstraint:
  		(abs(violation[t]) <= totalProduction[t]/15) || (penalty[2] == 1);
 
  }    	
} 

/* RELATIONSHIPS 
 * limitRateOfChangeConstraint >> limitViolationsConstraint
 maximalProductionConstraint   >>  limitRateOfChangeConstraint
 
 * ENDRELATIONSHIPS
 */

/* Postprocessing */
tuple results {
   float energyProduction;
   float energyConsumption;
   float violation;
   float violationPercent;
}

results Result[t in TIMERANGE] = 
		<	
			totalProduction[t],
  			energyConsumption[t],
  			violation[t],
  			(violation[t]/totalProduction[t]) * 100
  		>;
execute DISPLAY {
  writeln("results=", Result);
}
