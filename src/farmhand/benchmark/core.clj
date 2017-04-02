(ns farmhand.benchmark.core
  (:require [clojure.java.shell :as sh]
            [farmhand.core :as farmhand]
            [farmhand.redis :as redis])
  (:gen-class))

(def counter (atom 0))
(defn job [] (swap! counter inc))
(defn fail [] (throw (Exception. "foo")))

(defn- print-duration
  [msg start]
  (println msg (/ (- (System/currentTimeMillis) start)
                  1000.0)))

(defn -main
  [& args]
  ;; Really basic test - queue a bunch of jobs, wait for them all to be
  ;; processed
  (let [start (System/currentTimeMillis)
        context (farmhand/create-context {:prefix "farmhand-benchmark:"})]
    (dotimes [n 10000]
      (farmhand/enqueue context {:fn-var #'job})
      (farmhand/enqueue context {:fn-var #'fail}))
    (print-duration "Queuing duration: " start)
    (let [processing-start (System/currentTimeMillis)]
      (farmhand/start-server {:num-workers 1 :context context})
      (while (not= (redis/with-jedis [{:keys [jedis]} @farmhand/context*]
                     (.llen jedis "farmhand-benchmark:queue:default"))
                   0))
      (print-duration "Processing duration: " processing-start)
      (print-duration "Total duration: " start))
    (redis/with-jedis [{:keys [jedis]} @farmhand/context*]
      (assert (= (.zcard jedis "farmhand-benchmark:completed") 10000))
      (assert (= (.zcard jedis "farmhand-benchmark:dead") 10000)))
    (farmhand/stop-server)))
