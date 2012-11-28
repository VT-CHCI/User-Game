package edu.vt.cs.research.usergame.tuples;

import com.ibm.tspaces.TupleSpaceException;
import com.sri.tuples.common.tuple.ExplicitTuple;

@SuppressWarnings("serial")
public class ControlTuple extends ExplicitTuple implements Cloneable
{
    private static final String TUPLE_TYPE = "Control";

    private static final String DESCRIPTION_FIELD = "description";
    
    public static final String BOARD_CONTROL_DESCRIPTION = "boardControl";
    public static final String MOMENT_CONTROL_DESCRIPTION = "momentControl";
    public static final String SIGN_CONTROL_DESCRIPTION = "signControl";

    private static final String[] FIELD_NAMES = {DESCRIPTION_FIELD};
    private static final Class[] FIELD_TYPES = {String.class};
    
    public static final ControlTuple BOARD_CONTROL_TUPLE = new ControlTuple(BOARD_CONTROL_DESCRIPTION);
    public static final ControlTuple MOMENT_CONTROL_TUPLE = new ControlTuple(MOMENT_CONTROL_DESCRIPTION);
    public static final ControlTuple SIGN_CONTROL_TUPLE = new ControlTuple(SIGN_CONTROL_DESCRIPTION);
    
    public ControlTuple(String description)
    {
        try
        {
            setDescription(description);
        }
        catch (TupleSpaceException e)
        {
            e.printStackTrace();
        }
    }

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

    public String getDescription() throws TupleSpaceException
    {
        return (String)getField(DESCRIPTION_FIELD).getValue();
    }

    public void setDescription(String description) throws TupleSpaceException
    {
        setValue(DESCRIPTION_FIELD, description);
    }
}
