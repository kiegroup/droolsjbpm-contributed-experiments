;; Manners.clp
;; Manners benchmark test
;; Adapted from CLIPS manners.ops
;; James C. Owen
;; 1/23/2002

(import benchmarks.manners.*)

( defclass guest Guest )
( defclass last_seat LastSeat )
( defclass seating Seating )
( defclass context Context )
( defclass path Path )
( defclass chosen Chosen )
( defclass count Count )
   
(defrule assign_first_seat ""
   (context (state 0) (OBJECT ?contextObj) )
   (guest (name ?n))
   (count ( value ?cnt) (OBJECT ?countObj) )
   =>
   (bind ?seating ( new Seating ?cnt 0 TRUE 1 ?n 1 ?n ) )
   (definstance seating ?seating static )
   (bind ?path ( new Path ?cnt 1 ?n ) )
   (definstance path ?path static )
   (set ?countObj "value" (+ ?cnt 1))
   (update ?countObj)
   ;(printout t "leftSeat :1: leftGuestName :" ?n ": rightSeat :1: rightGuestName :" ?n ": count :" (+ ?cnt 1) ": pid :0: pathDone TRUE" crlf)
   (set ?contextObj "state" 1 ) 
   (update ?contextObj)
)


(defrule find_seating ""
   (context (state 1) (OBJECT ?contextObj) )
   (seating (leftSeat ?s1) (leftGuestName ?n1) (rightGuestName ?n2) (rightSeat ?s2) (id ?seatID) (pid ?seatPID) (pathDone TRUE) )
   (guest (name ?n2) (sex ?sex1) (hobby ?h1) )
   (guest (name ?g2) (sex ~?sex1) (hobby ?h1) )
   (count ( value ?cnt) (OBJECT ?countObj) )
   (not (path (id ?seatID) (guestName ?g2) ) )
   (not (chosen (id ?seatID) (guestName ?g2) (hobby ?h1) ) )
   =>
   (bind ?s2plus1 (+ ?s2 1) )
   (definstance seating (new Seating ?cnt ?seatID FALSE ?s2 ?n2 ?s2plus1 ?g2) static)
   (definstance path (new Path ?cnt ?s2plus1 ?g2) static)
   (definstance chosen (new Chosen ?seatID ?g2 ?h1) static ) 
   
   (bind ?cntPlus1 (+ ?cnt 1) )
   (set ?countObj "value" ?cntPlus1 )
   (update ?countObj)
   
   (set ?contextObj "state" 2 ) 
   (update ?contextObj)

   ;(printout t "leftSeat :" ?s1 ": leftGuestName :" ?n2 ": rightSeat :" ?s2plus1 ": rightGuestName :" ?g2 ": count :" ?cnt ": pid :" ?seatID ": pathDone FALSE" crlf)
)

(defrule make_path ""
   (context (state 2) )
   (seating (leftSeat ?s1) (leftGuestName ?nam1) (rightSeat ?s2) (rightGuestName ?nam2) (id ?sID) (pid ?sPID) (pathDone FALSE) )
   (path (id ?sPID) (guestName ?n1) (seat ?s) )
   (not (path (id ?sID) (guestName ?n1) ) )
   =>
   (definstance path (new Path ?sID ?s ?n1) static)
)

(defrule pathDone ""
   (context (state 2) (OBJECT ?contextObj) )
   (seating (pathDone FALSE) (OBJECT ?seatingObj) )
   =>
   (set ?seatingObj "pathDone" TRUE ) 
   (update ?seatingObj)

   (set ?contextObj "state" 3 ) 
   (update ?contextObj)
)

(defrule are_we_done ""
   (context (state 3) (OBJECT ?contextObj) )
   (last_seat (seat ?l_seat) )
   (seating (rightSeat ?l_seat) )
   =>
   ;(printout t "rule are_we_done: Yes, we are done:  Print Results!!" crlf)

   (set ?contextObj "state" 4 ) 
   (update ?contextObj)
)


(defrule continue "continue processing"
   (context (state 3) (OBJECT ?contextObj) )
   =>
   (set ?contextObj "state" 1 ) 
   (update ?contextObj)
)

(defrule all_done ""
   (context (state 4) (OBJECT ?contextObj) )
   =>
   ;(printout t crlf "rule all done: Condition context print_results" crlf )
)
  
