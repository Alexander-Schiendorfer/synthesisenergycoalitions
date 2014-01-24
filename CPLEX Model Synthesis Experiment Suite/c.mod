/*********************************************
 * OPL 12.4 Model
 * Simplistic concrete powerplant temporally relevant
 * information
 * Author: Alexander Schiendorfer
 *********************************************/
 
using CPLEX;

int LAST_SIMULATION_STEP = 120;
range TIMERANGE = 0..LAST_SIMULATION_STEP;

float P_min =  %P_MIN%;
float P_max = %P_MAX%;

int minDownTime = %MIN_DOWN_TIME%;
int minUpTime = %MIN_UP_TIME%;
float rateOfChange = %RATE_OF_CHANGE%;
float optRate_ofChange = %RATEOF_CHANGE_OPT%;
int IDLE = 0;
int STARTING = 1;
int STOPPING = 2;
int UP = 3;
stepFunction startupDuration = stepwise{ %HOT_TIME%->3; %COLD_TIME% };

dvar float+ production[TIMERANGE];  // Production of the plant in kW.
dvar int+ consStopping[TIMERANGE];
dvar int+ consRunning[TIMERANGE]; 
dvar int+ countdown[TIMERANGE]; // the count down variable denoting the steps needed to wait
dvar int+ powerPlantState[TIMERANGE] in 0..3;
dexpr int running[t in TIMERANGE] = (powerPlantState[t] == UP);
dvar int signal[TIMERANGE] in -1 .. 1;

subject to {
	forall (t in TIMERANGE) {
		c1_economically_optimal: production[t] >= %IDEAL_MIN% && production[t] <= %IDEAL_MAX%;
	    c2_economically_good: production[t] >= %GOOD_MIN% && production[t] <= %GOOD_MAX%;
	    signal_on_idle: signal[t] == 1 => powerPlantState[t] == IDLE;
   		signal_off_idle: signal[t] == -1 => powerPlantState[t] == UP;
   		   state_running: (running[t] == 1) => (production[t] >= P_min);
        state_not_running: (running[t] == 0) => (production[t] == 0);
	}   	
	forall (t in 0..LAST_SIMULATION_STEP-1) {	
		rate_of_change: (running[t] == 1 && (running[t+1] == 1) ) => abs(production[t] - production[t+1]) <= production[t]  * rateOfChange;
		c3_rate_ofchange_opt: (running[t] == 1 && (running[t+1] == 1)) => abs(production[t] - production[t+1]) <= production[t]  * optRate_ofChange;
		switch_off_min: (running[t] == true && running[t+1] == false) => production[t] == P_min;
		switcH_on: (running[t] == false && running[t+1] == true) => (consStopping[t] - minDownTime) >= 0;
		switcH_off: (running[t] == true  && running[t+1] == false) => (consRunning[t] - minUpTime) >= 0;
		switch_on_min: (running[t] == false && running[t+1] == true) => production[t+1] == P_min;
		consrun_const:(running[t+1] == 1 && consRunning[t+1] == (1 + consRunning[t])) || (running[t+1] == 0 && consRunning[t+1] == 0);
        consstop_const:(running[t+1] == 0 && consStopping[t+1] == (1 + consStopping[t])) || (running[t+1] == 1 && consStopping[t+1] == 0);
      	powerPlantState[t] == IDLE => (powerPlantState[t+1] == IDLE || (powerPlantState[t+1] == STARTING && signal[t] == 1 && (countdown[t+1] == startupDuration(consStopping[t]))));
	    (powerPlantState[t] == STARTING && countdown[t] >= 1) => (powerPlantState[t+1] == STARTING && countdown[t+1] == countdown[t] - 1);
	    (powerPlantState[t] == STARTING && countdown[t] == 0) => powerPlantState[t+1] == UP;
	    powerPlantState[t] == UP => (powerPlantState[t+1] == UP || (powerPlantState[t+1] == IDLE && signal[t] == -1)); 
	}
};

/* SOFT-CONSTRAINTS
 c1_economically_optimal >> c2_economically_good
 c1_economically_optimal >> c3_rate_ofchange_opt
 * End SOFT-CONSTRAINTS  */
