(ns automata
  (:require [clojure.browser.repl :as repl]))

(defn canvas []
  (-> js/document
      (.getElementById "canvas")
      (.getContext "2d")))

(defn black [c]
  (set! (.-fillStyle c) "rgb(0,0,0)"))

(defn draw-cell [c [x y] fill]
  (let [xpos (+ 147 (* 6 x)) ypos (* 6 y)]
    (if fill
      (.fillRect c xpos ypos 5 5)
      ;; (.strokeRect c xpos ypos 5 5)
      )))

(def sequence [(repeat 0) (lazy-seq (cons 1 (repeat 0)))])

(defn xcoords-lhs [cells]
  (let [end (- (inc (count cells)))]
    (range -1 end -1)))

(defn xcoords-rhs [cells]
  (range 0 (count cells)))

(defn draw-lhs [canvas row lhs]
  (doseq [cell (map (fn [x c] [canvas [x row] (= 1 c)]) (xcoords-lhs lhs) lhs)]
    (apply draw-cell cell)))

(defn draw-rhs [canvas row rhs]
  (doseq [cell (map (fn [x c] [canvas [x row] (= 1 c)]) (xcoords-rhs rhs) rhs)]
    (apply draw-cell cell)))

(defn draw-sequence [canvas row [lhs rhs]]
  (black canvas)
  (draw-lhs canvas row (take 50 lhs))
  (draw-rhs canvas row (take 50 rhs)))

(defn rand-row [length]
  (take length (repeatedly #(rand-nth [0 1]))))

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
                     (take 50 (iterate (partial evolve-seq rule) row-zero)))]
    (draw-sequence (canvas) r s)))

(defn get-checks []
  (map #(.getElementById js/document (str "cb-" %)) (range 0 8)))

(defn set-checks [rule]
  (doseq [[c cb] (map #(vector (bit-and rule %1) %2) [128 64 32 16 8 4 2 1] (get-checks))]
    (set! (.-checked cb) (not (= 0 c)))))

(defn decode-rule [& checks]
  (js/parseInt (apply str checks) 2))

(defn check-to-bit [check]
  (if (.-checked check) 1 0))

(defn draw-onclick []
  (draw-automata
   (apply decode-rule (map check-to-bit (get-checks)))
   sequence))

(set!
 (.-onclick (.getElementById js/document "draw"))
 draw-onclick)

(set!
 (.-onclick (.getElementById js/document "clear"))
 #(.clearRect (canvas) 0 0 300 300))

(defn draw-rules [start]
  (when (> start -1)
    (.clearRect (canvas) 0 0 300 300)
    (set-checks start)
    (draw-automata start sequence)
    (js/setTimeout #(draw-rules (dec start)) 5000)))

(repl/connect "http://localhost:9000/repl")