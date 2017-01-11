package simbryo.phantom.io.sandbox;

import java.io.IOException;

import org.scijava.io.DefaultIOService;
import org.scijava.io.IOService;

import io.scif.services.SCIFIODatasetService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.img.array.ArrayImgs;

public class Test
{

  @org.junit.Test
  public void test() throws 
                     IOException
  {
    DatasetService lDatasetService = new SCIFIODatasetService();
    IOService lIOService = new DefaultIOService();
    int width = 100;
    int height = 100;
    float[] myFloatArray = new float[width * height];
    final Dataset d =
                    lDatasetService.create(ArrayImgs.floats(myFloatArray,
                                                            new long[]
                                                            { width, height }));
    lIOService.save(d, "myFile.tif");

  }

}
