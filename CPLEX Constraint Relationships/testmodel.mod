/*********************************************
* OPL 12.2 Model
* Author: steghoja
* Creation Date: 03.05.2012 at 14:59:51
* AgentSet
plants
*********************************************/
{string} plants = {"b", "c", "a"};

float P_max[plants] = [35.0, 400.0, 100.0];
int LAST_SIMULATION_STEP = 5;
range TIMERANGE = 0..LAST_SIMULATION_STEP;
float loadCurve[TIMERANGE] = [200.0, 250.0, 230.0, 247.0, 349.0, 551.0];

dvar float+ production[plants][TIMERANGE];
dexpr float totalProduction[t in TIMERANGE] = sum ( p in plants ) production[p][t];
dexpr float loadFactor[p in plants][t in TIMERANGE] = production[p][t] / P_max[p];
dexpr float minLoadFact[t in TIMERANGE] = min(p in plants) loadFactor[p][t];
dexpr float maxLoadFact[t in TIMERANGE] = max(p in plants) loadFactor[p][t];
dexpr float violation = sum(t in TIMERANGE) abs(totalProduction[t]-loadCurve[t]);

float P_min_b = 15.0;
int minUpTime_b = 2;
float fixedChange_b = 5;
dvar int+ consStopping_b[TIMERANGE];
dvar int+ consRunning_b[TIMERANGE];
dexpr int running_b[t in TIMERANGE] = !(production["b"][t] == 0);
float P_min_c = 200.0;
int minOffTime_c = 2;
float rateOfChange_c = 0.2;
float pricePerKWh_c = 6.0;
int IDLE_c = 0;
int STARTING_c = 1;
int STOPPING_c = 2;
int UP_c = 3;
stepFunction startupDuration_c = stepwise{ 2->3; 4 };
dvar int+ consStopping_c[TIMERANGE];
dvar int+ consRunning_c[TIMERANGE];
dvar int+ countdown_c[TIMERANGE];
dvar int+ powerPlantState_c[TIMERANGE] in 0..3;
dexpr int running_c[t in TIMERANGE] = (powerPlantState_c[t] == UP_c);
dvar int signal_c[TIMERANGE] in -1 .. 1;
float P_min_a = 50.0;
float rateOfChange_a = 0.15;
dexpr int running_a[t in TIMERANGE] = !(production["a"][t] == 0);
minimize violation;
subject to {
consRunning_b[0] == 4;
consStopping_b[0] == 0;
production["b"][0] == 17;
consRunning_c[0] == 0;
countdown_c[0] == 0;
consStopping_c[0] == 5;
signal_c[0] == 0;
powerPlantState_c[0] == 0;
production["c"][0] == 0;
production["a"][0] == 55;
forall ( t in TIMERANGE ) {
oc1: minLoadFact[t] >= 0.4;
oc2: maxLoadFact[t] <= 0.6;
forall(p in plants) {
production[p][t] <= P_max[p];
}
}
forall (t in TIMERANGE) {
c1_economically_optimal_b: production["b"][t] >= 22 && production["b"][t] <= 25;
c2_economically_good_b: production["b"][t] >= 20 && production["b"][t] <= 30;
c3_economically_acc_b: production["b"][t] >= 18 && production["b"][t] <= 33;
}

forall (t in 0..LAST_SIMULATION_STEP-1) {
fixed_change_b: (running_b[t] == 1 && running_b[t+1] == 1) => abs(production["b"][t] - production["b"][t+1]) <= fixedChange_b;
minimal_up_time_b: (running_b[t] == true && running_b[t+1] == false) => (consRunning_b[t] - minUpTime_b) >= 0;
switch_off_min_b: (running_b[t] == true && running_b[t+1] == false) => production["b"][t] == P_min_b;
switch_on_min_b:  (running_b[t] == false && running_b[t+1] == true) => production["b"][t+1] == P_min_b;
consrun_const_b:  (running_b[t+1] == 1 && consRunning_b[t+1] == (1 + consRunning_b[t])) || (running_b[t+1] == 0 && consRunning_b[t+1] == 0);
consstop_const_b: (running_b[t+1] == 0 && consStopping_b[t+1] == (1 + consStopping_b[t])) || (running_b[t+1] == 1 && consStopping_b[t+1] == 0);
}
forall (t in TIMERANGE) {
c1_economically_optimal_c: production["c"][t] >= 300.0 && production["c"][t] <= 350.0;
c2_economically_good_c: production["c"][t] >= 280.0 && production["c"][t] <= 370.0;
signal_c_on_idle: signal_c[t] == 1 => powerPlantState_c[t] == IDLE_c;
signal_c_off_idle: signal_c[t] == -1 => powerPlantState_c[t] == UP_c;
state_running_c_c: (running_c[t] == 1) => (production["c"][t] >= P_min_c);
state_not_running_c_c: (running_c[t] == 0) => (production["c"][t] == 0);
}
forall (t in 0..LAST_SIMULATION_STEP-1) {
rate_of_change_c: (running_c[t] == 1) => abs(production["c"][t] - production["c"][t+1]) <= production["c"][t]  * rateOfChange_c;
c3_rate_ofchange_opt_c: (running_c[t] == 1) => abs(production["c"][t] - production["c"][t+1]) <= production["c"][t]  * 0.1;
switch_off_min_c: (running_c[t] == true && running_c[t+1] == false) => production["c"][t] == P_min_c;
switch_on_c: (running_c[t] == false && running_c[t+1] == true) => (consStopping_c[t] - minOffTime_c) >= 0;
switch_on_c_min: (running_c[t] == false && running_c[t+1] == true) => production["c"][t+1] == P_min_c;
consrun_const_c:(running_c[t+1] == 1 && consRunning_c[t+1] == (1 + consRunning_c[t])) || (running_c[t+1] == 0 && consRunning_c[t+1] == 0);
consstop_const_c:(running_c[t+1] == 0 && consStopping_c[t+1] == (1 + consStopping_c[t])) || (running_c[t+1] == 1 && consStopping_c[t+1] == 0);
powerPlantState_c[t] == IDLE_c => (powerPlantState_c[t+1] == IDLE_c || (powerPlantState_c[t+1] == STARTING_c && signal_c[t] == 1 && (countdown_c[t+1] == startupDuration_c(consRunning_c[t]))));
(powerPlantState_c[t] == STARTING_c && countdown_c[t] >= 1) => (powerPlantState_c[t+1] == STARTING_c && countdown_c[t+1] == countdown_c[t] - 1);
(powerPlantState_c[t] == STARTING_c && countdown_c[t] == 0) => powerPlantState_c[t+1] == UP_c;
powerPlantState_c[t] == UP_c => (powerPlantState_c[t+1] == UP_c || (powerPlantState_c[t+1] == IDLE_c && signal_c[t] == -1));
}

forall (t in 0..LAST_SIMULATION_STEP-1) {
rate_of_change_a:  (running_a[t] == 1) && (running_a[t+1] == 1) => abs(production["a"][t] - production["a"][t+1]) <= production["a"][t]  * rateOfChange_a;
c1_rate_ofchangeOpt_a: (running_a[t] == 1) && (running_a[t+1] == 1) => abs(production["a"][t] - production["a"][t+1]) <= production["a"][t]  * 0.07;
c2_rate_ofchangePref_a: (running_a[t] == 1) && (running_a[t+1] == 1) => abs(production["a"][t] - production["a"][t+1]) <= production["a"][t]  * 0.10;
}
};
/* SOFT-CONSTRAINTS
oc1
oc2
oc1 >> c1_economically_optimal_b;
oc1 >> c1_economically_optimal_c;
oc1 >> c1_rate_ofchangeOpt_a;
oc2 >> c1_economically_optimal_b;
oc2 >> c1_economically_optimal_c;
oc2 >> c1_rate_ofchangeOpt_a;
c1_economically_optimal_b >> c2_economically_good_b
c2_economically_good_b >> c3_economically_acc_b
c1_economically_optimal_c >> c2_economically_good_c
c1_economically_optimal_c >> c3_rate_ofchange_opt_c
c1_rate_ofchangeOpt_a >> c2_rate_ofchangePref_a
End SOFT-CONSTRAINTS */
