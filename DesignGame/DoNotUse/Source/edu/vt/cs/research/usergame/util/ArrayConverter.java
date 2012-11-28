package edu.vt.cs.research.usergame.util;

import edu.vt.cs.research.usergame.tuples.MomentCardTuple;
import edu.vt.cs.research.usergame.tuples.SignCardTuple;

public class ArrayConverter
{
    public static MomentCardTuple[] convertToMomentCardTupleArray(Object[] array)
    {
        int length = array.length;
        MomentCardTuple[] result = new MomentCardTuple[length];
        for (int i = 0; i < length; ++i)
        {
            result[i] = (MomentCardTuple)array[i];
        }
        return result;
    }

    public static SignCardTuple[] convertToSignCardTupleArray(Object[] array)
    {
        int length = array.length;
        SignCardTuple[] result = new SignCardTuple[length];
        for (int i = 0; i < length; ++i)
        {
            result[i] = (SignCardTuple)array[i];
        }
        return result;
    }
}
