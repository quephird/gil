(ns gil.core
  (:use [quil.core]
        [quil.applet :only [current-applet]])
  (:import [java.io File FileWriter]
           [javax.imageio IIOImage ImageIO ImageTypeSpecifier]
           [javax.imageio.metadata IIOMetadataNode]))

; TODO: Consider making one data structure from these
;       or investigate how to extract the metadata and param objects from writer
(def writer (atom nil))
(def metadata (atom nil))
(def param (atom nil))

; TODO: COMMENTS!!!!!
;       make everything except save-animation private
(defn get-buffered-image [pimage]
  (.getImage pimage))

(defn make-metadata-node [name]
  (IIOMetadataNode. name))

(defn make-delay-node [delay-time]
  (doto (make-metadata-node "GraphicControlExtension")
    (.setAttribute "disposalMethod" "none")
    (.setAttribute "userInputFlag" "FALSE")
    (.setAttribute "transparentColorFlag" "FALSE")
    (.setAttribute "delayTime" (str delay-time))
    (.setAttribute "transparentColorIndex" "255")))

(defn make-loop-byte-array [loop-continuously?]
  (let [loop-continuously-int? (if loop-continuously? 0 1)
        first-byte 0x01
        second-byte (bit-and loop-continuously-int? 0xff)
        third-byte (bit-and (bit-shift-right loop-continuously-int? 8) 0xff)]
    (->> [first-byte second-byte third-byte]
      (map unchecked-byte)
      byte-array)))

(defn make-loop-node [loop-count]
  (doto (make-metadata-node "ApplicationExtension")
    (.setAttribute "applicationID" "NETSCAPE")
    (.setAttribute "authenticationCode" "2.0")
    (.setUserObject (make-loop-byte-array true))))

(defn make-app-ext-node []
  (doto (make-metadata-node "ApplicationExtensions")))

(defn append-child-node! [parent-node child-node]
  (.appendChild parent-node child-node))

(defn set-from-tree! [metadata format-name root-node]
  (.setFromTree metadata format-name root-node))

(defn set-output! [writer outputstream]
  (.setOutput writer outputstream))

(defn prepare-write-sequence! [writer metadata]
  (.prepareWriteSequence writer metadata))

(defn write-to-sequence! [writer image metadata param]
  (.writeToSequence writer (IIOImage. image nil metadata) param))

; TODO: Extract format name from file extension
;       Possibly dispatch on value or extension to support multiple formats
;       Throw exception for unsupported formats
;       Create helper functions for everything involving Java interop
(defn init-writer [filename format-name loop-count delay-time]
  (let [outputstream (ImageIO/createImageOutputStream (File. filename))
        current-image (get-buffered-image (get-pixel))
        image-type (.getType current-image)
        specifier (ImageTypeSpecifier/createFromBufferedImageType image-type)
        gif-writer (.next (ImageIO/getImageWriters specifier format-name))
        gif-param (.getDefaultWriteParam gif-writer)
        gif-metadata (.getDefaultImageMetadata gif-writer specifier gif-param)
        native-format-name (.getNativeMetadataFormatName gif-metadata)
        root-node (.getAsTree gif-metadata native-format-name)
        delay-node (make-delay-node delay-time)
        loop-node (make-loop-node loop-count)
        app-ext-node (make-app-ext-node)]
    ; ACHTUNG!!! The order in which nodes are added to the root matters below
    (append-child-node! app-ext-node loop-node)
    (append-child-node! root-node delay-node)
    (append-child-node! root-node app-ext-node)
    (set-from-tree! gif-metadata native-format-name root-node)
    (set-output! gif-writer outputstream)
    (prepare-write-sequence! gif-writer gif-metadata)
    (reset! metadata gif-metadata)
    (reset! param gif-param)
    (reset! writer gif-writer)
    ))

; TODO: Get rid of this
(defn init-globals [filename loop-count delay-time]
  (init-writer filename "GIF" loop-count delay-time))

(defn clean-up []
  (reset! metadata nil)
  (reset! param nil)
  (.dispose @writer))

; TODO: capture frame-count once into local variable
(defn save-animation [filename loop-count delay-time]
  (if (= 1 (frame-count))
    (init-globals filename loop-count delay-time))

  (if (<= (frame-count) loop-count)
    (let [current-image (get-buffered-image (get-pixel))]
      (write-to-sequence! @writer current-image @metadata @param)))

  (if (= (frame-count) loop-count)
    (clean-up))
  )
