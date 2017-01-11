package simbryo.phantom.io;

import java.io.File;
import java.io.IOException;

import io.scif.DependencyException;
import io.scif.FormatException;
import io.scif.services.ServiceException;
import simbryo.phantom.PhantomRendererInterface;

public interface PhantomWriterInterface
{

  /**
   * Renders a phantom stack to a file.
   * @param pPhantomRenderer phantom renderer
   * @param pFile file to write to
   * @throws DependencyException 
   * @throws ServiceException 
   * @throws IOException 
   * @throws FormatException 
   */
  void write(PhantomRendererInterface pPhantomRenderer, File pFile) throws Throwable;

}
