package simbryo.render;

import clearcl.viewer.ClearCLImageViewer;

public interface RendererInterface
{
  
  long getWidth();

  long getHeight();

  long getDepth();

  void clear();
  
  boolean render(int pZPLaneIndex);

  ClearCLImageViewer openFluorescenceImageViewer();

}
