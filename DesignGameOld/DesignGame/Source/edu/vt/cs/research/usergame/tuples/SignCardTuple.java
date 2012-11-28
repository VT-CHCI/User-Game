package edu.vt.cs.research.usergame.tuples;

import com.ibm.tspaces.TupleSpaceException;
import com.sri.tuples.common.tuple.ExplicitTuple;

@SuppressWarnings("serial")
public class SignCardTuple extends ExplicitTuple
{
    private static final String TUPLE_TYPE = "SignCard";
    
    private static final String TEXT_FIELD = "text";
    private static final String MOMENT_CARD_FIELD = "momentCard";
    private static final String STORY_DIRECTION_FIELD = "storyDirection";
    private static final String STORY_TEXT_FIELD = "storyText";
    
    private static final String[] FIELD_NAMES = {TEXT_FIELD, MOMENT_CARD_FIELD, STORY_DIRECTION_FIELD, STORY_TEXT_FIELD};
    private static final Class[] FIELD_TYPES = {String.class, String.class, String.class, String.class};
    
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
    
    public String getText() throws TupleSpaceException
    {
        return (String)getField(TEXT_FIELD).getValue();
    }
    
    public String getMomentCard() throws TupleSpaceException
    {
        return (String)getField(MOMENT_CARD_FIELD).getValue();
    }
    
    public String getStoryDirection() throws TupleSpaceException
    {
        return (String)getField(STORY_DIRECTION_FIELD).getValue();
    }
    
    public String getStoryText() throws TupleSpaceException
    {
        return (String)getField(STORY_TEXT_FIELD).getValue();
    }
    
    public void setText(String text) throws TupleSpaceException
    {
        setValue(TEXT_FIELD, text);
    }
    
    public void setMomentCard(String momentCardSourceURL) throws TupleSpaceException
    {
        setValue(MOMENT_CARD_FIELD, momentCardSourceURL);
    }
    
    public void setStoryDirection(String storyDirection) throws TupleSpaceException
    {
        setValue(STORY_DIRECTION_FIELD, storyDirection);
    }
    
    public void setStoryText(String storyText) throws TupleSpaceException
    {
        setValue(STORY_TEXT_FIELD, storyText);
    }
}
