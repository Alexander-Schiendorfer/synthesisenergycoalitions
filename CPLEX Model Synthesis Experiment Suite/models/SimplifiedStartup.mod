/*********************************************
 * Experimental version of Avpp with violation
 * based on CPLEX engine hence using decision
 * variables for consRun/consStop since CPLEX
 * cannot deal with nonlinear dexpr constraints
 * or recursive definitions as would be required
 * for dexpr consRun (AS)
 * 
 * OPL 12.2 Model
 * Author: steghoja
 * Creation Date: 24.05.2011 at 11:22:27
 *********************************************/

using CPLEX;
//using CP;

// constant definitions
int lastSimStep = 10;
int lastConsideredStep = 15;
 
int inf = lastSimStep; // "infinity" for countdown
 
// this range serves as the objective 
range TIMERANGE = 0..lastSimStep;
// considered time range to avoid overflows
range CONSIDERED_RANGE = 0..lastConsideredStep ;

// TODO {int} TIMERANGE = ...;

{string} ControllablePlants = ...;

int maxBps = ...; // maximal number of breakpoints for all power plants for startup

tuple powerPlantData {
	float minimalProduction;
	float maximalProduction;
	float rateOfChange;
	float fixedRamp;
	float pricePerKWh;
	int numBPs;               // number of breakpoints for pwlinear func
	float slopesStartUp[1..maxBps+1];
	float breakpointsStartUp[1..maxBps];
	float fAtZero;            // function value at 0
	int consRunningTimeInit;
	int consStandingTimeInit;
	int minDownTime;          // minimal timesteps the plant has to be off
	int minUpTime;            // minimal timesteps the plant has to be on
	int countDownInit;        // initial count down if plant was about to start
};

powerPlantData PowerPlant[ControllablePlants] = ...;

// ======================================================
// BEGIN of start up function area

// emulating an enumeration for the states in the transition system
int IDLE = 0;
int STARTING = 1;
int STOPPING = 2;
int ON = 3;
 
// contains the states for each power plant and time step
dvar int powerPlantState[ControllablePlants][TIMERANGE] in 0..3;

// Determines whether or not a power plant is running in each time step.
dexpr int powerPlantRunning[p in ControllablePlants][t in TIMERANGE] = powerPlantState[p][t] == ON;
  
int initialCountdown = 2;

// two times the same breakpoint (32, 32) indicate a step
// rather than a slope at slope[i+1] with i being the index
// of the first occurrence of breakpoint[i]
pwlFunction startUpFunction[p in ControllablePlants] = piecewise(i in 1..PowerPlant[p].numBPs) 
{
   PowerPlant[p].slopesStartUp[i]->PowerPlant[p].breakpointsStartUp[i]; PowerPlant[p].slopesStartUp[PowerPlant[p].numBPs+1] 
} (0, PowerPlant[p].fAtZero);

// decision expressions defining running time and consecutive runs / stops

// decision variables 
// decision expressions for cons - RUN
dvar int consRun[p in ControllablePlants][t in TIMERANGE] in 0 .. lastConsideredStep ;

// decision expressions for cons - STOP
dvar int consStop[p in ControllablePlants][t in TIMERANGE] in 0 .. lastConsideredStep;

// the count down variable denoting the steps needed to wait
dvar int countdown[ControllablePlants][TIMERANGE]; 

// start up time depending on stop time 
dexpr float startUpSteps[p in ControllablePlants][t in TIMERANGE] = startUpFunction[p](consStop[p][t]);

dvar boolean startSignals[p in ControllablePlants][t in CONSIDERED_RANGE] in 0 .. 1;

dvar boolean offSignals[p in ControllablePlants][t in CONSIDERED_RANGE] in 0 .. 1;

// this one is used for tests

// END of start up function area
// ======================================================

// START testing area
int loadProfile[CONSIDERED_RANGE] = [0,0,0,0,0,0,1,1,1,1,1,1,1];
// END of testing area
// ======================================================
float energyConsumption[TIMERANGE] = ...;

// The scheduled energy production per controllable plant in each time step.
dvar float+ energyProduction[ControllablePlants][TIMERANGE] in 0..600;

dexpr float totalProduction[t in TIMERANGE] = (sum( p in ControllablePlants ) (energyProduction[p][t]));

// The amount of energy missing or superfluous to meet the demand in each time step.
dexpr float violation[t in TIMERANGE] = abs(energyConsumption[t] - totalProduction[t]);

minimize
   sum ( t in TIMERANGE ) violation[t]; //+ sum( p in ControllablePlants, t in 0..94 ) prodDifference[p][t];
  //  sum(t in TIMERANGE, p in ControllablePlants) abs(loadProfile[t] - powerPlantRunning[p][t]); 

