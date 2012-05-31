(ns automata
  (:require [clojure.browser.repl :as repl]
            [goog.dom :as dom]
            [goog.events :as ev]))

(def CANVAS-SIZE 300)
(def CELL-SIZE 5)
(def CELL-GAP 1)
(def CELL-INTERVAL (+ CELL-SIZE CELL-GAP))
(def V-CELLS (int (/ CANVAS-SIZE 6)))
(def LHS-CELLS (int (/ CANVAS-SIZE 12)))
(def RHS-CELLS LHS-CELLS)

(def CANVAS
  (-> (dom/getElement "canvas")
      (.getContext "2d")))

(defn black [c]
  (set! (.-fillStyle c) "rgb(0,0,0)"))

(defn draw-cell [[x y] fill]
  (let [xpos (+ (- (/ CANVAS-SIZE 2) CELL-INTERVAL) (* CELL-INTERVAL x))
        ypos (* CELL-INTERVAL y)]
    (when fill
      (.fillRect CANVAS xpos ypos CELL-SIZE CELL-SIZE))))

(def sequence [(repeat 0) (lazy-seq (cons 1 (repeat 0)))])

(defn xcoords-lhs [cells]
  (let [end (- (inc (count cells)))]
    (range -1 end -1)))

(defn xcoords-rhs [cells]
  (range 0 (count cells)))

(defn draw-lhs [row lhs]
  (doseq [cell (map (fn [x c] [[x row] (= 1 c)]) (xcoords-lhs lhs) lhs)]
    (apply draw-cell cell)))

(defn draw-rhs [row rhs]
  (doseq [cell (map (fn [x c] [[x row] (= 1 c)]) (xcoords-rhs rhs) rhs)]
    (apply draw-cell cell)))

(defn draw-sequence [row [lhs rhs]]
  (black CANVAS)
  (draw-lhs row (take LHS-CELLS lhs))
  (draw-rhs row (take RHS-CELLS rhs)))

(defn rand-row []
  [(repeatedly #(rand-nth [0 1])) (repeatedly #(rand-nth [0 1]))])

(defn evolve-cell [rule [in1 in2 in3]]
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
  (draw-automata (checks-value) (rand-row)))

(doseq [i (range 0 8)]
  (ev/listen (dom/getElement (str "cb-" i))
             ev/EventType.CLICK
             #(set! (.-value (dom/getElement "rule-no")) (checks-value))))

(ev/listen (dom/getElement "draw")
           ev/EventType.CLICK
           draw-onclick)

(ev/listen (dom/getElement "clear")
           ev/EventType.CLICK
           #(.clearRect CANVAS 0 0 CANVAS-SIZE CANVAS-SIZE))

(ev/listen (dom/getElement "rule-no")
           ev/EventType.KEYUP
           #(set-checks (js/parseInt (.-value (dom/getElement "rule-no")))))

(defn draw-rules [start]
  (when (> start -1)
    (.clearRect CANVAS 0 0 CANVAS-SIZE CANVAS-SIZE)
    (set-checks start)
    (draw-automata start (rand-row))
    (js/setTimeout #(draw-rules (dec start)) 3000)))

(repl/connect "http://localhost:9000/repl")