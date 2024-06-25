
package com.example.blindapp.detection.customview;

import com.example.blindapp.detection.tflite.Classifier.Recognition;

import java.util.List;

public interface ResultsView {
  public void setResults(final List<Recognition> results);
}
