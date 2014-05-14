(ns gil.orbiting-circle
  (:use quil.core)
  (:use gil.core))

(def θ (atom 0))

(defn setup []
  (smooth)
  (ellipse-mode :center)
  )

(defn draw []
  (background 0)
  (translate 250 250)
  (let [θ' (radians @θ)
        x (* 150 (cos θ'))
        y (* 150 (sin θ'))]
    (ellipse x y 50 50))

  (swap! θ + 20)
  (save-animation "orbiting-circle.gif" 18 0)
  )

(sketch
  :title "orbiting circle"
  :setup setup
  :draw draw
  :renderer :java2d
  :size [500 500])
