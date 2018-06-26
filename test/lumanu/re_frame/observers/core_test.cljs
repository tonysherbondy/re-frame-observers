(ns lumanu.re-frame.observers.core-test
  (:require [cljs.test :refer-macros [is deftest async]]
            [lumanu.re-frame.observers :as obs]))

(deftest test1
  (let [app-db (atom #{})]
    (is (= @app-db #{}))))
