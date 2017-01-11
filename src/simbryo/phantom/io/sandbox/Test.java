package simbryo.phantom.io.sandbox;

import static org.junit.Assert.*;

import java.io.IOException;

import org.scijava.io.IOService;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatWriter;
import loci.formats.ImageWriter;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;

public class Test
{

  @org.junit.Test
  public void test() throws DependencyException, ServiceException, FormatException, IOException
  {
    System.out.println("Creating random image...");
    int w = 512, h = 512, c = 1;
    int pixelType = FormatTools.UINT16;
    byte[] img = new byte[w * h * c * FormatTools.getBytesPerPixel(pixelType)];

    // fill with random data
    for (int i=0; i<img.length; i++) img[i] = (byte) (256 * Math.random());

    // create metadata object with minimum required metadata fields
    System.out.println("Populating metadata...");

    ServiceFactory factory = new ServiceFactory();
    OMEXMLService service = factory.getInstance(OMEXMLService.class);
    IMetadata meta = service.createOMEXMLMetadata();

    MetadataTools.populateMetadata(meta, 0, null, false, "XYZCT",
      FormatTools.getPixelTypeString(pixelType), w, h, 1, c, 1, c);

    String id ="test.tiff";
    // write image plane to disk
    System.out.println("Writing image to '" + id  + "'...");
    IFormatWriter writer = new ImageWriter();
    writer.setMetadataRetrieve(meta);
    writer.setId(id);
    writer.saveBytes(0, img);
    writer.close();

    System.out.println("Done.");
  }

}
