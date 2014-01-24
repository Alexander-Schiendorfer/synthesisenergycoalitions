/*********************************************
* OPL 12.2 Model
* Author: steghoja
* Creation Date: 03.05.2012 at 14:59:51
* AgentSet
plants
*********************************************/
{string} plants = {"generated_plant_gas_35", "generated_plant_gas_84", "generated_plant_gas_34", "generated_plant_gas_11", "generated_plant_gas_87"};

float P_max[plants] = [1328.0, 80.0, 1276.0, 200.0, 130.0];
int LAST_SIMULATION_STEP = ...;
range TIMERANGE = 0..LAST_SIMULATION_STEP;
float loadCurve[TIMERANGE] = ...;

dvar float+ production[plants][TIMERANGE];
dexpr float totalProduction[t in TIMERANGE] = sum ( p in plants ) production[p][t];
dexpr float loadFactor[p in plants][t in TIMERANGE] = production[p][t] / P_max[p];
dexpr float minLoadFact[t in TIMERANGE] = min(p in plants) loadFactor[p][t];
dexpr float maxLoadFact[t in TIMERANGE] = max(p in plants) loadFactor[p][t];
dexpr float violation = sum(t in TIMERANGE) abs(totalProduction[t]-loadCurve[t]);

float P_min_generated_plant_gas_35 = 334.9143763595357;
float rateOfChange_generated_plant_gas_35 = 0.18140673112861547;
float rateOf_ChangePref_generated_plant_gas_35 = 0.12373598868567445;
float rate_Of_ChangeOpt_generated_plant_gas_35 = 0.9075630616314798;
dexpr int running_generated_plant_gas_35[t in TIMERANGE] = !(production["generated_plant_gas_35"][t] == 0);
float P_min_generated_plant_gas_84 = 17.922277034555442;
float rateOfChange_generated_plant_gas_84 = 0.1729708885417878;
float rateOf_ChangePref_generated_plant_gas_84 = 0.1502382076906063;
float rate_Of_ChangeOpt_generated_plant_gas_84 = 0.3136639398818172;
dexpr int running_generated_plant_gas_84[t in TIMERANGE] = !(production["generated_plant_gas_84"][t] == 0);
float P_min_generated_plant_gas_34 = 324.7220864221818;
float rateOfChange_generated_plant_gas_34 = 0.19698350328438136;
float rateOf_ChangePref_generated_plant_gas_34 = 0.10867662270226629;
float rate_Of_ChangeOpt_generated_plant_gas_34 = 0.5359503540787501;
dexpr int running_generated_plant_gas_34[t in TIMERANGE] = !(production["generated_plant_gas_34"][t] == 0);
float P_min_generated_plant_gas_11 = 48.81962578627903;
float rateOfChange_generated_plant_gas_11 = 0.1939650824099046;
float rateOf_ChangePref_generated_plant_gas_11 = 0.12225704109104284;
float rate_Of_ChangeOpt_generated_plant_gas_11 = 0.182628128663391;
dexpr int running_generated_plant_gas_11[t in TIMERANGE] = !(production["generated_plant_gas_11"][t] == 0);
float P_min_generated_plant_gas_87 = 26.094691691347087;
float rateOfChange_generated_plant_gas_87 = 0.18698846682066514;
float rateOf_ChangePref_generated_plant_gas_87 = 0.11721734788465415;
float rate_Of_ChangeOpt_generated_plant_gas_87 = 0.669355402222372;
dexpr int running_generated_plant_gas_87[t in TIMERANGE] = !(production["generated_plant_gas_87"][t] == 0);
{string} softConstraints = {"c1_rate_ofchangeOpt_generated_plant_gas_11", "c1_rate_ofchangeOpt_generated_plant_gas_34", "c1_rate_ofchangeOpt_generated_plant_gas_35", "c1_rate_ofchangeOpt_generated_plant_gas_84", "c1_rate_ofchangeOpt_generated_plant_gas_87", "c2_rate_ofchangePref_generated_plant_gas_11", "c2_rate_ofchangePref_generated_plant_gas_34", "c2_rate_ofchangePref_generated_plant_gas_35", "c2_rate_ofchangePref_generated_plant_gas_84", "c2_rate_ofchangePref_generated_plant_gas_87", "oc1", "oc2"};
dvar int+ penalties[softConstraints][TIMERANGE];
dexpr float penaltySum = sum(t in TIMERANGE, c in softConstraints) penalties[c][t];
dexpr float penaltyPerStep[t in TIMERANGE] = sum(c in softConstraints) penalties[c][t];
dexpr int penaltyCount[c in softConstraints][t in TIMERANGE] = (penalties[c][t] >= 1);
dexpr int constraintViolated[c in softConstraints] = sum(t in TIMERANGE) penaltyCount[c][t];
dexpr float sumConstraintViolations = sum(t in TIMERANGE, c in softConstraints) penaltyCount[c][t];
int childWeights[softConstraints] = ...;
dexpr int predsPerViolatedConstraint[c in softConstraints] = childWeights[c]*constraintViolated[c];
dexpr float predsPerViolatedAvg = (sum(c in softConstraints) predsPerViolatedConstraint[c]) / (card(softConstraints) * card(TIMERANGE)) ;
minimize violation;
subject to {
penaltySum >= 0;
sumConstraintViolations >= 0;
forall(c in softConstraints) { 	constraintViolated[c] >= 0; }
predsPerViolatedAvg >= 0;
forall (c in softConstraints) {  predsPerViolatedConstraint[c] >= 0; }
forall ( t in TIMERANGE ) {
oc1:( minLoadFact[t] >= 0.4 && penalties["oc1"][t] == 0) || (!( minLoadFact[t] >= 0.4) && penalties["oc1"][t] == 16);
oc2:( maxLoadFact[t] <= 0.6 && penalties["oc2"][t] == 0) || (!( maxLoadFact[t] <= 0.6) && penalties["oc2"][t] == 16);
forall(p in plants) {
production[p][t] <= P_max[p];
}
}
forall(t in TIMERANGE) {
running_generated_plant_gas_35[t] == 1 => production["generated_plant_gas_35"][t] >= P_min_generated_plant_gas_35;
}
forall (t in 0..LAST_SIMULATION_STEP-1) {
rate_of_change_generated_plant_gas_35:  (running_generated_plant_gas_35[t] == 1) && (running_generated_plant_gas_35[t+1] == 1) => abs(production["generated_plant_gas_35"][t] - production["generated_plant_gas_35"][t+1]) <= production["generated_plant_gas_35"][t]  * rateOfChange_generated_plant_gas_35;
c1_rate_ofchangeOpt_generated_plant_gas_35:( (running_generated_plant_gas_35[t] == 1) && (running_generated_plant_gas_35[t+1] == 1) => abs(production["generated_plant_gas_35"][t] - production["generated_plant_gas_35"][t+1]) <= production["generated_plant_gas_35"][t]  * rate_Of_ChangeOpt_generated_plant_gas_35 && penalties["c1_rate_ofchangeOpt_generated_plant_gas_35"][t] == 0) || (!( (running_generated_plant_gas_35[t] == 1) && (running_generated_plant_gas_35[t+1] == 1) => abs(production["generated_plant_gas_35"][t] - production["generated_plant_gas_35"][t+1]) <= production["generated_plant_gas_35"][t]  * rate_Of_ChangeOpt_generated_plant_gas_35) && penalties["c1_rate_ofchangeOpt_generated_plant_gas_35"][t] == 2);
c2_rate_ofchangePref_generated_plant_gas_35:( (running_generated_plant_gas_35[t] == 1) && (running_generated_plant_gas_35[t+1] == 1) => abs(production["generated_plant_gas_35"][t] - production["generated_plant_gas_35"][t+1]) <= production["generated_plant_gas_35"][t]  * rateOf_ChangePref_generated_plant_gas_35 && penalties["c2_rate_ofchangePref_generated_plant_gas_35"][t] == 0) || (!( (running_generated_plant_gas_35[t] == 1) && (running_generated_plant_gas_35[t+1] == 1) => abs(production["generated_plant_gas_35"][t] - production["generated_plant_gas_35"][t+1]) <= production["generated_plant_gas_35"][t]  * rateOf_ChangePref_generated_plant_gas_35) && penalties["c2_rate_ofchangePref_generated_plant_gas_35"][t] == 1);
}
forall(t in TIMERANGE) {
running_generated_plant_gas_84[t] == 1 => production["generated_plant_gas_84"][t] >= P_min_generated_plant_gas_84;
}
forall (t in 0..LAST_SIMULATION_STEP-1) {
rate_of_change_generated_plant_gas_84:  (running_generated_plant_gas_84[t] == 1) && (running_generated_plant_gas_84[t+1] == 1) => abs(production["generated_plant_gas_84"][t] - production["generated_plant_gas_84"][t+1]) <= production["generated_plant_gas_84"][t]  * rateOfChange_generated_plant_gas_84;
c1_rate_ofchangeOpt_generated_plant_gas_84:( (running_generated_plant_gas_84[t] == 1) && (running_generated_plant_gas_84[t+1] == 1) => abs(production["generated_plant_gas_84"][t] - production["generated_plant_gas_84"][t+1]) <= production["generated_plant_gas_84"][t]  * rate_Of_ChangeOpt_generated_plant_gas_84 && penalties["c1_rate_ofchangeOpt_generated_plant_gas_84"][t] == 0) || (!( (running_generated_plant_gas_84[t] == 1) && (running_generated_plant_gas_84[t+1] == 1) => abs(production["generated_plant_gas_84"][t] - production["generated_plant_gas_84"][t+1]) <= production["generated_plant_gas_84"][t]  * rate_Of_ChangeOpt_generated_plant_gas_84) && penalties["c1_rate_ofchangeOpt_generated_plant_gas_84"][t] == 2);
c2_rate_ofchangePref_generated_plant_gas_84:( (running_generated_plant_gas_84[t] == 1) && (running_generated_plant_gas_84[t+1] == 1) => abs(production["generated_plant_gas_84"][t] - production["generated_plant_gas_84"][t+1]) <= production["generated_plant_gas_84"][t]  * rateOf_ChangePref_generated_plant_gas_84 && penalties["c2_rate_ofchangePref_generated_plant_gas_84"][t] == 0) || (!( (running_generated_plant_gas_84[t] == 1) && (running_generated_plant_gas_84[t+1] == 1) => abs(production["generated_plant_gas_84"][t] - production["generated_plant_gas_84"][t+1]) <= production["generated_plant_gas_84"][t]  * rateOf_ChangePref_generated_plant_gas_84) && penalties["c2_rate_ofchangePref_generated_plant_gas_84"][t] == 1);
}
forall(t in TIMERANGE) {
running_generated_plant_gas_34[t] == 1 => production["generated_plant_gas_34"][t] >= P_min_generated_plant_gas_34;
}
forall (t in 0..LAST_SIMULATION_STEP-1) {
rate_of_change_generated_plant_gas_34:  (running_generated_plant_gas_34[t] == 1) && (running_generated_plant_gas_34[t+1] == 1) => abs(production["generated_plant_gas_34"][t] - production["generated_plant_gas_34"][t+1]) <= production["generated_plant_gas_34"][t]  * rateOfChange_generated_plant_gas_34;
c1_rate_ofchangeOpt_generated_plant_gas_34:( (running_generated_plant_gas_34[t] == 1) && (running_generated_plant_gas_34[t+1] == 1) => abs(production["generated_plant_gas_34"][t] - production["generated_plant_gas_34"][t+1]) <= production["generated_plant_gas_34"][t]  * rate_Of_ChangeOpt_generated_plant_gas_34 && penalties["c1_rate_ofchangeOpt_generated_plant_gas_34"][t] == 0) || (!( (running_generated_plant_gas_34[t] == 1) && (running_generated_plant_gas_34[t+1] == 1) => abs(production["generated_plant_gas_34"][t] - production["generated_plant_gas_34"][t+1]) <= production["generated_plant_gas_34"][t]  * rate_Of_ChangeOpt_generated_plant_gas_34) && penalties["c1_rate_ofchangeOpt_generated_plant_gas_34"][t] == 2);
c2_rate_ofchangePref_generated_plant_gas_34:( (running_generated_plant_gas_34[t] == 1) && (running_generated_plant_gas_34[t+1] == 1) => abs(production["generated_plant_gas_34"][t] - production["generated_plant_gas_34"][t+1]) <= production["generated_plant_gas_34"][t]  * rateOf_ChangePref_generated_plant_gas_34 && penalties["c2_rate_ofchangePref_generated_plant_gas_34"][t] == 0) || (!( (running_generated_plant_gas_34[t] == 1) && (running_generated_plant_gas_34[t+1] == 1) => abs(production["generated_plant_gas_34"][t] - production["generated_plant_gas_34"][t+1]) <= production["generated_plant_gas_34"][t]  * rateOf_ChangePref_generated_plant_gas_34) && penalties["c2_rate_ofchangePref_generated_plant_gas_34"][t] == 1);
}
forall(t in TIMERANGE) {
running_generated_plant_gas_11[t] == 1 => production["generated_plant_gas_11"][t] >= P_min_generated_plant_gas_11;
}
forall (t in 0..LAST_SIMULATION_STEP-1) {
rate_of_change_generated_plant_gas_11:  (running_generated_plant_gas_11[t] == 1) && (running_generated_plant_gas_11[t+1] == 1) => abs(production["generated_plant_gas_11"][t] - production["generated_plant_gas_11"][t+1]) <= production["generated_plant_gas_11"][t]  * rateOfChange_generated_plant_gas_11;
c1_rate_ofchangeOpt_generated_plant_gas_11:( (running_generated_plant_gas_11[t] == 1) && (running_generated_plant_gas_11[t+1] == 1) => abs(production["generated_plant_gas_11"][t] - production["generated_plant_gas_11"][t+1]) <= production["generated_plant_gas_11"][t]  * rate_Of_ChangeOpt_generated_plant_gas_11 && penalties["c1_rate_ofchangeOpt_generated_plant_gas_11"][t] == 0) || (!( (running_generated_plant_gas_11[t] == 1) && (running_generated_plant_gas_11[t+1] == 1) => abs(production["generated_plant_gas_11"][t] - production["generated_plant_gas_11"][t+1]) <= production["generated_plant_gas_11"][t]  * rate_Of_ChangeOpt_generated_plant_gas_11) && penalties["c1_rate_ofchangeOpt_generated_plant_gas_11"][t] == 2);
c2_rate_ofchangePref_generated_plant_gas_11:( (running_generated_plant_gas_11[t] == 1) && (running_generated_plant_gas_11[t+1] == 1) => abs(production["generated_plant_gas_11"][t] - production["generated_plant_gas_11"][t+1]) <= production["generated_plant_gas_11"][t]  * rateOf_ChangePref_generated_plant_gas_11 && penalties["c2_rate_ofchangePref_generated_plant_gas_11"][t] == 0) || (!( (running_generated_plant_gas_11[t] == 1) && (running_generated_plant_gas_11[t+1] == 1) => abs(production["generated_plant_gas_11"][t] - production["generated_plant_gas_11"][t+1]) <= production["generated_plant_gas_11"][t]  * rateOf_ChangePref_generated_plant_gas_11) && penalties["c2_rate_ofchangePref_generated_plant_gas_11"][t] == 1);
}
forall(t in TIMERANGE) {
running_generated_plant_gas_87[t] == 1 => production["generated_plant_gas_87"][t] >= P_min_generated_plant_gas_87;
}
forall (t in 0..LAST_SIMULATION_STEP-1) {
rate_of_change_generated_plant_gas_87:  (running_generated_plant_gas_87[t] == 1) && (running_generated_plant_gas_87[t+1] == 1) => abs(production["generated_plant_gas_87"][t] - production["generated_plant_gas_87"][t+1]) <= production["generated_plant_gas_87"][t]  * rateOfChange_generated_plant_gas_87;
c1_rate_ofchangeOpt_generated_plant_gas_87:( (running_generated_plant_gas_87[t] == 1) && (running_generated_plant_gas_87[t+1] == 1) => abs(production["generated_plant_gas_87"][t] - production["generated_plant_gas_87"][t+1]) <= production["generated_plant_gas_87"][t]  * rate_Of_ChangeOpt_generated_plant_gas_87 && penalties["c1_rate_ofchangeOpt_generated_plant_gas_87"][t] == 0) || (!( (running_generated_plant_gas_87[t] == 1) && (running_generated_plant_gas_87[t+1] == 1) => abs(production["generated_plant_gas_87"][t] - production["generated_plant_gas_87"][t+1]) <= production["generated_plant_gas_87"][t]  * rate_Of_ChangeOpt_generated_plant_gas_87) && penalties["c1_rate_ofchangeOpt_generated_plant_gas_87"][t] == 2);
c2_rate_ofchangePref_generated_plant_gas_87:( (running_generated_plant_gas_87[t] == 1) && (running_generated_plant_gas_87[t+1] == 1) => abs(production["generated_plant_gas_87"][t] - production["generated_plant_gas_87"][t+1]) <= production["generated_plant_gas_87"][t]  * rateOf_ChangePref_generated_plant_gas_87 && penalties["c2_rate_ofchangePref_generated_plant_gas_87"][t] == 0) || (!( (running_generated_plant_gas_87[t] == 1) && (running_generated_plant_gas_87[t+1] == 1) => abs(production["generated_plant_gas_87"][t] - production["generated_plant_gas_87"][t+1]) <= production["generated_plant_gas_87"][t]  * rateOf_ChangePref_generated_plant_gas_87) && penalties["c2_rate_ofchangePref_generated_plant_gas_87"][t] == 1);
}
};
