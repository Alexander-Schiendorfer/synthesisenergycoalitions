/*********************************************
 * OPL 12.2 Model
 * Author: steghoja
 * Creation Date: 17.08.2011 at 15:07:59
 * Example for a manually extended constraint relationship problem (see http://kti.mff.cuni.cz/~bartak/constraints/hierarchies.html).
 * Modelled with individual arrays for the different kinds of clothes and individual constraints.
 * As all constraints are hard and the problem is over-constrained, no solution is found.
 * Semantics are different than in the example as the precondition of the weak constraint may not
 * be satisfied (when the shirt chosen is red). Also, a tupel (r,g) is interpreted as an equivalence
 * r<->g or r->g && g->r.
 *********************************************/

 using CP;
 
 {string} Shirts = { "Red", "White" };
 {string} Footwear = { "Leather", "Sneakers" };
 {string} Trousers = { "Blue", "Denim", "Grey" };
 {string} Jackets = {"Black", "Corduroy" };
  
 dvar int shirts[Shirts] in 0..1;
 dvar int shoes[Footwear] in 0..1;
 dvar int pants[Trousers] in 0..1; 
 dvar int jackets[Jackets] in 0..1;
 
 /*PENALTYMINIMIZER*/
   
 subject to {
   /*forceRedShirt: shirts["Red"] == 1; /* To force a solution with a red shirt */
   
   /* Required Constraints */
   redShirtGreyPants: (shirts["Red"] == 1 => pants["Grey"] == 1) && (pants["Grey"] == 1 => shirts["Red"] == 1); /* c1 */
   whiteShirtBlueOrDenimPants: (shirts["White"] == 1 => pants["Blue"] == 1 || pants["Denim"] == 1) && ((pants["Blue"] == 1 || pants["Denim"] == 1) => shirts["White"] == 1); /* c2 and c3 */
   //whiteShirtBluePants: (shirts["White"] == 1 => pants["Blue"] == 1) && (pants["Blue"] == 1 => shirts["White"] == 1); /* c2 */
   //whiteShirtDenimPants: (shirts["White"] == 1 =>  pants["Denim"] == 1) && (pants["Denim"] == 1 => shirts["White"] == 1); /* c3 */
   
   /* Strong Constraints */
   sneakersShoesDenimPants: (shoes["Sneakers"] == 1 => pants["Denim"] == 1) && (pants["Denim"] == 1 => shoes["Sneakers"] == 1); /* c4 */
   leatherShoesGreyPants: ((shoes["Leather"] == 1 => pants["Grey"] == 1) && (pants["Grey"] == 1 => shoes["Leather"] == 1)); /* c5 */
  		
  /* Weak Constraints */
  whiteShirtLeatherShoes: (shirts["White"] == 1 => shoes["Leather"] == 1) && (shoes["Leather"] == 1 => shirts["White"] == 1); /* c6 */
  
  /* Additional Constraints */
  blackJacketSneakersOrLeather: (jackets["Black"] == 1 => (shoes["Sneakers"] == 1 || shoes["Leather"] == 1)) && ((shoes["Sneakers"] == 1 || shoes["Leather"] == 1 ) => jackets["Black"] == 1); /* c10 */ 
  corduroyJacketSneakersOrLeather: (jackets["Corduroy"] == 1 => (shoes["Sneakers"] == 1 || shoes["Leather"] == 1)) && ((shoes["Sneakers"] == 1  || shoes["Leather"] == 1) => jackets["Corduroy"] == 1); /* c12 */
  redShirtBlackJacket: (shirts["Red"] == 1 => jackets["Black"] == 1) && (jackets["Black"] == 1 => shirts["Red"] == 1); /* c7 */
  whiteShirtBlackOrCorduroyJacket: (shirts["White"] == 1 => jackets["Black"] == 1 || jackets["Corduroy"] == 1) && ((jackets["Black"] == 1  || jackets["Corduroy"] == 1) => shirts["White"] == 1); /* c8 */
    		
   sum ( s in Shirts ) shirts[s]  == 1;
   sum ( p in Trousers ) pants[p] == 1;
   sum ( f in Footwear ) shoes[f] == 1;
   sum ( j in Jackets ) jackets[j] == 1;
 }
 
 /* RELATIONSHIPS:
  * # Required constraints are more important than strong ones
  * redShirtGreyPants >> sneakersShoesDenimPants
  * redShirtGreyPants >> leatherShoesGreyPants
  * whiteShirtBlueOrDenimPants >> sneakersShoesDenimPants
  * whiteShirtBlueOrDenimPants >> leatherShoesGreyPants
  * # Strong constraints are more important than weak ones
  * sneakersShoesDenimPants >> whiteShirtLeatherShoes
  * leatherShoesGreyPants >> whiteShirtLeatherShoes
  * # Weak constraints are more important than the one forcing a white shirt
  * # Additional relationships from the extended example
  * blackJacketSneakersOrLeather >> corduroyJacketSneakersOrLeather
  * redShirtBlackJacket >> whiteShirtBlackOrCorduroyJacket
  * redShirtBlackJacket >> redShirtGreyPants
  * blackJacketSneakersOrLeather >> sneakersShoesDenimPants
  */
 
 /* Postprocessing */
 tuple Outfit {
   string shirt;
   string shoes;
   string pants;
   string jacket;
 }
 
 {Outfit} outfits = { <s,f,t,j> | s in Shirts: shirts[s] == 1, f in Footwear: shoes[f] == 1, t in Trousers: pants[t] == 1, j in Jackets: jackets[j] == 1 };

execute DISPLAY {
  for ( var o in outfits ) {
  	writeln("A possible outfit: ", o);
 }  	
}