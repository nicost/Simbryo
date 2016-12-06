package simbryo.phantom;

public interface PhantomRendererInterface
{

  long getWidth();

  long getHeight();

  long getDepth();

  void clear();

  boolean render(int pZPlaneIndex);

  void render(int pZPlaneIndexBegin, int pZPlaneIndexEnd);

  int renderSmart(int pZPlaneIndexBegin, int pZPlaneIndexEnd);

  void invalidate(int pZPlaneIndex);

}
