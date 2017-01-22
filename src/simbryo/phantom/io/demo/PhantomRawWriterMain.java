package simbryo.phantom.io.demo;

import java.io.File;
import java.io.IOException;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.util.ElapsedTime;
import coremem.enums.NativeTypeEnum;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.phantom.ClearCLPhantomRendererUtils;
import simbryo.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.phantom.io.PhantomRawWriter;

/**
 * Phantom raw writer main program
 *
 * @author royer
 */
public class PhantomRawWriterMain
{

	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(final String[] args)
	{
		try
		{
			write(args[0]);
		}
		catch (final Throwable e)
		{
			e.printStackTrace();
		}

	}

	private static void write(final String pFolderPathString) throws IOException
	{
		/*String lUserHome = System.getProperty("user.home");
		File lDownloadFolder = new File(lUserHome + "/Downloads/");
		File lDataFolder = new File(lDownloadFolder, "DrosoStacks");/**/

		final File lDataFolder = new File(pFolderPathString);

		final int lWidth = 512;
		final int lHeight = 512;
		final int lDepth = 512;

		ElapsedTime.sStandardOutput = true;

		final ClearCLBackendInterface lBestBackend = ClearCLBackends.getBestBackend();
		System.out.println("lBestBackend=" + lBestBackend);
		try (ClearCL lClearCL = new ClearCL(lBestBackend);
				ClearCLDevice lFastestGPUDevice =
																				lClearCL.getFastestGPUDeviceForImages();
		    ClearCLContext lContext = lFastestGPUDevice.createContext())
		{
			System.out.println("lFastestGPUDevice=" + lFastestGPUDevice);

			final int[] lGridDimensions =
																	ClearCLPhantomRendererUtils.getOptimalGridDimensions(	lFastestGPUDevice,
																																												lWidth,
																																												lHeight,
																																												lDepth);

			final Drosophila lDrosophila = new Drosophila(16,
																										lGridDimensions);

			final DrosophilaHistoneFluorescence lDrosoFluo = new DrosophilaHistoneFluorescence(	lContext,
																																													lDrosophila,
																																													lWidth,
																																													lHeight,
																																													lDepth);

			final PhantomRawWriter lPhantomRawWriter = new PhantomRawWriter(100,
																																			0);
			lPhantomRawWriter.setOverwrite(false);
			lPhantomRawWriter.setDataType(NativeTypeEnum.Byte);

			// lDrosophila.simulationSteps(14000, 1);

			// ClearCLImageViewer lOpenViewer = lDrosoFluo.openViewer();

			final int lPeriod = 10;

			while (lDrosophila.getTimeStepIndex() < 15000)
			{
				lDrosophila.simulationSteps(lPeriod, 1);
				final long lTimeIndex = lDrosophila.getTimeStepIndex();

				lDrosoFluo.clear();
				lDrosoFluo.render();

				final File lFile = new File(lDataFolder,
																		String.format("stack.%d.%d.%d.%d.%s.raw",
																									lWidth,
																									lHeight,
																									lDepth,
																									lTimeIndex,
																									lPhantomRawWriter.getDataType()));

				if (lPhantomRawWriter.write(lDrosoFluo, lFile))
				{
					System.out.println("Writting file: " + lFile);
				}
			}

			lDrosoFluo.close();

		}
	}

}
