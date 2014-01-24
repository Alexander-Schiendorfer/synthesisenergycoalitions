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

int initialCountdown = 2;

// emulating an enumeration for the states in the transition system
int IDLE = 0;
int STARTING = 1;
int STOPPING = 2;
int ON = 3;

dvar int state[TIMERANGE] in 0..4; // states 0 - off, 1 - starting, 2 - stopping, 3 - on
dvar int countdown[TIMERANGE];
dvar int output[TIMERANGE] in 0..1;
dvar int signal[TIMERANGE] in 0..1;
dvar int offsignal[TIMERANGE] in 0..1;

minimize sum(t in TIMERANGE) abs(pattern[t] - output[t]);

subject to {
  state[0] == IDLE;
  
  forall(t in TIMERANGE) {
   	state[t] <= 2 => output[t] == 0;
   	state[t] == ON => output[t] == 1;
   	signal[t] == 1 => state[t] == STARTING;
   	offsignal[t] == 1 => state[t] == ON;
  }  
  
  // transition rules
  forall(t in SUBRANGE) {   
    state[t] == IDLE => (state[t+1] == IDLE || (state[t+1] == STARTING && signal[t] == 1 && (countdown[t+1] == initialCountdown)));
    (state[t] == 1 && countdown[t] > 0) => (state[t+1] == STARTING && countdown[t+1] == countdown[t] - 1);
    (state[t] == 1 && countdown[t] == 0) => state[t+1] == ON;
    state[t] == ON => (state[t+1] == ON || (state[t+1] == IDLE && offsignal[t] == 1)); 
  }  
}  