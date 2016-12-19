package simbryo.textures.noise;

import java.util.SplittableRandom;

import simbryo.textures.TextureGeneratorBase;
import simbryo.textures.TextureGeneratorInterface;

/**
 * 
 * @author royer
 */
public class UniformNoise extends TextureGeneratorBase
                          implements TextureGeneratorInterface
{

  private SplittableRandom mRandom;
  

  /**
   * @param pDimension
   */
  public UniformNoise(int pDimension)
  {
    super(pDimension);
    mRandom = new SplittableRandom(); 
  }


  @Override
  public TextureGeneratorInterface clone()
  {
    UniformNoise lSimplexNoise = new UniformNoise(getDimension());
    return lSimplexNoise;
  }

  @Override
  public float sampleTexture(int... pCoordinate)
  {
    float lValue = (float) mRandom.nextDouble(-1, Math.nextUp(1));
    return lValue;
  }






}