subject to {

  forall ( p in ControllablePlants ) {
	// TEST
	 sum(t in TIMERANGE) powerPlantRunning[p][t] >= 1;
	// -----

	countdown[p][0] == PowerPlant[p].countDownInit;
	// constraints for additional decision variables
	consRun[p][0] == PowerPlant[p].consRunningTimeInit;
    consStop[p][0] == PowerPlant[p].consStandingTimeInit;
    
	(PowerPlant[p].consRunningTimeInit >= 1) == (powerPlantState[p][0] == ON);
	
	// if the plant is off but already has a count down, switch to state isStarting
    (PowerPlant[p].consRunningTimeInit == 0 && PowerPlant[p].countDownInit <= inf-1) == (powerPlantState[p][0] == STARTING); 
    
    // otherwise it is off
    (PowerPlant[p].consRunningTimeInit == 0 && PowerPlant[p].countDownInit == inf) == (powerPlantState[p][0] == IDLE); 
    
     // minimal running/standing time constraint
    forall (t in 1..lastSimStep) {
	     minStopTimeConstraint:
	       (powerPlantRunning[p][t] == 1 && powerPlantRunning[p][t-1] == 0) => consStop[p][t-1] - PowerPlant[p].minDownTime >= 0;
    	 minRunTimeConstraint: 
    	  (powerPlantRunning[p][t] == 0 && powerPlantRunning[p][t-1] == 1) => consRun[p][t-1] - PowerPlant[p].minUpTime >= 0;	
    	 startUpAtMin:
  		    ((powerPlantRunning[p][t-1] == 0) && (powerPlantRunning[p][t] == 1)) => (energyProduction[p][t] == PowerPlant[p].minimalProduction);
  		 stopAtMin:
  		    ((powerPlantRunning[p][t-1] == 1) && (powerPlantRunning[p][t] == 0)) => (energyProduction[p][t-1] == PowerPlant[p].minimalProduction);
  		 
	}   
	
	
    // consRun / consStop decision variable constraints
    forall(t in 1..lastSimStep) {
      (powerPlantRunning[p][t] == 1 && consRun[p][t] == (1 + consRun[p][t-1])) || (powerPlantRunning[p][t] == 0 && consRun[p][t] == 0);
      (powerPlantRunning[p][t] == 0 && consStop[p][t] == (1 + consStop[p][t-1])) || (powerPlantRunning[p][t] == 1 && consStop[p][t] == 0);
    }      
	

    forall ( t in TIMERANGE ) {
      	// Each plants production must always be greater than its minimal production
  		minimalProductionConstraint:
  		  	(powerPlantRunning[p][t] == true) => 
  		  		PowerPlant[p].minimalProduction <= energyProduction[p][t];
  		// Each plants production may never exceed its maximal production
  		maximalProductionConstraint:
  		  	energyProduction[p][t] <= PowerPlant[p].maximalProduction;
  		// If a power plant is not running, its output has to be zero
  		notRunningNoOutputConstraint:
  		   (powerPlantRunning[p][t] == false) => (energyProduction[p][t] == 0);
  	 }
  	 
  	 forall(t in TIMERANGE) {
   		startSignals[p][t] == 1 => powerPlantState[p][t] == IDLE;
   		offSignals[p][t] == 1 => powerPlantState[p][t] == ON;
  	 }
  	   
  	 // transition rules
	 forall ( t in 0..card(TIMERANGE)-2 ) {
	  	powerPlantState[p][t] == IDLE => (powerPlantState[p][t+1] == IDLE || (powerPlantState[p][t+1] == STARTING && startSignals[p][t] == 1 && (countdown[p][t+1] == startUpSteps[p][t])));
	    (powerPlantState[p][t] == STARTING && countdown[p][t] >= 1) => (powerPlantState[p][t+1] == STARTING && countdown[p][t+1] == countdown[p][t] - 1);
	    (powerPlantState[p][t] == STARTING && countdown[p][t] == 0) => powerPlantState[p][t+1] == ON;
	    powerPlantState[p][t] == ON => (powerPlantState[p][t+1] == ON || (powerPlantState[p][t+1] == IDLE && offSignals[p][t] == 1)); 
	 }
	      
	 forall ( t in 0..card(TIMERANGE)-2 ) {
	  // The percentual change in output must always be lower than the rate of change; but
	   rateOfChangeConstraint:
	    powerPlantRunning[p][t] == 1 && powerPlantRunning[p][t+1] == 1  =>
	    abs(energyProduction[p][t] - energyProduction[p][t+1]) <= energyProduction[p][t] * PowerPlant[p].rateOfChange;  
	  fixedRampConstraint:
	    powerPlantRunning[p][t] == 1 && powerPlantRunning[p][t+1] == 1 =>
	    abs(energyProduction[p][t] - energyProduction[p][t+1]) <= PowerPlant[p].fixedRamp;  
	  
	}
  }
} 
