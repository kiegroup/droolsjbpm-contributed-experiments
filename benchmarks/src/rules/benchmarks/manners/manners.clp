(deftemplate guest
   (slot name) 
   (slot sex)
   (slot hobby) )
   
(deftemplate last_seat
   (slot seat) )
   
(deftemplate seating
   (slot seat1)
   (slot name1)
   (slot name2)
   (slot seat2)
   (slot id)
   (slot pid)
   (slot path_done) )
   
(deftemplate context
   (slot name) )
   
(deftemplate path
   (slot id)
   (slot name)
   (slot seat) )
   
(deftemplate chosen
   (slot id)
   (slot name)
   (slot hobby) )

(deftemplate count
   (slot c) )
   
(defrule assign_first_seat "assign the first seat"
   ?context <- (context (name "startup") )
   (guest (name ?n))
   ?count <- (count (c ?cnt) )
   =>
   (assert (seating (seat1 1) (name1 ?n) (name2 ?n) (seat2 1) (id ?cnt) (pid 0) (path_done TRUE) ) )
   (assert (path (id ?cnt) (name ?n) (seat 1) ) )
   (printout t "first seat seat1=1 name=" ?n " name2=" ?n " seat2=1 id=" ?cnt " pid=0 path_done TRUE" crlf)
   (bind ?cntPlus1 (+ ?cnt 1) )
   (modify ?count (c ?cntPlus1) )
   (modify ?context (name "assign_seats") )
)
(defrule find_seating "find seating for guest"
   ?context <- (context (name "assign_seats") )
   (seating (seat2 ?s2) (name2 ?n2) (id ?id) (path_done TRUE) )
   (guest (name ?n2) (sex ?sex1) (hobby ?h1) )
   (guest (name ?g2) (sex ~?sex1) (hobby ?h1) )
   ?count <- (count (c ?cnt) )
   (not (path (id ?id) (name ?g2) ) )
   (not (chosen (id ?id) (name ?g2) (hobby ?h1) ) )
=>
   (bind ?s2plus1 (+ ?s2 1) )
   (assert (seating (seat1 ?s2) (name1 ?n2) (name2 ?g2) (seat2 ?s2plus1) (id ?cnt) (pid ?id) (path_done FALSE) ) )
   (assert (path (id ?cnt) (name ?g2) (seat ?s2plus1) ) )
   (assert (chosen (id ?id) (name ?g2) (hobby ?h1) ) ) 
   (bind ?cntPlus1 (+ ?cnt 1) )
   (modify ?count (c ?cntPlus1) )
   (modify ?context (name "make_path") )
   (printout t "seat1=" ?s2 " : name1=" ?n2 " : seat2=" ?s2plus1 " : name2=" ?g2 " : id=" ?cnt " : pid=" ?id " : path_done=FALSE" crlf)
)
(defrule make_path
   ?context <- (context (name "make_path") )
   (seating (id ?id) (pid ?sPID) (path_done FALSE) )
   (path (id ?sPID) (name ?n1) (seat ?s) )
   (not (path (id ?id) (name ?n1) ) )
=>
   (assert (path (id ?id) (name ?n1) (seat ?s) ) )
;   (printout t "make_path - id=" ?id " name=" ?n1 " seat=" ?s crlf)
)
(defrule path_done "path is done"
   ?context <- (context (name "make_path") )
   ?seating <- (seating (path_done FALSE) )
=>
   (modify ?seating (path_done TRUE) )
   (modify ?context (name "check_done") )
;   (printout t "path is done " crlf)
)
(defrule continue "continue matching"
   ?context <- (context (name "check_done") )
=>
   (modify ?context (name "assign_seats") )
;   (printout t "assign seats again" crlf) 
)
(defrule are_we_done "are we done?"
   ?context <- (context (name "check_done") )
   (last_seat (seat ?lseat) )
   (seating (seat2 ?lseat) )
=>
   (printout t "Yes, we are done:  Print Results!!" crlf)
   (modify ?context (name "all_done") )
)
(defrule all_done "all done with test"
  ?context <- (context (name "all_done") )
=>
  (modify ?context (name "print_results") )
  (printout t crlf "rule all done: Condition context print_results" crlf )
)
(defrule print_results "print the results"
  (context (name "print_results") )
  ?seating <- (seating (id ?id) (seat2 ?s2) )
  (last_seat (seat ?s2) )
  (path (id ?id) (name ?n1) (seat ?s1) )
=>
  (printout t "name=" ?n1 " seat=" ?s1 crlf)
  (retract ?seating)
)
