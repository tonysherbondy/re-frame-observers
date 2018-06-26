(ns lumanu.re-frame.observers
  (:require
   [reagent.core :as r]
   [reagent.ratom :as ratom]
   [re-frame.core :as rf]
   [re-frame.trace :as trace :include-macros true]))

(defonce reactions (atom {}))

(defn reg-obs [observer-id rf-vectors f]
  (let [subscriptions (into [] (map rf/subscribe rf-vectors))
        reaction      (ratom/make-reaction
                       (fn []
                         (let [subscription-vals (into [] (map deref subscriptions))]
                           ;; in the execution of f any derefed ratoms will not add to dependency chain
                           ;; e.g. if db is derefed any changes to db will not trigger this observation again
                           (binding [ratom/*ratom-context* nil]
                             (trace/with-trace
                              ;; TODO -- figure out how to hook our own op-type into re-frame-10x correctly
                              {:operation observer-id
                               :op-type   :sub/run
                               :tags      {:query-v    rf-vectors
                                           :reaction   observer-id}}
                              (apply f subscription-vals))))))]
    (r/rswap! reactions conj {observer-id reaction})))

(defn with-observers [component]
  (fn []
    (doseq [[observer-id r] @reactions]
      (deref r))
    component))
