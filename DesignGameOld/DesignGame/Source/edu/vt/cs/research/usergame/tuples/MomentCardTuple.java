package edu.vt.cs.research.usergame.tuples;

import com.ibm.tspaces.TupleSpaceException;
import com.sri.tuples.common.tuple.ExplicitTuple;

@SuppressWarnings("serial")
public class MomentCardTuple extends ExplicitTuple implements Cloneable
{
    private static final String TUPLE_TYPE = "MomentCard";

    private static final String SOURCE_FIELD = "source";
    private static final String SIGN_CARD_FIELD = "signCard";
    private static final String SPACE_FIELD = "space";
    private static final String X_FIELD = "x";
    private static final String Y_FIELD = "y";
    private static final String BOARD_X_FIELD = "boardX";
    private static final String BOARD_Y_FIELD = "boardY";
    
    public static final String STAGE_SPACE = "stage";
    public static final String BOARD_SPACE = "board";
    public static final String STAGE_PHANTOM_SPACE = "stagePhantom";
    public static final String BOARD_PHANTOM_SPACE = "boardPhantom";

    private static final String[] FIELD_NAMES = {SOURCE_FIELD, SIGN_CARD_FIELD, SPACE_FIELD, X_FIELD, Y_FIELD, BOARD_X_FIELD, BOARD_Y_FIELD};
    private static final Class[] FIELD_TYPES = {String.class, String.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class};

    @Override
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
            return (null);
        }
    }
    
    @Override
    public String getTupleType()
    {
        return TUPLE_TYPE;
    }

    @Override
    public String[] getFieldNames()
    {
        return FIELD_NAMES;
    }

    @Override
    public Class[] getFieldTypes()
    {
        return FIELD_TYPES;
    }

    public String getSource() throws TupleSpaceException
    {
        return (String)getField(SOURCE_FIELD).getValue();
    }

    public void setSource(String source) throws TupleSpaceException
    {
        setValue(SOURCE_FIELD, source);
    }

    public String getSignCard() throws TupleSpaceException
    {
        return (String)getField(SIGN_CARD_FIELD).getValue();
    }

    public void setSignCard(String signCard) throws TupleSpaceException
    {
        setValue(SIGN_CARD_FIELD, signCard);
    }

    public String getSpace() throws TupleSpaceException
    {
        return (String)getField(SPACE_FIELD).getValue();
    }

    public void setSpace(String space) throws TupleSpaceException
    {
        setValue(SPACE_FIELD, space);
    }

    public Integer getX() throws TupleSpaceException
    {
        return (Integer)getField(X_FIELD).getValue();
    }

    public void setX(Integer x) throws TupleSpaceException
    {
        setValue(X_FIELD, x);
    }

    public Integer getY() throws TupleSpaceException
    {
        return (Integer)getField(Y_FIELD).getValue();
    }

    public void setY(Integer y) throws TupleSpaceException
    {
        setValue(Y_FIELD, y);
    }

    public Integer getBoardX() throws TupleSpaceException
    {
        return (Integer)getField(BOARD_X_FIELD).getValue();
    }

    public void setBoardX(Integer boardX) throws TupleSpaceException
    {
        setValue(BOARD_X_FIELD, boardX);
    }

    public Integer getBoardY() throws TupleSpaceException
    {
        return (Integer)getField(BOARD_Y_FIELD).getValue();
    }

    public void setBoardY(Integer boardY) throws TupleSpaceException
    {
        setValue(BOARD_Y_FIELD, boardY);
    }
}
