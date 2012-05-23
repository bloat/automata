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

(def sequence [0 [1]])

(defn xcoords [start cells]
  (let [end (+ start (count cells))]
    (range start end)))

(defn draw-sequence [canvas row [start cells]]
  (black canvas)
  (doseq [cell (map (fn [x c] [canvas [x row]  (= 1 c)]) (xcoords start cells) cells)]
    (apply draw-cell cell)))

(defn rand-row [length]
  (take length (repeatedly #(rand-nth [0 1]))))

(defn evolve-cell [rule [in1 in2 in3]]
  (if (= 0 (bit-and
            rule
            (Math/pow 2 (js/parseInt (str in1 in2 in3) 2))))
    0
    1))

(defn evolve [rule row] 
  (map (partial evolve-cell rule) (partition 3 1 (concat [0 0] row [0 0]))))

(defn evolve-seq [rule [start row]]
  [(dec start) (evolve rule row)])

(defn draw-automata [rule row-zero]
  (doseq [[r s] (map vector
                     (range)
                     (take 60 (iterate (partial evolve-seq rule) row-zero)))]
    (draw-sequence (canvas) r s)))

(defn get-checks []
  (map #(.getElementById js/document (str "cb-" %)) (range 0 8)))

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

(repl/connect "http://localhost:9000/repl")