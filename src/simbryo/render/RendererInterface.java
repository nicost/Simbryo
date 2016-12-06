package simbryo.render;

import clearcl.viewer.ClearCLImageViewer;

public interface RendererInterface
{

  long getWidth();

  long getHeight();

  long getDepth();

  void clear();

  boolean render(int pZPlaneIndex);

  void render(int pZPlaneIndexBegin, int pZPlaneIndexEnd);

  int renderSmart(int pZPlaneIndexBegin, int pZPlaneIndexEnd);

  void invalidate(int pZPlaneIndex);

  ClearCLImageViewer openFluorescenceImageViewer();

}
