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
        pool (redis/create-pool {})]
    (dotimes [n 10000]
      (farmhand/enqueue pool {:fn-var #'job})
      (farmhand/enqueue pool {:fn-var #'fail}))
    (redis/close-pool pool)
    (print-duration "Queuing duration: " start)
    (let [processing-start (System/currentTimeMillis)]
      (farmhand/start-server {:num-workers 1})
      (while (not= (redis/with-jedis @farmhand/pool* jedis
                     (.llen jedis "farmhand:queue:default"))
                   0))
      (print-duration "Processing duration: " processing-start)
      (print-duration "Total duration: " start))
    (redis/with-jedis @farmhand/pool* jedis
      (assert (= (.zcard jedis "farmhand:completed") 10000))
      (assert (= (.zcard jedis "farmhand:dead") 10000)))
    (farmhand/stop-server)))
