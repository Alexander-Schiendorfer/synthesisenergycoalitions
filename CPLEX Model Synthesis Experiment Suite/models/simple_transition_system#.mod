/*********************************************
 * OPL 12.4 Model
 * Author: alexander
 * Creation Date: Jan 10, 2014 at 5:11:36 PM
 *********************************************/
// given a boolean vector, this system tries to follow 
// the pattern as closely as possible

//using CPLEX;
using CP;
 
// this range serves as the objective 
range TIMERANGE = 0..10;
range SUBRANGE = 0..9;

int pattern[TIMERANGE] = [0,0,0,0,1,1,0,0,0,0,1];

dvar int state[TIMERANGE] in 0..4;
dvar int output[TIMERANGE] in 0..1;
dvar int signal[TIMERANGE] in 0..1;
dvar int offsignal[TIMERANGE] in 0..1;

minimize sum(t in TIMERANGE) abs(pattern[t] - output[t]);

subject to {
  state[0] == 0;
  
  forall(t in TIMERANGE) {
   	state[t] <= 3 => output[t] == 0;
   	state[t] == 4 => output[t] == 1;
   	signal[t] == 1 => state[t] == 0;
   	offsignal[t] == 1 => state[t] == 4;
  }  
  
  // transition rules
  forall(t in SUBRANGE) {   
    state[t] == 0 => (state[t+1] == 0 || (state[t+1] == 1 && signal[t] == 1));
    state[t] == 1 => state[t+1] == 2;
    state[t] == 2 => state[t+1] == 3;
    state[t] == 3 => state[t+1] == 4; 
    state[t] == 4 => (state[t+1] == 4 || (state[t+1] == 0 && offsignal[t] == 1)); 
  }  
}  