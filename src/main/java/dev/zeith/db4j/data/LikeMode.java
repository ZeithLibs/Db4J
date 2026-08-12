package dev.zeith.db4j.data;

public enum LikeMode
{
	LIKE(true, false),
	ILIKE(false, false),
	NOT_LIKE(true, true),
	NOT_ILIKE(false, true),
	;
	
	public final boolean caseSensitive;
	public final boolean inverted;
	
	LikeMode(boolean caseSensitive, boolean inverted)
	{
		this.caseSensitive = caseSensitive;
		this.inverted = inverted;
	}
}