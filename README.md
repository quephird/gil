# gil

For now, just a way of saving a quil sketch to an animated .gif file, and in a way that works just like saving a still frame in a non-looping sketch.

## Usage

Just like the `save` and `save-frame` functions, you can simply put a `save-animation` invocation in the draw method of a quil sketch at the place you would like to capture the current state of each frame. Below is an example sketch:

<pre>
(ns example
  (:use quil.core)
  (:use gil.core))

(def θ (atom 0))

(defn setup []
  (smooth)
  (ellipse-mode :center))

(defn draw []
  (background 0)
  (translate 250 250)
  (let [θ' (radians @θ)
        x (* 150 (cos θ'))
        y (* 150 (sin θ'))]
    (ellipse x y 50 50))
  (swap! θ + 20)
  (save-animation "orbiting-circle.gif" 18 0))

(sketch
  :setup setup
  :draw draw
  :renderer :p2d
  :size [500 500])
</pre>

The arguments for `save-animation` are:

* the name of the file
* the number of frames to be captured
* the delay (in centiseconds) between frames

## License

Copyright © 2014 

danielle kefford

Distributed under the Eclipse Public License.
