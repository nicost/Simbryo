package simbryo.phantom.io;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coremem.buffers.ContiguousBuffer;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;
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
import simbryo.phantom.PhantomRendererInterface;

public class PhantomRawWriter extends PhantomWriterBase implements PhantomWriterInterface
{

  private OffHeapMemory mTransferMemory;



  public PhantomRawWriter(float pScaling, float pOffset)
  {
    super(pScaling,pOffset);
  }

  @Override
  public void write(PhantomRendererInterface pPhantomRenderer,
                    File pFile) throws DependencyException,
                                ServiceException,
                                FormatException,
                                IOException
  {
    pFile.getParentFile().mkdirs();

    int lWidth = (int) pPhantomRenderer.getWidth();
    int lHeight = (int) pPhantomRenderer.getHeight();
    int lDepth = (int) pPhantomRenderer.getDepth();

    long lFloatSizeInBytes = lWidth * lHeight * lDepth * Size.FLOAT;

    if (mTransferMemory == null
        || lFloatSizeInBytes != mTransferMemory.getSizeInBytes())
    {
      mTransferMemory =
                      OffHeapMemory.allocateBytes("PhantomRawWriter",
                                                  lFloatSizeInBytes);
    }

    pPhantomRenderer.copyTo(mTransferMemory, true);
    
    
    Path lFilePath = pFile.toPath();
    FileChannel lFileChannel = FileChannel.open(lFilePath, StandardOpenOption.CREATE,StandardOpenOption.WRITE);
    mTransferMemory.writeBytesToFileChannel(lFileChannel, 0);
    lFileChannel.close();

  }

}
