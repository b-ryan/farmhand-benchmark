(defproject com.buckryan/farmhand-benchmark "0.1.0-SNAPSHOT"
  :url "https://github.com/b-ryan/farmhand-benchmark"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.buckryan/farmhand "0.9.0-SNAPSHOT"]
                 [log4j "1.2.17"]]
  :main ^:skip-aot farmhand.benchmark.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
