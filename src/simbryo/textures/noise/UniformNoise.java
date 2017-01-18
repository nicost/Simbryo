package simbryo.textures.noise;

import java.util.SplittableRandom;

import simbryo.textures.TextureGeneratorBase;
import simbryo.textures.TextureGeneratorInterface;

/**
 * Uniform noise
 * 
 * @author royer
 */
public class UniformNoise extends TextureGeneratorBase
                          implements TextureGeneratorInterface
{

  private SplittableRandom mRandom;

  /**
   * Instanciates a uniform noise object.
   * 
   * @param pDimension
   *          dimension
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
