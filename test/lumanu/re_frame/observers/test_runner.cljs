(ns lumanu.re-frame.observers.test-runner
  (:require
    [cljs.test         :as cljs-test :include-macros true]
    [jx.reporter.karma :as karma :include-macros true]
    ;; Test Namespaces -------------------------------
    [lumanu.re-frame.observers.core-test])
  (:refer-clojure :exclude (set-print-fn!)))

(enable-console-print!)

;; ---- BROWSER based tests ----------------------------------------------------
(defn ^:export set-print-fn! [f]
  (set! cljs.core.*print-fn* f))


(defn ^:export run-html-tests []
  (cljs-test/run-tests
    'lumanu.re-frame.observers.core-test))

;; ---- KARMA  -----------------------------------------------------------------

(defn ^:export run-karma [karma]
  (karma/run-tests
    karma
    'lumanu.re-frame.observers.core-test))
