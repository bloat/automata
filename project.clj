(defproject automata "1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "0.1.9"]]
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :cljsbuild {:builds [{:source-path "cljs"
                        :compiler {:output-to "site/automata.js"
                                   :pretty-print true}}]})
