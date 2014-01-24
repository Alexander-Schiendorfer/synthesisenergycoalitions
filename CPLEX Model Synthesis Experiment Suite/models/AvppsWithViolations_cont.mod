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
int lastSimStep = 46;
int lastConsideredStep = 300;
 
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

// Determines whether or not a power plant is running in each time step.
dvar boolean powerPlantRunning[ControllablePlants][CONSIDERED_RANGE];

// ======================================================
// BEGIN of start up function area

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

// start up time depending on stop time 
dexpr float startUpSteps[p in ControllablePlants][t in TIMERANGE] = startUpFunction[p](consStop[p][t]);

dvar int countDown[p in ControllablePlants][t in CONSIDERED_RANGE] in 0..inf;
dvar boolean startSignals[p in ControllablePlants][t in CONSIDERED_RANGE] in 0 .. 1;
dexpr int isStarting[p in ControllablePlants][t in CONSIDERED_RANGE] = countDown[p][t] <= (inf-1); 
 
// END of start up function area
// ======================================================

float energyConsumption[TIMERANGE] = ...;

// The scheduled energy production per controllable plant in each time step.
dvar float+ energyProduction[ControllablePlants][TIMERANGE];

dexpr float totalProduction[t in TIMERANGE] = (sum( p in ControllablePlants ) (energyProduction[p][t]));

// The amount of energy missing or superfluous to meet the demand in each time step.
dexpr float violation[t in TIMERANGE] = abs(energyConsumption[t] - totalProduction[t]);

minimize
  sum ( t in TIMERANGE ) violation[t]; //+ sum( p in ControllablePlants, t in 0..94 ) prodDifference[p][t];


subject to {
  forall ( p in ControllablePlants ) {
	// initial definition for isStarting
	isStarting[p][0] == (startSignals[p][0] == 1 || PowerPlant[p].countDownInit <= inf - 1);
	// TODO quite redundant these two constraints - maybe reformulate
	//PowerPlant[p].countDownInit <= inf - 1 => countDown[p][0] == PowerPlant[p].countDownInit;
	countDown[p][0] == PowerPlant[p].countDownInit;
	powerPlantRunning[p][0] == (PowerPlant[p].consRunningTimeInit >= 1);

     // minimal running/standing time constraint
    forall (t in 1..lastSimStep) {
	     minStopTimeConstraint:
	       (powerPlantRunning[p][t] == 1 && powerPlantRunning[p][t-1] == 0) => consStop[p][t-1] - PowerPlant[p].minDownTime >= 0;
    	 minRunTimeConstraint: 
    	  (powerPlantRunning[p][t] == 0 && powerPlantRunning[p][t-1] == 1) => consRun[p][t-1] - PowerPlant[p].minUpTime >= 0;	
	}   
	
	// constraints for additional decision variables
	consRun[p][0] == PowerPlant[p].consRunningTimeInit;
    consStop[p][0] == PowerPlant[p].consStandingTimeInit;
    
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
	
	// count down constraints
	forall(t in TIMERANGE) { 
         resetIsStartingConstraint: // when plant is running, reset state  
	        (powerPlantRunning[p][t+1] == 1) => ((countDown[p][t+1] == 0) && (isStarting[p][t+2] == 0)) || (powerPlantRunning[p][t] == 1);
	     justifyIsStartingState: // there has to be a reason for isStarting to be set true
	        (isStarting[p][t+1] == 1) => ((isStarting[p][t] == 1) || (startSignals[p][t+1] == 1));
	     initCountDownAfterStartSignal : // after a start signal is placed, countDown has to be adapted 
	        (startSignals[p][t] == 1) => (countDown[p][t+1] == startUpSteps[p][t] && isStarting[p][t] == 1 && powerPlantRunning[p][t] == 0);
	     defaultCountdown: // if no start-progress set cntDown
	     	(isStarting[p][t] == 0) => countDown[p][t] == inf;
	     decreaseConstraint: 
	        (isStarting[p][t] == 1 && countDown[p][t] >= 1) => (countDown[p][t+1] == countDown[p][t] - 1);

      }	
	forall ( t in 0..card(TIMERANGE)-2 ) {
	  // The percentual change in output must always be lower than the rate of change; but
	  rateOfChangeConstraint:
	    powerPlantRunning[p][t] == 1 =>
	    abs(energyProduction[p][t] - energyProduction[p][t+1]) <= energyProduction[p][t] * PowerPlant[p].rateOfChange;  
	  fixedRampConstraint:
	    powerPlantRunning[p][t] == 1 =>
	    abs(energyProduction[p][t] - energyProduction[p][t+1]) <= PowerPlant[p].fixedRamp;  
	  
	}
  }  
	
} 
