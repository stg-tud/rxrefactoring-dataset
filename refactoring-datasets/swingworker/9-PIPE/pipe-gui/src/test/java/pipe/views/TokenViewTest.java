package pipe.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import org.junit.Before;
import org.junit.Test;

import pipe.exceptions.TokenLockedException;

public class TokenViewTest implements Observer
{

	private TokenView tokenView;
	private TokenView newTokenView;
	private boolean called;
	private boolean newTokenViewShouldBeNull;

	@Before
	public void setUp() throws Exception
	{
		tokenView = new TokenView(true, "Default", Color.black); 
		called = false; 
		newTokenViewShouldBeNull = false; 
	}
	@Test
	public void verifyPrintsSummaryInfo() throws Exception
	{
		assertEquals("TokenView: Enabled=true, Id=Default, Color=java.awt.Color[r=0,g=0,b=0]", tokenView.toString());
	}
	@Test
	public void verifyIdIsNormalizedToTrimmedNotNullLowerCase() throws Exception
	{
		tokenView = new TokenView(true, "Default ", Color.black); 
		assertEquals("Default ",tokenView.getID()); 
		assertEquals("default",tokenView.getNormalizedID()); 
		tokenView = new TokenView(true, null, Color.black); 
		assertEquals(null,tokenView.getID()); 
		assertEquals("avoid NPE, although this is not a valid ID; should we throw?", 
				"",tokenView.getNormalizedID()); 
	}
	@Test
	public void verifyValidTokenViewHasNonBlankNonNullIdAndIsEnabled() throws Exception
	{
		assertTrue(tokenView.isValid()); 
		tokenView = new TokenView(true,  "", Color.black); 
		assertTrue(tokenView.isValid()); 
		tokenView = new TokenView(false,  "", Color.black); 
		assertFalse(tokenView.isValid()); 
	}

	@Test
	public void verifyTokenViewTellsObserversToDeleteSelfIfUpdatedTokenViewIsDisabled() throws Exception
	{
		tokenView.addObserver(this); 
		newTokenView = new TokenView(false, "Fred", Color.black); 
		newTokenViewShouldBeNull = true; 
		newTokenView.updateModelFromPrevious(tokenView); 
		assertEquals("Fred",newTokenView.getID()); 
		while (!called)
		{
			Thread.sleep(10);
		}	
	}
	@Test
	public void verifyTokenViewTellsObserversToDeleteSelfIfIsSetDisabled() throws Exception
	{
		tokenView.addObserver(this); 
		newTokenViewShouldBeNull = true; 
		tokenView.disableAndNotifyObservers(); 
		while (!called)
		{
			Thread.sleep(10);
		}	
	}
	@Override
	public void update(Observable oldObject, Object newObject)
	{
		called = true;
		assertEquals((TokenView) oldObject, tokenView); 
		if (newTokenViewShouldBeNull)  assertNull(newObject);
		else assertEquals((TokenView) newObject, newTokenView); 
	}
}
