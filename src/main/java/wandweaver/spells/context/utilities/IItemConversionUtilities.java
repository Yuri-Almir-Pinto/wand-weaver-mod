package wandweaver.spells.context.utilities;

import wandweaver.spells.context.data.IItemConversionData;

import java.util.List;

public interface IItemConversionUtilities {
    boolean canConvert(IItemConversionData data);
    void convert(IItemConversionData data);
    boolean attemptConversion(List<IItemConversionData> data);
}
