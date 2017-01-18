package simbryo.phantom.io;

import java.io.File;

import simbryo.phantom.PhantomRendererInterface;

/**
 * Phantom TIFF writer
 * 
 * TODO: not working until someone figures out how to save tiff files, which
 * does not seem to be me ;-)
 *
 * @author royer
 */
public class PhantomTiffWriter extends PhantomWriterBase
                               implements PhantomWriterInterface
{

  // private OffHeapMemory mTransferMemory;
  // private byte[] mTransferArray;

  /**
   * Instanciates a Phantom raw writer. The voxel values produced by the phantom
   * are scaled accoding to y = a*x+b.
   * 
   * @param pScaling
   *          value scaling a
   * @param pOffset
   *          value offset b
   */
  public PhantomTiffWriter(float pScaling, float pOffset)
  {
    super(pScaling, pOffset);
  }

  @Override
  public boolean write(PhantomRendererInterface<?> pPhantomRenderer,
                       File pFile) throws Throwable
  {
    return pFile.getParentFile().mkdirs();

    /*
    int lWidth = (int) pPhantomRenderer.getWidth();
    int lHeight = (int) pPhantomRenderer.getHeight();
    int lDepth = (int) pPhantomRenderer.getDepth();
    
    int lPixelType = FormatTools.UINT16;
    long lUINT16BitSizeInBytes = lWidth * lHeight
                                 * FormatTools.getBytesPerPixel(lPixelType);
    long lFloatSizeInBytes = lWidth * lHeight * lDepth * Size.FLOAT;
    
    if (mTransferMemory == null
        || lFloatSizeInBytes != mTransferMemory.getSizeInBytes())
    {
      mTransferMemory =
                      OffHeapMemory.allocateBytes("PhantomTiffWriter",
                                                  lFloatSizeInBytes);
    }
    
    if (mTransferArray == null
        || lUINT16BitSizeInBytes != mTransferArray.length
                                    * Size.SHORT)
    {
      mTransferArray =
                     new byte[Math.toIntExact(lUINT16BitSizeInBytes)];
    }
    
    pPhantomRenderer.copyTo(mTransferMemory, true);
    
    ServiceFactory factory = new ServiceFactory();
    OMEXMLService service = factory.getInstance(OMEXMLService.class);
    IMetadata meta = service.createOMEXMLMetadata();
    
    MetadataTools.populateMetadata(meta,
                                   0,
                                   null,
                                   false,
                                   "XYZCT",
                                   FormatTools.getPixelTypeString(lPixelType),
                                   lWidth,
                                   lHeight,
                                   lDepth,
                                   1,
                                   1,
                                   1);
    
    String lFileName = pFile.getAbsolutePath();
    
    System.out.println("Writing image to '" + lFileName + "'...");
    IFormatWriter writer = new ImageWriter();
    writer.setMetadataRetrieve(meta);
    writer.setId(lFileName);
    
    ContiguousBuffer lBuffer = new ContiguousBuffer(mTransferMemory);
    
    for (int z = 0; z < lDepth; z++)
    {
      int i = 0;
      while (lBuffer.hasRemainingFloat() && i<mTransferArray.length)
      {
        float lFloatValue = lBuffer.readFloat() * mScaling + mOffset;
        int lIntValue = Math.round(lFloatValue);
        byte lLowByte = (byte) (lIntValue & 0xFF);
        byte lHighByte = (byte) ((lIntValue >> 8) & 0xFF);
    
        mTransferArray[i++] = lHighByte;
        mTransferArray[i++] = lLowByte;
      }
      
      System.out.println("length="+mTransferArray.length);
      writer.saveBytes(z, mTransferArray);
    }
    writer.close();
    
    System.out.println("Done.");/**/

  }

}
