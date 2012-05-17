(ns automata
  (:require [clojure.browser.repl :as repl]))

(defn canvas []
  (-> js/document
      (.getElementById "canvas")
      (.getContext "2d")))

(defn draw-cell [c [x y] fill]
  (set! (.-fillStyle c) "rgb(0,0,0)")
  (let [xpos (* 6 x) ypos (* 6 y)]
    (if fill
      (.fillRect c xpos ypos 5 5)
      ;; (.strokeRect c xpos ypos 5 5)
      )))

(def sequence [0 [true]])

(defn xcoords [start cells]
  (let [end (+ start (count cells))]
    (range start end)))

(defn draw-sequence [canvas row [start cells]]
  (doseq [cell (map (fn [x c] [canvas [x row] c]) (xcoords start cells) cells)]
    (apply draw-cell cell)))

(defn rand-row [length]
  (take length (repeatedly #(rand-nth [true false]))))


(repl/connect "http://localhost:9000/repl")