(ns automata
  (:require [clojure.browser.repl :as repl]
            [goog.dom :as dom]
            [goog.events :as ev]))

;; TODO retreive canvas size from the document
(def CANVAS-SIZE "The width and height of the canvas." 300)

(def CELL-SIZE "The size of each cell in pixels." 5)

(def CELL-GAP "The size of the gap between cells, in pixels." 1)

(def CELL-INTERVAL "The gap between the start of one cell, and the start of the next." (+ CELL-SIZE CELL-GAP))

(def V-CELLS "The number of cells in a column on the canvas." (int (/ CANVAS-SIZE 6)))

(def LHS-CELLS "The number of cells on the left hand side of a row on the canvas." (int (/ CANVAS-SIZE 12)))

(def RHS-CELLS "The number of cells on the left hand side of a row on the canvas." LHS-CELLS)

(def CANVAS "The graphics element from the canvas."
  (-> (dom/getElement "canvas")
      (.getContext "2d")))

(defn black 
  "Sets the color of the canvas"
  [c]
  (set! (.-fillStyle c) "rgb(0,0,0)"))

(defn draw-cell
  "When fill is true, draws a single cell on the canvas, otherwise leaves it blank.
The input coordinates are given in terms of cells, and converted here into pixel coordinates."
  [[x y] fill]
  (when fill
    (let [xpos (+ (- (/ CANVAS-SIZE 2) CELL-INTERVAL) (* CELL-INTERVAL x))
          ypos (* CELL-INTERVAL y)]
      (.fillRect CANVAS xpos ypos CELL-SIZE CELL-SIZE))))

(defn clear-canvas
  "Clears the entire canvas."
  []
  (.clearRect CANVAS 0 0 CANVAS-SIZE CANVAS-SIZE))

;; A row in the automata is represented by a vector of two infinite sequences.
;; The first sequence is the cells from the center out to the left, and the
;; second is the cells from the center out to the right.
;; A 0 indicates a non-live (white) cell, and a 1 indicates a live (black cell).

(defn middle-cell
  "Returns a row with one cell live in the center."
  []
  [(repeat 0) (lazy-seq (cons 1 (repeat 0)))])

(defn white-row
  "Returns a row with no cells live."
  []
  [(repeat 0) (repeat 0)])

(defn black-row
  "Returns a row with all cells live."
  []
  [(repeat 1) (repeat 1)])

(defn rand-row
  "Returns a random row."
  []
  [(repeatedly #(rand-nth [0 1])) (repeatedly #(rand-nth [0 1]))])

(def row-types
  "A map from the names of the row types (as entered by the user)
to the actual row type functions."
  {"middle-cell" middle-cell
   "white-row" white-row
   "black-row" black-row
   "rand-row" rand-row})

(def start-row
  "Holds the current starting row, set whenever the user selects a new row type."
  (atom (middle-cell)))

(defn xcoords-lhs
  "Returns the cell x-coordinates for a finite sequence of cells on the
left hand side of the automata."
  [cells]
  (let [end (- (inc (count cells)))]
    (range -1 end -1)))

(defn xcoords-rhs
  "Returns the cell x-coordinates for a finite sequence of cells on the
right hand side of the automata."
  [cells]
  (range 0 (count cells)))

(defn draw-lhs
  "Given a row number, and a finite sequence of cells, draws the cells on the
left hand side of the automata."
  [row lhs]
  (doseq [cell (map (fn [x c] [[x row] (= 1 c)]) (xcoords-lhs lhs) lhs)]
    (apply draw-cell cell)))

(defn draw-rhs
  "Given a row number, and a finite sequence of cells, draws the cells on the
right hand side of the automata."
  [row rhs]
  (doseq [cell (map (fn [x c] [[x row] (= 1 c)]) (xcoords-rhs rhs) rhs)]
    (apply draw-cell cell)))

(defn draw-sequence
  "Draws the given row on the canvas, where a row is represented by
two (possibly infinite) sequences of cells."
  [row [lhs rhs]]
  (black CANVAS)
  (draw-lhs row (take LHS-CELLS lhs))
  (draw-rhs row (take RHS-CELLS rhs)))

(defn evolve-cell
  "Take a rule number and three input cells, and produces the value for the outptut cell."
  [rule [in1 in2 in3]]
  (if (= 0 (bit-and
            rule
            (Math/pow 2 (js/parseInt (str in1 in2 in3) 2))))
    0
    1))

(defn evolve-lhs [rule lhs rhs]
  (map (comp (partial evolve-cell rule) reverse) (partition 3 1 (cons (first rhs) lhs))))

(defn evolve-rhs [rule lhs rhs]
  (map (partial evolve-cell rule) (partition 3 1 (cons (first lhs) rhs))))

(defn evolve-seq [rule [lhs rhs]]
  [(evolve-lhs rule lhs rhs)
   (evolve-rhs rule lhs rhs)])

(defn draw-automata [rule row-zero]
  (doseq [[r s] (map vector
                     (range)
                     (take V-CELLS (iterate (partial evolve-seq rule) row-zero)))]
    (draw-sequence r s)))

(defn get-checks []
  (map #(dom/getElement (str "cb-" %)) (range 0 8)))

(defn set-checks [rule]
  (doseq [[c cb] (map #(vector (bit-and rule %1) %2) [128 64 32 16 8 4 2 1] (get-checks))]
    (set! (.-checked cb) (not (= 0 c)))))

(defn decode-rule [checks]
  (js/parseInt (apply str checks) 2))

(defn check-to-bit [check]
  (if (.-checked check) 1 0))

(defn checks-value []
  (decode-rule (map check-to-bit (get-checks))))

(defn draw-onclick []
  (draw-automata (checks-value) @start-row))

(defn get-row [row-type]
  (if (row-types row-type)
    ((row-types row-type))
    (white-row)))

(defn draw-first-row []
  (clear-canvas)
  (draw-sequence 0 @start-row))

(let [rule-no (dom/getElement "rule-no")
      draw (dom/getElement "draw")
      clear (dom/getElement "clear")
      start (dom/getElement "start")]

  (doseq [i (range 0 8)]
          (ev/listen (dom/getElement (str "cb-" i))
                     ev/EventType.CLICK
                     #(set! (.-value rule-no) (checks-value))))

  (ev/listen draw ev/EventType.CLICK draw-onclick)

  (ev/listen clear
             ev/EventType.CLICK
             (do (clear-canvas)
                 (draw-first-row)))

  (ev/listen rule-no
             ev/EventType.KEYUP
             #(set-checks (js/parseInt (.-value rule-no))))

  (ev/listen start
             ev/EventType.KEYUP
             #(let [new-start-row (get-row (.-value start))]
                (reset! start-row new-start-row)
                (draw-first-row))))

(defn draw-rules [start]
  (when (> start -1)
    (.clearRect CANVAS 0 0 CANVAS-SIZE CANVAS-SIZE)
    (set-checks start)
    (draw-automata start (rand-row))
    (js/setTimeout #(draw-rules (dec start)) 3000)))

(repl/connect "http://localhost:9000/repl")

;; This file is part of Andrew's Automata.

;; Andrew's Automata is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.

;; Andrew's Automata is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
;; GNU General Public License for more details.

;; You should have received a copy of the GNU General Public License
;; along with Andrew's Automata. If not, see <http://www.gnu.org/licenses/>.

