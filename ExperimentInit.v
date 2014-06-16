(* Coq Script: Experiment 1
   Author: Harley Eades       *) 

Parameter (P Q R T:Prop). 

(* 10 Lemma - Proof Transitions Group 1.
   All the lemmas in this section where taken
   from Coq'Art.

   @book{Bertot:2004, 
     author = {Bertot, Yves and Casteran, Pierre},
     title = {{Interactive Theorem Proving and Program Development. Coq'Art: The Calculus of Inductive Constructions}},
     publisher = {Springer Verlag},
     year = {2004},
     series = {Texts in Theoretical Computer Science}
   }
*)
Lemma L1 : (P -> Q) -> (Q -> R) -> P -> R.
Proof.
intro.
intro.
intro.
apply H0.
apply H.
assumption.
Qed.


Lemma L2 : (P -> Q -> R) -> (Q -> P -> R).
Proof.
intro.
intro.
intro.
apply H.
assumption.
assumption.
Qed.

Lemma L3 : (P -> Q) -> (P -> R) -> (Q -> R -> T) -> P -> T.
Proof.
intro.
intro.
intro.
intro.
apply H1.
apply H.
assumption.
apply H0.
assumption.
Qed.


Lemma L4 : (P -> Q -> R) -> (P -> Q) -> (P -> R).
Proof.
intro.
intro.
intro.
apply H.
assumption.
apply H0.
assumption.
Qed.

Lemma L5 : (P -> Q) -> P -> R -> ((P -> Q) -> R -> (T -> Q) -> T) -> T.
Proof.
intro.
intro.
intro.
intro.
apply H2.
assumption. 
assumption.
intro.
apply H.
assumption.
Qed.

Lemma L6 : ~(P \/ Q) -> ~P /\ ~Q.
Proof.
unfold not.
split.
intro.
apply H.
left.
assumption.
intro.
apply H.
right.
assumption.
Qed.

Lemma L7 : P -> Q -> (P /\ Q).
Proof.
split.
assumption.
assumption.
   
Qed.

Lemma L8 : P /\ (Q /\ R) -> (P /\ Q) /\ R.
Proof.
split.
split.
destruct H.
destruct H0.
apply H.
destruct H.
destruct H0.
apply H0.
destruct H.
apply H0.
Qed.

Lemma L9 : (P <-> Q) -> P -> Q.
Proof.
intro.
intro.
apply H.
assumption.
Qed.

Lemma L10 : (P <-> Q) -> (Q <-> P).
Proof.
split.
intro.
apply H.
assumption.
intro.
apply H.
assumption.

Qed.

(* 10 Lemma - Proof Transitions. 
   All the lemmas in this section where taken
   from Coq'Art.

   @book{Bertot:2004,
     author = {Bertot, Yves and Casteran, Pierre},
     title = {{Interactive Theorem Proving and Program Development. Coq'Art: The Calculus of Inductive Constructions}},
     publisher = {Springer Verlag},
     year = {2004},
     series = {Texts in Theoretical Computer Science}
   }
*)
Parameter (A B C : Prop).

Lemma M1 : C -> (A -> (C /\ A)).
Proof.
split.
assumption.
assumption.
Qed.

Lemma M2 : (A <-> C) -> (C <-> A).
Proof.
split.
intro.
apply H.
assumption.
intro.
apply H.
assumption.
Qed.

Lemma M3 : ~(A \/ C) -> ~A /\ ~C.
Proof.
unfold not.
split.
intro.
apply H.
left.
assumption.
intro.
apply H.
right.
apply H0.  
Qed.

Lemma M4 : (A -> C -> B) -> C -> A -> B.
Proof.
intro.
intro.
intro.
apply H.
assumption.
assumption.
  
Qed.

Lemma M5 : (C -> A) -> C -> (C -> A -> B) -> B.
Proof.
intro.
intro.
intro.
apply H1.
assumption.
apply H.
assumption.
Qed.

Lemma M6 :  (A -> B) -> C -> (C -> A) -> B.
Proof.
intro.
intro.
intro.
apply H.
apply H1.
assumption.
  
Qed.

Lemma M7 : (A -> B -> T) -> (C -> B) -> (C -> A) -> C -> T.
Proof.
intro.
intro.
intro.
intro.
apply H.
apply H1.
assumption.
apply H0.
assumption.
Qed.

Lemma M8 : (C /\ (A /\ B)) -> ((C /\ A) /\ B).
Proof.
split.
split.
apply H.
apply H.
apply H.
Qed.

Lemma M9 : (C -> A) -> B -> ((C -> A) -> (T -> A) -> B -> T) -> C -> T.
Proof.
intro.
intro.
intro.
intro.
apply H1.
assumption.
intro.
apply H.
assumption.
assumption.
Qed.

Lemma M10 : (C <-> A) -> A -> C.
Proof.
intro.
intro.
apply H.
assumption.
Qed.

(* 2 Lemmas - For Proof Transitions (More Complicated). 
   These two lemmas are from Software Foundations:
   @book{Pierce:SF,
     author = {Benjamin C. Pierce and Chris Casinghino and 
               Michael Greenberg and C\v{a}t\v{a}lin Hri\c{t}cu and Vilhelm Sjoberg and Brent Yorgey},
     title = {Software Foundations},
     year = {2012},
     publisher = {Electronic textbook},
     plclub = {Yes},
     bcp = {Yes},
     keys = {poplmark,books},
     note = {\URL{http://www.cis.upenn.edu/~bcpierce/sf}},
     ebook = {http://www.cis.upenn.edu/~bcpierce/sf},
     japanese = {http://proofcafe.org/sf}
   }
*)
Lemma iff_trans : (P <-> Q) -> (Q <-> R) -> (P <-> R).
Proof.
split.
intro.
apply H0.
apply H0.
apply H0.
apply H0.
apply H0.
apply H0.
apply H0.
apply H0.
apply H0.
apply H0.
apply H0.
apply H.
assumption.
intro.
apply H.
apply H0.
assumption.
  
Qed.

Lemma forall_not_exists : forall (X:Type) (P:X -> Prop),
  (forall a, (P a)) -> ~(exists a, ~ P a).
Proof.
unfold not.
intro.
intro.
intro.
intro.
destruct H0.
apply H0.
destruct H0.
apply H.
Qed.

(* 2 Lemmas - For Proof Previews. 
   These two lemmas I made up.
*)
Lemma imp_strange : (P /\ R -> Q) -> (A -> B \/ C) -> (B -> P) -> (R <-> C) -> (C -> B) -> (A -> R) -> A -> Q.
Proof.
intro.
intro.
intro.
intro.
intro.
intro.
intro.
apply H.
split.
apply H1.
apply H3.
apply H2.
apply H2.
apply H2.
apply H2.
apply H2.
apply H2.
apply H2.
apply H4.
apply H5.
apply H4.
apply H5.
  
Qed.

Lemma N4 : forall (X:Type) (P:X -> Prop) (Q:X -> Prop),
  (forall a, P a) /\ (forall a, Q a) ->
  (forall a, P a /\ Q a).
Proof.
split.
apply H.
apply H.
  
Qed.

