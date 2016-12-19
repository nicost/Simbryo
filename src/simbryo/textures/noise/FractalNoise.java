package simbryo.textures.noise;

import simbryo.textures.TextureGeneratorBase;
import simbryo.textures.TextureGeneratorInterface;

/**
 * Fractal noise generator. Given a fratal noise generator it generates a
 * multi-scale version of it.
 *
 * @author royer
 */
public class FractalNoise extends TextureGeneratorBase
                          implements TextureGeneratorInterface
{

  private TextureGeneratorInterface[] mTextureGenerators;
  private float[] mScales;

  /**
   * 
   * @param pTextureGenerator
   *          texture generator to be used for each scale
   * @param pScales
   *          scales to use for each 'octave' of noise.
   */
  public FractalNoise(TextureGeneratorInterface pTextureGenerator,
                      float... pScales)
  {
    super(pTextureGenerator.getDimension());
    mScales = pScales;
    int lNumberOfScales = mScales.length;
    mTextureGenerators =
                       new TextureGeneratorInterface[lNumberOfScales];
    for (int i = 0; i < lNumberOfScales; i++)
      mTextureGenerators[i] = pTextureGenerator.clone();
    for (int i = 0; i < lNumberOfScales; i++)
      mTextureGenerators[i].setScales(mScales[i]);
  }

  @Override
  public TextureGeneratorInterface clone()
  {
    return new SimplexNoise(getDimension());
  }

  @Override
  public float sampleTexture(int... pCoordinate)
  {
    int lNumberOfScales = mScales.length;
    float lValue = 0;
    for (int i = 0; i < lNumberOfScales; i++)
    {
      lValue += mTextureGenerators[i].sampleTexture(pCoordinate);
    }
    return lValue;
  }

}
